package com.kragnoth.creativeengineering.content.cokeoven;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * One Coke Oven Brick. Bricks join into a vertical multiblock exactly like Create's fluid tanks (ConnectivityHandler,
 * width 1-3, height 1-3). Following Create's item vault, every brick keeps its OWN items (an input slot, an output slot
 * and four coking slots), so breaking one brick only drops what it held. The creosote tank lives in the controller
 * and Create's ConnectivityHandler merges / splits it like a fluid tank's.
 *
 * <p>Heat: every column of the bottom layer looks at the block under it through Create's BoilerHeater registry.
 * No heat = 0, passive (fire, magma...) = x0.5, heated blaze burner = x1, superheated = x2; the oven runs at the
 * average. Creosote never stalls the oven: when the tank is full the rest vents as smoke (spec rule 10).
 *
 * <p>Automation (spec section 0 / tarbound-compat T1-T5): every face and the side-less query expose the same compact
 * two-slot view of the whole oven: slot 0 = input (insert only), slot 1 = output (extract only).
 */
public class CokeOvenBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Fluid, IHaveGoggleInformation {
    public static final int MAX_SIZE = 3;
    public static final int WORK_SLOTS = 4;
    public static final int CREOSOTE_PER_BLOCK = 1000;
    private static final int HEAT_CHECK_TICKS = 20;

    // --- multiblock (same fields and meaning as Create's vault / tank)
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int width = 1;
    protected int height = 1;

    // --- this brick's own contents
    protected final ItemStackHandler own = new ItemStackHandler(2) { // 0 = input, 1 = output
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

    // --- controller only
    protected final FluidTank creosote = new FluidTank(CREOSOTE_PER_BLOCK) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            sendData();
        }
    };
    protected float speed;
    protected int heatTimer;
    protected int ventTimer;

    private final CokeOvenAutomation automation = new CokeOvenAutomation(this);
    private final IFluidHandler creosoteOut = new DrainOnly();

    public CokeOvenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // A belt ending at any brick hands its items straight in, the way basins and millstones accept them.
        behaviours.add(new DirectBeltInputBehaviour(this)
                .setInsertionHandler((transported, side, simulate) -> automation.insertItem(0, transported.stack, simulate)));
    }

    // ------------------------------------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (lastKnownPos == null) {
            lastKnownPos = getBlockPos();
        } else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
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
        if (isController() && ++heatTimer >= HEAT_CHECK_TICKS) {
            heatTimer = 0;
            refreshHeat();
            refreshWorking();
        }
        CokeOvenBlockEntity c = getControllerBE();
        if (c == null || c.speed <= 0) {
            return;
        }
        cook(c);
        if (isController() && ventTimer > 0) {
            ventTimer--;
        }
    }

    /** Runs this brick's four coking slots at the oven's speed. */
    private void cook(CokeOvenBlockEntity c) {
        for (int i = 0; i < WORK_SLOTS; i++) {
            if (work.getStackInSlot(i).isEmpty()) {
                ItemStack in = own.getStackInSlot(0);
                if (in.isEmpty() || recipeFor(in).isEmpty()) {
                    continue;
                }
                work.setStackInSlot(i, in.split(1));
                own.setStackInSlot(0, in);
                progress[i] = 0;
            }
            ItemStack cooking = work.getStackInSlot(i);
            Optional<RecipeHolder<CokingRecipe>> recipe = recipeFor(cooking);
            if (recipe.isEmpty()) { // datapack changed under us: hand the item back out
                own.insertItem(1, work.extractItem(i, 1, false), false);
                continue;
            }
            CokingRecipe r = recipe.get().value();
            progress[i] = Math.min(progress[i] + c.speed, r.time());
            if (progress[i] < r.time()) {
                continue;
            }
            ItemStack result = r.result().copy();
            if (!own.insertItem(1, result, true).isEmpty()) {
                continue; // output full: this slot waits (outputs are the product, so this one may block)
            }
            own.insertItem(1, result, false);
            work.setStackInSlot(i, ItemStack.EMPTY);
            progress[i] = 0;
            c.addCreosote(r.creosote());
        }
    }

    /** Creosote is a bonus: whatever doesn't fit is vented as smoke. */
    protected void addCreosote(int mb) {
        if (mb <= 0) {
            return;
        }
        int filled = creosote.fill(new FluidStack(com.kragnoth.creativeengineering.registry.ModFluids.CREOSOTE.get(), mb),
                IFluidHandler.FluidAction.EXECUTE);
        if (filled < mb && ventTimer == 0 && level instanceof ServerLevel server) {
            ventTimer = 10;
            BlockPos top = worldPosition.above(height);
            // A dark, dense burst: reads differently from normal working smoke (tank full, creosote venting)
            server.sendParticles(ParticleTypes.LARGE_SMOKE, top.getX() + width / 2.0, top.getY() + 0.1, top.getZ() + width / 2.0,
                    10, 0.25 * width, 0.1, 0.25 * width, 0.03);
            server.sendParticles(ParticleTypes.SQUID_INK, top.getX() + width / 2.0, top.getY() + 0.2, top.getZ() + width / 2.0,
                    4, 0.2 * width, 0.05, 0.2 * width, 0.02);
        }
    }

    /** Average heat over the bottom layer's footprint, via Create's BoilerHeater registry. Also sets LIT on every brick. */
    protected void refreshHeat() {
        float total = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos below = worldPosition.offset(x, -1, z);
                float heat = BoilerHeater.findHeat(level, below, level.getBlockState(below));
                total += heat < 0 ? 0F : heat == 0 ? 0.5F : heat; // NO_HEAT / PASSIVE / heated 1, superheated 2
            }
        }
        float newSpeed = total / (width * width);
        boolean wasLit = speed > 0;
        speed = newSpeed;
        if (wasLit != (speed > 0)) {
            forEachPart(p -> p.setLit(speed > 0));
            sendData();
        }
    }

    /** WORKING on every brick while the oven is heated and at least one slot is coking (drives smoke and crackle). */
    protected void refreshWorking() {
        boolean busy = false;
        if (speed > 0) {
            for (CokeOvenBlockEntity p : parts()) {
                for (int i = 0; i < WORK_SLOTS && !busy; i++) {
                    busy = !p.work.getStackInSlot(i).isEmpty();
                }
                if (busy) {
                    break;
                }
            }
        }
        boolean working = busy;
        forEachPart(p -> {
            BlockState st = p.getBlockState();
            if (st.hasProperty(CokeOvenBlock.WORKING) && st.getValue(CokeOvenBlock.WORKING) != working) {
                p.level.setBlock(p.worldPosition, st.setValue(CokeOvenBlock.WORKING, working), Block.UPDATE_CLIENTS);
            }
        });
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (state.hasProperty(CokeOvenBlock.LIT) && state.getValue(CokeOvenBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(CokeOvenBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    protected Optional<RecipeHolder<CokingRecipe>> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(ModRecipes.COKING_TYPE.get(), new SingleRecipeInput(stack), level);
    }

    // ------------------------------------------------------------------------------------------------ parts

    /** Every brick of this oven (controller first), or just this one if the oven isn't formed. */
    public List<CokeOvenBlockEntity> parts() {
        CokeOvenBlockEntity c = getControllerBE();
        List<CokeOvenBlockEntity> out = new ArrayList<>();
        if (c == null || level == null) {
            out.add(this);
            return out;
        }
        for (int y = 0; y < c.height; y++) {
            for (int x = 0; x < c.width; x++) {
                for (int z = 0; z < c.width; z++) {
                    CokeOvenBlockEntity p = ConnectivityHandler.partAt(ModBlockEntities.COKE_OVEN.get(), level,
                            c.worldPosition.offset(x, y, z));
                    if (p != null) {
                        out.add(p);
                    }
                }
            }
        }
        return out;
    }

    private void forEachPart(java.util.function.Consumer<CokeOvenBlockEntity> action) {
        parts().forEach(action);
    }

    /** Oven speed multiplier from heat (controller's value). 0 = no heat. */
    public float heatSpeed() {
        CokeOvenBlockEntity c = getControllerBE();
        return c == null ? 0 : c.speed;
    }

    /** [busy slots, total slots] across the whole oven. */
    public int[] slotUse() {
        int busy = 0;
        List<CokeOvenBlockEntity> ps = parts();
        for (CokeOvenBlockEntity p : ps) {
            for (int i = 0; i < WORK_SLOTS; i++) {
                if (!p.work.getStackInSlot(i).isEmpty()) {
                    busy++;
                }
            }
        }
        return new int[]{busy, ps.size() * WORK_SLOTS};
    }

    /** Average progress (0..1) of the slots currently coking, or 0 if none. */
    public float averageProgress() {
        float sum = 0;
        int n = 0;
        for (CokeOvenBlockEntity p : parts()) {
            for (int i = 0; i < WORK_SLOTS; i++) {
                ItemStack s = p.work.getStackInSlot(i);
                if (s.isEmpty()) {
                    continue;
                }
                int time = p.recipeFor(s).map(r -> r.value().time()).orElse(0);
                if (time > 0) {
                    sum += Math.min(1F, p.progress[i] / time);
                    n++;
                }
            }
        }
        return n == 0 ? 0 : sum / n;
    }

    /** The oven's creosote tank (controller's). */
    public FluidTank creosoteTank() {
        CokeOvenBlockEntity c = getControllerBE();
        return c == null ? creosote : c.creosote;
    }

    public ItemStackHandler ownItems() {
        return own;
    }

    public ItemStackHandler workItems() {
        return work;
    }

    // ------------------------------------------------------------------------------------------------ automation

    /** The compact view handed to funnels, hoppers, arms, belts and autocrafters (same on every face). */
    public IItemHandler automationItems() {
        return automation;
    }

    /** Creosote, drain only. */
    public IFluidHandler automationFluids() {
        return creosoteOut;
    }

    /**
     * Why this oven can't make {@code expectedOutputs} right now, or null if it can. Public for autocrafters
     * (Tarbound reads it by reflection, docs/tarbound-compat.md T7) and used by the goggles.
     */
    @Nullable
    public Component automationBlocker(List<ItemStack> expectedOutputs) {
        CokeOvenBlockEntity c = getControllerBE();
        if (c == null || c.speed <= 0) {
            return Component.translatable("creativeengineering.coke_oven.blocker.no_heat");
        }
        if (!expectedOutputs.isEmpty() && !automation.canAcceptOutput(expectedOutputs.get(0))) {
            return Component.translatable("creativeengineering.coke_oven.blocker.output_full");
        }
        return null;
    }

    private final class DrainOnly implements IFluidHandler {
        private FluidTank tank() {
            CokeOvenBlockEntity c = getControllerBE();
            return c == null ? creosote : c.creosote;
        }

        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int t) { return tank().getFluid(); }
        @Override public int getTankCapacity(int t) { return tank().getCapacity(); }
        @Override public boolean isFluidValid(int t, FluidStack s) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return tank().drain(resource, action); }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return tank().drain(maxDrain, action); }
    }

    // ------------------------------------------------------------------------------------------------ goggles

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CokeOvenBlockEntity c = getControllerBE();
        if (c == null) {
            return false;
        }
        List<CokeOvenBlockEntity> parts = c.parts();
        int busy = 0;
        for (CokeOvenBlockEntity p : parts) {
            for (int i = 0; i < WORK_SLOTS; i++) {
                if (!p.work.getStackInSlot(i).isEmpty()) {
                    busy++;
                }
            }
        }
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.coke_oven_bricks")));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.size",
                c.width, c.width, c.height)).withStyle(ChatFormatting.GRAY));
        Component heat = c.speed <= 0
                ? Component.translatable("creativeengineering.coke_oven.heat.none").withStyle(ChatFormatting.RED)
                : Component.literal("x" + trim(c.speed)).withStyle(ChatFormatting.GOLD);
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.heat", heat))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.slots",
                busy, parts.size() * WORK_SLOTS)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.creosote",
                c.creosote.getFluidAmount(), c.creosote.getCapacity())).withStyle(ChatFormatting.GRAY));
        Component blocker = automationBlocker(List.of());
        if (blocker != null) {
            tooltip.add(Component.literal("     ").append(blocker).withStyle(ChatFormatting.RED));
        }
        return true;
    }

    private static String trim(float f) {
        return f == (int) f ? Integer.toString((int) f) : String.format("%.2f", f).replaceAll("0+$", "");
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
    public CokeOvenBlockEntity getControllerBE() {
        if (isController()) {
            return this;
        }
        return level != null && level.getBlockEntity(controller) instanceof CokeOvenBlockEntity c ? c : null;
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
        applyCreosoteSize(1);
        invalidateCapabilities();
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        invalidateCapabilities();
        heatTimer = HEAT_CHECK_TICKS; // re-check heat for the new footprint next tick
        setChanged();
        sendData();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return MAX_SIZE;
    }

    @Override
    public int getMaxWidth() {
        return MAX_SIZE;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    // Create's ConnectivityHandler moves creosote between bricks through these, as it does for fluid tanks.
    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize(int tank) {
        return CREOSOTE_PER_BLOCK;
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyCreosoteSize(blocks);
    }

    @Override
    public IFluidTank getTank(int tank) {
        return creosote;
    }

    @Override
    public FluidStack getFluid(int tank) {
        return creosote.getFluid().copy();
    }

    private void applyCreosoteSize(int blocks) {
        creosote.setCapacity(blocks * CREOSOTE_PER_BLOCK);
        int overflow = creosote.getFluidAmount() - creosote.getCapacity();
        if (overflow > 0) {
            creosote.drain(overflow, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    // ------------------------------------------------------------------------------------------------ save

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
            tag.putFloat("Speed", speed);
            tag.put("Creosote", creosote.writeToNBT(registries, new CompoundTag()));
            tag.putInt("CreosoteCapacity", creosote.getCapacity());
        }
        if (!clientPacket) {
            tag.put("Items", own.serializeNBT(registries));
            tag.put("Work", work.serializeNBT(registries));
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
            speed = tag.getFloat("Speed");
            creosote.setCapacity(Math.max(CREOSOTE_PER_BLOCK, tag.getInt("CreosoteCapacity")));
            creosote.readFromNBT(registries, tag.getCompound("Creosote"));
        }
        if (!clientPacket) {
            own.deserializeNBT(registries, tag.getCompound("Items"));
            work.deserializeNBT(registries, tag.getCompound("Work"));
            for (int i = 0; i < WORK_SLOTS; i++) {
                progress[i] = tag.getFloat("Progress" + i);
            }
        }
    }
}
