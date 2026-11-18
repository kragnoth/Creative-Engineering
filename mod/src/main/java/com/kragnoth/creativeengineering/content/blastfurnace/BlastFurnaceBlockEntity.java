package com.kragnoth.creativeengineering.content.blastfurnace;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.kragnoth.creativeengineering.registry.ModItems;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * One Blast Furnace Brick (docs/steelworks-spec.md 4). Bricks stack like Create's fluid tanks (1-3 wide, 1-6 tall).
 * Like the coke oven, each brick keeps its own items: input (iron), fuel, metal out, slag out, plus two smelting slots.
 *
 * <ul>
 *   <li>Heat: at least one heated Blaze Burner under the bottom layer (passive heat is not enough). Speed is the
 *       average over the footprint: heated x1, superheated x2.</li>
 *   <li>Air blast: encased fans next to the bottom layer, blowing in. With none, iron becomes cast iron; with any,
 *       steel. Each fan past the first adds +25% speed (4 counted).</li>
 *   <li>Fuel: each item burns its recipe's iron units of fuel, from the data map blast_furnace_fuel
 *       (coke 2, charcoal 1, coal 0.5).</li>
 *   <li>Slag: a bonus. When its slot is full, new slag is voided; it never stops the furnace.</li>
 * </ul>
 */
public class BlastFurnaceBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Inventory, IHaveGoggleInformation {
    public static final int MAX_WIDTH = 3;
    public static final int MAX_HEIGHT = 6;
    public static final int WORK_SLOTS = 2;
    public static final int IN = 0, FUEL = 1, METAL = 2, SLAG = 3;
    private static final int CHECK_TICKS = 20;

    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int width = 1;
    protected int height = 1;

    protected final ItemStackHandler own = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    protected final ItemStackHandler work = new ItemStackHandler(WORK_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    protected final float[] progress = new float[WORK_SLOTS];
    protected float fuelCredit;

    // controller only
    protected float heat;
    protected int fans;
    protected int checkTimer;

    private final BlastFurnaceAutomation automation = new BlastFurnaceAutomation(this);

    public BlastFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).setInsertionHandler((t, side, simulate) -> {
            ItemStack rest = automation.insertItem(IN, t.stack, simulate);
            return rest.getCount() == t.stack.getCount() ? automation.insertItem(FUEL, t.stack, simulate) : rest;
        }));
    }

    // ------------------------------------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (lastKnownPos == null) {
            lastKnownPos = getBlockPos();
        } else if (!lastKnownPos.equals(worldPosition)) {
            removeController(true);
            lastKnownPos = worldPosition;
            return;
        }
        if (updateConnectivity) {
            updateConnectivity = false;
            if (level != null && !level.isClientSide && isController()) {
                ConnectivityHandler.formMulti(this);
            }
        }
        if (level == null || level.isClientSide) {
            return;
        }
        if (isController() && ++checkTimer >= CHECK_TICKS) {
            checkTimer = 0;
            refreshHeatAndAir();
            refreshWorking();
        }
        BlastFurnaceBlockEntity c = getControllerBE();
        if (c != null && c.heat > 0) {
            smelt(c);
        }
    }

    private void smelt(BlastFurnaceBlockEntity c) {
        float speed = c.speed();
        boolean air = c.fans > 0;
        for (int i = 0; i < WORK_SLOTS; i++) {
            if (work.getStackInSlot(i).isEmpty()) {
                ItemStack in = own.getStackInSlot(IN);
                Optional<RecipeHolder<BlastSmeltingRecipe>> r = recipeFor(in);
                if (r.isEmpty() || !payFuel(r.get().value().ironUnits())) {
                    continue;
                }
                work.setStackInSlot(i, in.split(1));
                own.setStackInSlot(IN, in);
                progress[i] = 0;
            }
            Optional<RecipeHolder<BlastSmeltingRecipe>> r = recipeFor(work.getStackInSlot(i));
            if (r.isEmpty()) {
                own.insertItem(METAL, work.extractItem(i, 1, false), false);
                continue;
            }
            BlastSmeltingRecipe recipe = r.get().value();
            progress[i] = Math.min(progress[i] + speed, recipe.time());
            if (progress[i] < recipe.time()) {
                continue;
            }
            ItemStack result = recipe.result(air);
            if (!own.insertItem(METAL, result, true).isEmpty()) {
                continue; // metal output full: this slot waits
            }
            own.insertItem(METAL, result, false);
            if (recipe.slagChance() > 0 && level.random.nextFloat() < recipe.slagChance()) {
                own.insertItem(SLAG, new ItemStack(ModItems.SLAG.get()), false); // full slag slot: voided
            }
            work.setStackInSlot(i, ItemStack.EMPTY);
            progress[i] = 0;
        }
    }

    /** Takes {@code units} of fuel credit, burning fuel items from this brick's fuel slot as needed. */
    private boolean payFuel(float units) {
        while (fuelCredit < units) {
            ItemStack fuel = own.getStackInSlot(FUEL);
            float per = BlastFuel.unitsOf(fuel);
            if (per <= 0) {
                return false;
            }
            fuelCredit += per;
            fuel.shrink(1);
            own.setStackInSlot(FUEL, fuel);
        }
        fuelCredit -= units;
        return true;
    }

    /** Heat (passive doesn't count) averaged over the footprint, and fans blowing into the bottom layer. */
    protected void refreshHeatAndAir() {
        float total = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos below = worldPosition.offset(x, -1, z);
                float h = BoilerHeater.findHeat(level, below, level.getBlockState(below));
                total += h >= 1 ? h : 0;
            }
        }
        heat = total / (width * width);
        int n = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos p = worldPosition.offset(x, 0, z);
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    BlockPos side = p.relative(d);
                    if (level.getBlockEntity(side) instanceof EncasedFanBlockEntity fan
                            && fan.getAirFlowDirection() == d.getOpposite()) {
                        n++;
                    }
                }
            }
        }
        fans = n;
        sendData(); // LIT / WORKING are set right after, in refreshWorking()
    }

    /** Effective speed: heat x air bonus (+25% per fan after the first, up to 4 fans). */
    public float speed() {
        BlastFurnaceBlockEntity c = getControllerBE();
        if (c == null || c.heat <= 0) {
            return 0;
        }
        return c.heat * (c.fans <= 1 ? 1F : 1F + 0.25F * (Math.min(c.fans, 4) - 1));
    }

    public int fans() {
        BlastFurnaceBlockEntity c = getControllerBE();
        return c == null ? 0 : c.fans;
    }

    protected void refreshWorking() {
        boolean busy = false;
        if (heat > 0) {
            outer:
            for (BlastFurnaceBlockEntity p : parts()) {
                for (int i = 0; i < WORK_SLOTS; i++) {
                    if (!p.work.getStackInSlot(i).isEmpty()) {
                        busy = true;
                        break outer;
                    }
                }
            }
        }
        boolean w = busy;
        forEachPart(p -> {
            p.setState(BlastFurnaceBlock.WORKING, w);
            p.setState(BlastFurnaceBlock.LIT, heat > 0);
        });
    }

    private void setState(net.minecraft.world.level.block.state.properties.BooleanProperty prop, boolean value) {
        BlockState st = getBlockState();
        if (st.hasProperty(prop) && st.getValue(prop) != value) {
            level.setBlock(worldPosition, st.setValue(prop, value), Block.UPDATE_CLIENTS);
        }
    }

    protected Optional<RecipeHolder<BlastSmeltingRecipe>> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(ModRecipes.BLAST_SMELTING_TYPE.get(), new SingleRecipeInput(stack), level);
    }

    // ------------------------------------------------------------------------------------------------ parts & views

    public List<BlastFurnaceBlockEntity> parts() {
        BlastFurnaceBlockEntity c = getControllerBE();
        List<BlastFurnaceBlockEntity> out = new ArrayList<>();
        if (c == null || level == null) {
            out.add(this);
            return out;
        }
        for (int y = 0; y < c.height; y++) {
            for (int x = 0; x < c.width; x++) {
                for (int z = 0; z < c.width; z++) {
                    BlastFurnaceBlockEntity p = ConnectivityHandler.partAt(ModBlockEntities.BLAST_FURNACE.get(), level,
                            c.worldPosition.offset(x, y, z));
                    if (p != null) {
                        out.add(p);
                    }
                }
            }
        }
        return out;
    }

    private void forEachPart(java.util.function.Consumer<BlastFurnaceBlockEntity> action) {
        parts().forEach(action);
    }

    public ItemStackHandler ownItems() {
        return own;
    }

    public ItemStackHandler workItems() {
        return work;
    }

    public void resetProgress() {
        Arrays.fill(progress, 0F);
        setChanged();
    }

    public IItemHandler automationItems() {
        return automation;
    }

    public int[] slotUse() {
        int busy = 0;
        List<BlastFurnaceBlockEntity> ps = parts();
        for (BlastFurnaceBlockEntity p : ps) {
            for (int i = 0; i < WORK_SLOTS; i++) {
                if (!p.work.getStackInSlot(i).isEmpty()) {
                    busy++;
                }
            }
        }
        return new int[]{busy, ps.size() * WORK_SLOTS};
    }

    public float averageProgress() {
        float sum = 0;
        int n = 0;
        for (BlastFurnaceBlockEntity p : parts()) {
            for (int i = 0; i < WORK_SLOTS; i++) {
                ItemStack s = p.work.getStackInSlot(i);
                int t = s.isEmpty() ? 0 : p.recipeFor(s).map(r -> r.value().time()).orElse(0);
                if (t > 0) {
                    sum += Math.min(1F, p.progress[i] / t);
                    n++;
                }
            }
        }
        return n == 0 ? 0 : sum / n;
    }

    /** Why this furnace can't make {@code expectedOutputs} now, or null (goggles, Jade, Tarbound T7). */
    @Nullable
    public Component automationBlocker(List<ItemStack> expectedOutputs) {
        BlastFurnaceBlockEntity c = getControllerBE();
        if (c == null || c.heat <= 0) {
            return Component.translatable("creativeengineering.blast_furnace.blocker.no_heat");
        }
        boolean wantsSteel = expectedOutputs.stream().anyMatch(s -> s.is(ModItems.STEEL_INGOT.get()));
        if (wantsSteel && c.fans == 0) {
            return Component.translatable("creativeengineering.blast_furnace.blocker.no_air");
        }
        if (automation.getStackInSlot(FUEL).isEmpty() && parts().stream().allMatch(p -> p.fuelCredit <= 0)
                && !automation.getStackInSlot(IN).isEmpty()) {
            return Component.translatable("creativeengineering.blast_furnace.blocker.no_fuel");
        }
        return null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        BlastFurnaceBlockEntity c = getControllerBE();
        if (c == null) {
            return false;
        }
        int[] use = slotUse();
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.blast_furnace_bricks")));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.size", c.width, c.width, c.height)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.blast_furnace.air." + (c.fans > 0 ? "on" : "off"), c.fans)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.blast_furnace.speed", String.format("%.2f", speed()))).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.blast_furnace.slots", use[0], use[1])).withStyle(ChatFormatting.GRAY));
        Component b = automationBlocker(List.of());
        if (b != null) {
            tooltip.add(Component.literal("     ").append(b).withStyle(ChatFormatting.RED));
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------ multiblock

    public void updateConnectivitySoon() {
        updateConnectivity = true;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.equals(controller);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlastFurnaceBlockEntity getControllerBE() {
        if (isController()) {
            return this;
        }
        return level != null && level.getBlockEntity(controller) instanceof BlastFurnaceBlockEntity c ? c : null;
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    public void setController(BlockPos controller) {
        if (level != null && level.isClientSide && !isVirtual()) {
            return;
        }
        if (controller.equals(this.controller)) {
            return;
        }
        this.controller = controller;
        invalidateCapabilities();
        setChanged();
        sendData();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level == null || level.isClientSide) {
            return;
        }
        updateConnectivity = true;
        controller = null;
        width = 1;
        height = 1;
        invalidateCapabilities();
        setChanged();
        sendData();
    }

    @Override public BlockPos getLastKnownPos() { return lastKnownPos; }
    @Override public void preventConnectivityUpdate() { updateConnectivity = false; }

    @Override
    public void notifyMultiUpdated() {
        invalidateCapabilities();
        checkTimer = CHECK_TICKS;
        setChanged();
        sendData();
    }

    @Override public Direction.Axis getMainConnectionAxis() { return Direction.Axis.Y; }
    @Override public int getMaxLength(Direction.Axis longAxis, int width) { return MAX_HEIGHT; }
    @Override public int getMaxWidth() { return MAX_WIDTH; }
    @Override public int getHeight() { return height; }
    @Override public void setHeight(int height) { this.height = height; }
    @Override public int getWidth() { return width; }
    @Override public void setWidth(int width) { this.width = width; }
    @Override public boolean hasInventory() { return true; }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (updateConnectivity) {
            tag.putBoolean("Uninitialized", true);
        }
        if (lastKnownPos != null) {
            tag.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        }
        if (!isController()) {
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
        } else {
            tag.putInt("Width", width);
            tag.putInt("Height", height);
            tag.putFloat("Heat", heat);
            tag.putInt("Fans", fans);
        }
        if (!clientPacket) {
            tag.put("Items", own.serializeNBT(registries));
            tag.put("Work", work.serializeNBT(registries));
            tag.putFloat("FuelCredit", fuelCredit);
            for (int i = 0; i < WORK_SLOTS; i++) {
                tag.putFloat("Progress" + i, progress[i]);
            }
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        updateConnectivity = tag.contains("Uninitialized");
        lastKnownPos = tag.contains("LastKnownPos") ? NbtUtils.readBlockPos(tag, "LastKnownPos").orElse(null) : null;
        controller = tag.contains("Controller") ? NbtUtils.readBlockPos(tag, "Controller").orElse(null) : null;
        if (isController()) {
            width = Math.max(1, tag.getInt("Width"));
            height = Math.max(1, tag.getInt("Height"));
            heat = tag.getFloat("Heat");
            fans = tag.getInt("Fans");
        }
        if (!clientPacket) {
            own.deserializeNBT(registries, tag.getCompound("Items"));
            work.deserializeNBT(registries, tag.getCompound("Work"));
            fuelCredit = tag.getFloat("FuelCredit");
            for (int i = 0; i < WORK_SLOTS; i++) {
                progress[i] = tag.getFloat("Progress" + i);
            }
        }
    }
}
