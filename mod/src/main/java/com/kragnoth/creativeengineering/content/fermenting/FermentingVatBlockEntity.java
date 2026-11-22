package com.kragnoth.creativeengineering.content.fermenting;

import com.kragnoth.creativeengineering.content.shared.MachineReadout;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * One Fermenting Vat block (docs/fermentation-spec.md). Any box of vats 1..3 wide and 1..4 tall forms one vat (own
 * flood-fill forming, as the alloy furnace); the min corner is the controller and runs it.
 * <ul>
 *   <li>Storage stays in each block (nothing to merge or lose when a vat is split): one ingredient slot, an input tank
 *       and a product tank. Automation and buckets see the whole vat as one: 1 item slot (insert) and 2 tanks
 *       (0 = input, fill only; 1 = product, drain only), the same from every side.</li>
 *   <li>Heat under the bottom layer, averaged over the footprint: none 0, passive (campfire, smouldering Blaze Burner)
 *       x1, lit Blaze Burner x2, superheated x3. Needs some heat to work.</li>
 *   <li>Batches: one recipe at a time, run as many times at once as the vat has blocks (and ingredients allow).</li>
 * </ul>
 */
public class FermentingVatBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, MachineReadout {
    public static final int MAX_WIDTH = 3, MAX_HEIGHT = 4, TANK_PER_BLOCK = 2000;
    private static final int CHECK_TICKS = 20;

    @Nullable protected BlockPos controller;
    protected int sx = 1, sy = 1, sz = 1;
    protected boolean recheck = true;

    protected final ItemStackHandler slot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int s) {
            setChanged();
        }
    };
    protected final FluidTank input = new FluidTank(TANK_PER_BLOCK) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            sendData();
        }
    };
    protected final FluidTank product = new FluidTank(TANK_PER_BLOCK) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            sendData();
        }
    };

    // controller only
    @Nullable protected ResourceLocation active;
    protected int batches;
    protected float progress;
    protected float speed;
    protected int checkTimer;

    private final VatItems items = new VatItems();
    private final VatFluids fluids = new VatFluids();

    public FermentingVatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).setInsertionHandler((t, side, sim) -> items.insertItem(0, t.stack, sim)));
    }

    // ------------------------------------------------------------------------------------------------ forming

    public void requestRecheck() {
        recheck = true;
    }

    public boolean isController() {
        return worldPosition.equals(controller);
    }

    @Nullable
    public FermentingVatBlockEntity controllerBE() {
        if (controller == null || level == null) {
            return null;
        }
        if (isController()) {
            return this;
        }
        return level.getBlockEntity(controller) instanceof FermentingVatBlockEntity c && c.isController() ? c : null;
    }

    public List<FermentingVatBlockEntity> members() {
        FermentingVatBlockEntity c = controllerBE();
        List<FermentingVatBlockEntity> out = new ArrayList<>();
        if (c == null) {
            out.add(this);
            return out;
        }
        for (int x = 0; x < c.sx; x++) {
            for (int y = 0; y < c.sy; y++) {
                for (int z = 0; z < c.sz; z++) {
                    if (level.getBlockEntity(c.worldPosition.offset(x, y, z)) instanceof FermentingVatBlockEntity m) {
                        out.add(m);
                    }
                }
            }
        }
        return out;
    }

    private void checkStructure() {
        Block vat = ModBlocks.FERMENTING_VAT.get();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(worldPosition);
        seen.add(worldPosition);
        int cap = MAX_WIDTH * MAX_WIDTH * MAX_HEIGHT;
        boolean tooBig = false;
        while (!queue.isEmpty() && !tooBig) {
            BlockPos p = queue.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (seen.contains(n)) {
                    continue;
                }
                if (!isVirtual() && !level.isLoaded(n)) {
                    return;
                }
                if (level.getBlockState(n).is(vat)) {
                    seen.add(n);
                    queue.add(n);
                    if (seen.size() > cap) {
                        tooBig = true;
                        break;
                    }
                }
            }
        }
        int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, z0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE, z1 = Integer.MIN_VALUE;
        List<FermentingVatBlockEntity> found = new ArrayList<>();
        for (BlockPos p : seen) {
            if (level.getBlockEntity(p) instanceof FermentingVatBlockEntity be) {
                found.add(be);
                be.recheck = false;
            }
            x0 = Math.min(x0, p.getX()); y0 = Math.min(y0, p.getY()); z0 = Math.min(z0, p.getZ());
            x1 = Math.max(x1, p.getX()); y1 = Math.max(y1, p.getY()); z1 = Math.max(z1, p.getZ());
        }
        int dx = x1 - x0 + 1, dy = y1 - y0 + 1, dz = z1 - z0 + 1;
        boolean valid = !tooBig && found.size() == seen.size() && dx * dy * dz == seen.size()
                && dx <= MAX_WIDTH && dz <= MAX_WIDTH && dy <= MAX_HEIGHT;
        if (!valid) {   // a shape that isn't a box: every block works alone
            found.forEach(m -> m.setController(m.worldPosition, 1, 1, 1));
            return;
        }
        BlockPos min = new BlockPos(x0, y0, z0);
        for (FermentingVatBlockEntity m : found) {
            m.setController(min, dx, dy, dz);
        }
    }

    private void setController(BlockPos c, int x, int y, int z) {
        boolean changed = !c.equals(controller) || sx != x || sy != y || sz != z;
        controller = c;
        sx = x; sy = y; sz = z;
        if (changed) {
            invalidateCapabilities();
            BlockPos rel = worldPosition.subtract(c);
            boolean top = rel.getY() == y - 1, bottom = rel.getY() == 0;
            boolean valve = top && rel.getX() == x / 2 && rel.getZ() == z / 2;   // one pressure valve, top middle
            BlockState st = getBlockState()
                    .setValue(FermentingVatBlock.TOP, top).setValue(FermentingVatBlock.BOTTOM, bottom)
                    .setValue(FermentingVatBlock.VALVE, valve)
                    .setValue(FermentingVatBlock.WEST, rel.getX() == 0).setValue(FermentingVatBlock.EAST, rel.getX() == x - 1)
                    .setValue(FermentingVatBlock.NORTH, rel.getZ() == 0).setValue(FermentingVatBlock.SOUTH, rel.getZ() == z - 1);
            if (st != getBlockState()) {
                level.setBlock(worldPosition, st, Block.UPDATE_CLIENTS);
            }
            if (!isController()) {
                active = null;
                batches = 0;
                progress = 0;
            }
        }
        setChanged();
        sendData();
    }

    // ------------------------------------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (recheck && (!level.isClientSide || isVirtual())) {
            recheck = false;
            checkStructure();
        }
        if (level.isClientSide || !isController()) {
            return;
        }
        if (++checkTimer >= CHECK_TICKS) {
            checkTimer = 0;
            refreshHeat();
        }
        if (speed <= 0) {
            return;
        }
        FermentingRecipe r = recipe(active);
        if (r == null) {
            active = null;
            startBatch();
            return;
        }
        progress += speed;
        if (progress >= r.time()) {
            FluidStack out = r.result().copyWithAmount(r.result().getAmount() * batches);
            if (fillProduct(out, IFluidHandler.FluidAction.SIMULATE) == out.getAmount()) {
                fillProduct(out, IFluidHandler.FluidAction.EXECUTE);
                active = null;
                batches = 0;
                progress = 0;
                setWorking(false);
            }
        }
        setChanged();
    }

    private void refreshHeat() {
        float total = 0;
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                BlockPos below = worldPosition.offset(x, -1, z);
                float h = BoilerHeater.findHeat(level, below, level.getBlockState(below));
                total += h < 0 ? 0 : h >= 2 ? 3 : h >= 1 ? 2 : 1;
            }
        }
        speed = total / (sx * sz);
        sendData();
    }

    @Nullable
    private FermentingRecipe recipe(@Nullable ResourceLocation id) {
        if (id == null || level == null) {
            return null;
        }
        return level.getRecipeManager().byKey(id).map(h -> h.value() instanceof FermentingRecipe f ? f : null).orElse(null);
    }

    private void startBatch() {
        List<FermentingVatBlockEntity> ms = members();
        for (RecipeHolder<FermentingRecipe> h : level.getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING_TYPE.get())) {
            FermentingRecipe r = h.value();
            int n = ms.size();
            for (SizedIngredient need : r.ingredients()) {
                n = Math.min(n, countItems(ms, need) / need.count());
            }
            FluidStack fluid = inputFluid(ms);
            if (fluid.isEmpty() || !r.fluid().ingredient().test(fluid)) {
                continue;
            }
            n = Math.min(n, totalInput(ms, fluid) / r.fluid().amount());
            if (n <= 0) {
                continue;
            }
            int room = fillProduct(r.result().copyWithAmount(Integer.MAX_VALUE / 2), IFluidHandler.FluidAction.SIMULATE);
            n = Math.min(n, room / r.result().getAmount());
            if (n <= 0) {
                continue;
            }
            for (SizedIngredient need : r.ingredients()) {
                takeItems(ms, need, need.count() * n);
            }
            drainInput(ms, fluid.copyWithAmount(r.fluid().amount() * n));
            active = h.id();
            batches = n;
            progress = 0;
            setWorking(true);
            return;
        }
    }

    private void setWorking(boolean on) {
        for (FermentingVatBlockEntity m : members()) {
            BlockState s = m.getBlockState();
            if (s.getValue(FermentingVatBlock.WORKING) != on) {
                level.setBlock(m.worldPosition, s.setValue(FermentingVatBlock.WORKING, on), Block.UPDATE_CLIENTS);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ storage helpers

    private static int countItems(List<FermentingVatBlockEntity> ms, SizedIngredient need) {
        int n = 0;
        for (FermentingVatBlockEntity m : ms) {
            ItemStack s = m.slot.getStackInSlot(0);
            if (!s.isEmpty() && need.ingredient().test(s)) {
                n += s.getCount();
            }
        }
        return n;
    }

    private static void takeItems(List<FermentingVatBlockEntity> ms, SizedIngredient need, int count) {
        for (FermentingVatBlockEntity m : ms) {
            ItemStack s = m.slot.getStackInSlot(0);
            if (count > 0 && !s.isEmpty() && need.ingredient().test(s)) {
                count -= m.slot.extractItem(0, count, false).getCount();
            }
        }
    }

    private static FluidStack inputFluid(List<FermentingVatBlockEntity> ms) {
        for (FermentingVatBlockEntity m : ms) {
            if (!m.input.isEmpty()) {
                return m.input.getFluid();
            }
        }
        return FluidStack.EMPTY;
    }

    private static int totalInput(List<FermentingVatBlockEntity> ms, FluidStack kind) {
        int n = 0;
        for (FermentingVatBlockEntity m : ms) {
            if (FluidStack.isSameFluidSameComponents(m.input.getFluid(), kind)) {
                n += m.input.getFluidAmount();
            }
        }
        return n;
    }

    private static void drainInput(List<FermentingVatBlockEntity> ms, FluidStack want) {
        int left = want.getAmount();
        for (FermentingVatBlockEntity m : ms) {
            if (left > 0) {
                left -= m.input.drain(want.copyWithAmount(left), IFluidHandler.FluidAction.EXECUTE).getAmount();
            }
        }
    }

    private int fillProduct(FluidStack stack, IFluidHandler.FluidAction action) {
        int done = 0;
        for (FermentingVatBlockEntity m : members()) {
            if (done < stack.getAmount()) {
                done += m.product.fill(stack.copyWithAmount(stack.getAmount() - done), action);
            }
        }
        return done;
    }

    // ------------------------------------------------------------------------------------------------ views

    public IItemHandler automationItems() {
        return items;
    }

    public IFluidHandler automationFluids() {
        return fluids;
    }

    /** Empty-hand use: hands the ingredients back. */
    public List<ItemStack> takeAllItems() {
        List<ItemStack> out = new ArrayList<>();
        for (FermentingVatBlockEntity m : members()) {
            ItemStack s = m.slot.getStackInSlot(0);
            if (!s.isEmpty()) {
                out.add(s.copy());
                m.slot.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
        return out;
    }

    public List<ItemStack> contentsToDrop() {
        return slot.getStackInSlot(0).isEmpty() ? List.of() : List.of(slot.getStackInSlot(0).copy());
    }

    private boolean usedByAnyRecipe(ItemStack s) {
        return level != null && level.getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING_TYPE.get()).stream()
                .anyMatch(h -> h.value().usesItem(s));
    }

    private boolean fluidUsedByAnyRecipe(FluidStack f) {
        return level != null && level.getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING_TYPE.get()).stream()
                .anyMatch(h -> h.value().fluid().ingredient().test(f));
    }

    /** The whole vat as one item slot: insert ingredients only (each block's slot holds one kind). */
    private final class VatItems implements IItemHandler {
        @Override public int getSlots() { return 1; }

        @Override
        public ItemStack getStackInSlot(int i) {
            ItemStack kind = ItemStack.EMPTY;
            int n = 0;
            for (FermentingVatBlockEntity m : members()) {
                ItemStack s = m.slot.getStackInSlot(0);
                if (!s.isEmpty() && (kind.isEmpty() || ItemStack.isSameItemSameComponents(kind, s))) {
                    kind = s;
                    n += s.getCount();
                }
            }
            return kind.isEmpty() ? ItemStack.EMPTY : kind.copyWithCount(n);
        }

        @Override public int getSlotLimit(int i) { return 64 * members().size(); }
        @Override public boolean isItemValid(int i, ItemStack s) { return !s.isEmpty() && usedByAnyRecipe(s); }

        @Override
        public ItemStack insertItem(int i, ItemStack stack, boolean simulate) {
            if (!isItemValid(i, stack)) {
                return stack;
            }
            ItemStack rest = stack.copy();
            for (FermentingVatBlockEntity m : members()) {   // top up matching slots, then empty ones
                if (ItemStack.isSameItemSameComponents(m.slot.getStackInSlot(0), rest)) {
                    rest = m.slot.insertItem(0, rest, simulate);
                }
            }
            for (FermentingVatBlockEntity m : members()) {
                if (!rest.isEmpty() && m.slot.getStackInSlot(0).isEmpty()) {
                    rest = m.slot.insertItem(0, rest, simulate);
                }
            }
            return rest;
        }

        @Override public ItemStack extractItem(int i, int amount, boolean simulate) { return ItemStack.EMPTY; }
    }

    /** The whole vat as two tanks: 0 = input (fill only, recipe fluids), 1 = product (drain only). */
    private final class VatFluids implements IFluidHandler {
        @Override public int getTanks() { return 2; }

        @Override
        public FluidStack getFluidInTank(int t) {
            List<FermentingVatBlockEntity> ms = members();
            FluidStack kind = FluidStack.EMPTY;
            int n = 0;
            for (FermentingVatBlockEntity m : ms) {
                FluidStack f = (t == 0 ? m.input : m.product).getFluid();
                if (!f.isEmpty()) {
                    if (kind.isEmpty()) {
                        kind = f;
                    }
                    n += f.getAmount();
                }
            }
            return kind.isEmpty() ? FluidStack.EMPTY : kind.copyWithAmount(n);
        }

        @Override public int getTankCapacity(int t) { return TANK_PER_BLOCK * members().size(); }
        @Override public boolean isFluidValid(int t, FluidStack s) { return t == 0 && fluidUsedByAnyRecipe(s); }

        @Override
        public int fill(FluidStack res, FluidAction action) {
            if (res.isEmpty() || !fluidUsedByAnyRecipe(res)) {
                return 0;
            }
            int done = 0;
            for (FermentingVatBlockEntity m : members()) {
                if (done < res.getAmount()) {
                    done += m.input.fill(res.copyWithAmount(res.getAmount() - done), action);
                }
            }
            return done;
        }

        @Override
        public FluidStack drain(FluidStack res, FluidAction action) {
            if (res.isEmpty()) {
                return FluidStack.EMPTY;
            }
            int done = 0;
            for (FermentingVatBlockEntity m : members()) {
                if (done < res.getAmount() && FluidStack.isSameFluidSameComponents(m.product.getFluid(), res)) {
                    done += m.product.drain(res.copyWithAmount(res.getAmount() - done), action).getAmount();
                }
            }
            return done == 0 ? FluidStack.EMPTY : res.copyWithAmount(done);
        }

        @Override
        public FluidStack drain(int max, FluidAction action) {
            FluidStack kind = getFluidInTank(1);
            return kind.isEmpty() ? FluidStack.EMPTY : drain(kind.copyWithAmount(Math.min(max, kind.getAmount())), action);
        }
    }

    // ------------------------------------------------------------------------------------------------ info

    public float speed() {
        FermentingVatBlockEntity c = controllerBE();
        return c == null ? 0 : c.speed;
    }

    public float progressFraction() {
        FermentingVatBlockEntity c = controllerBE();
        FermentingRecipe r = c == null ? null : c.recipe(c.active);
        return r == null ? -1 : Math.min(1F, c.progress / r.time());
    }

    @Nullable
    public FermentingRecipe activeRecipe() {
        FermentingVatBlockEntity c = controllerBE();
        return c == null ? null : c.recipe(c.active);
    }

    public int batchCount() {
        FermentingVatBlockEntity c = controllerBE();
        return c == null ? 0 : c.batches;
    }

    public int[] size() {
        FermentingVatBlockEntity c = controllerBE();
        return c == null ? new int[]{1, 1, 1} : new int[]{c.sx, c.sy, c.sz};
    }

    @Nullable
    public Component blocker() {
        FermentingVatBlockEntity c = controllerBE();
        if (c == null) {
            return null;
        }
        if (c.speed <= 0) {
            return Component.translatable("creativeengineering.vat.blocker.no_heat");
        }
        if (c.active == null && fluids.getFluidInTank(0).isEmpty()) {
            return Component.translatable("creativeengineering.vat.blocker.no_fluid");
        }
        if (c.active == null && items.getStackInSlot(0).isEmpty()) {
            return Component.translatable("creativeengineering.vat.blocker.no_items");
        }
        if (c.active == null) {
            return Component.translatable("creativeengineering.vat.blocker.no_recipe");
        }
        return null;
    }

    @Override
    public Component readoutStatus() {
        return MachineReadout.status(blocker(), progressFraction() >= 0);
    }

    @Override
    public Float readoutProgress() {
        float p = progressFraction();
        return p < 0 ? null : p;
    }

    @Override
    public int readoutOutput() {
        return fluids.getFluidInTank(1).getAmount();
    }

    @Override
    public Float readoutTank() {
        return (float) fluids.getFluidInTank(1).getAmount() / fluids.getTankCapacity(1);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        int[] s = size();
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.fermenting_vat")));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.size", s[0], s[2], s[1])).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.vat.heat", String.format("%.2f", speed()))).withStyle(ChatFormatting.GRAY));
        for (int t = 0; t < 2; t++) {
            FluidStack f = fluids.getFluidInTank(t);
            Component name = f.isEmpty() ? Component.translatable("creativeengineering.vat.empty") : f.getHoverName();
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.vat.tank." + t, name,
                    f.getAmount(), fluids.getTankCapacity(t))).withStyle(ChatFormatting.GRAY));
        }
        float p = progressFraction();
        if (p >= 0) {
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.vat.progress",
                    batchCount(), Math.round(p * 100))).withStyle(ChatFormatting.GOLD));
        }
        Component b = blocker();
        if (b != null) {
            tooltip.add(Component.literal("     ").append(b).withStyle(ChatFormatting.RED));
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------ save / sync

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (controller != null) {
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
        }
        tag.putIntArray("Size", new int[]{sx, sy, sz});
        tag.put("Item", slot.serializeNBT(registries));
        tag.put("Input", input.writeToNBT(registries, new CompoundTag()));
        tag.put("Product", product.writeToNBT(registries, new CompoundTag()));
        tag.putFloat("Speed", speed);
        if (active != null) {
            tag.putString("Active", active.toString());
        }
        tag.putInt("Batches", batches);
        tag.putFloat("Progress", progress);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        BlockPos old = controller;
        int oldSize = sx * 100 + sy * 10 + sz;
        controller = tag.contains("Controller") ? NbtUtils.readBlockPos(tag, "Controller").orElse(null) : null;
        int[] s = tag.getIntArray("Size");
        if (s.length == 3) {
            sx = s[0]; sy = s[1]; sz = s[2];
        }
        slot.deserializeNBT(registries, tag.getCompound("Item"));
        input.readFromNBT(registries, tag.getCompound("Input"));
        product.readFromNBT(registries, tag.getCompound("Product"));
        speed = tag.getFloat("Speed");
        active = tag.contains("Active") ? ResourceLocation.parse(tag.getString("Active")) : null;
        batches = tag.getInt("Batches");
        progress = tag.getFloat("Progress");
        if (clientPacket && level != null && oldSize != sx * 100 + sy * 10 + sz) {
            old = null;   // same controller, new size: still a new picture for every block
        }
        if (clientPacket && level != null && !java.util.Objects.equals(old, controller)) {
            // connected textures depend on which vat this block belongs to: re-render
            com.kragnoth.creativeengineering.content.shared.CTRefresh.onClientController(this, old, controller);
        }
    }

    /** Client-safe: this block's copy of its vat's size {w, h, d} (synced to every member). */
    public int[] clientSize() {
        return new int[]{sx, sy, sz};
    }

    /** Client-safe: the formed vat's controller this block belongs to (for connected textures). */
    @Nullable
    public BlockPos controllerPos() {
        return controller;
    }
}
