package com.kragnoth.creativeengineering.content.distilling;

import com.kragnoth.creativeengineering.content.shared.CTRefresh;
import com.kragnoth.creativeengineering.content.shared.FormedTank;
import com.kragnoth.creativeengineering.content.shared.MachineReadout;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * One block of a Distilling Tank (docs/distilling-spec.md): any box 1..3 wide and 2..8 tall over heat forms a
 * fractionating column (own flood-fill forming, like the vat; min corner = controller).
 * <ul>
 *   <li>Bottom layer = boiler: its blocks' tanks take the input fluid (fill only, recipe inputs only).</li>
 *   <li>Layer n above it collects fraction n - 1 of the running recipe (drain only), heaviest low, lightest high.</li>
 *   <li>Every block keeps its own 1000 mB tank, so nothing is lost when the column is reshaped; automation sees one
 *       tank per layer from any side of that layer. Residue items (e.g. bitumen) wait in the controller, extracted
 *       from the boiler layer.</li>
 *   <li>Heat as the vat (averaged over the footprint: passive x1, lit x2, superheated x3); a recipe also needs its own
 *       minimum heat. Batches run as many times at once as the column is wide (footprint).</li>
 * </ul>
 */
public class DistillingTankBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, MachineReadout, FormedTank {
    public static final int MAX_WIDTH = 3, MIN_HEIGHT = 2, MAX_HEIGHT = 8, TANK_PER_BLOCK = 1000;
    private static final int CHECK_TICKS = 20, PUSH_TICKS = 5, PUSH_MB = 100;

    @Nullable protected BlockPos controller;
    protected int sx = 1, sy = 1, sz = 1;
    protected boolean recheck = true;

    protected final FluidTank tank = new FluidTank(TANK_PER_BLOCK) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            sendData();
        }
    };

    // controller only
    protected final ItemStackHandler residue = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int s) {
            setChanged();
        }
    };
    @Nullable protected ResourceLocation active;
    protected int batches, heatLevel;
    protected float progress, speed;
    protected int checkTimer;

    public DistillingTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    // ------------------------------------------------------------------------------------------------ forming

    public void requestRecheck() {
        recheck = true;
    }

    public boolean isController() {
        return worldPosition.equals(controller);
    }

    @Override
    @Nullable
    public BlockPos controllerPos() {
        return controller;
    }

    @Nullable
    public DistillingTankBlockEntity controllerBE() {
        if (controller == null || level == null) {
            return null;
        }
        if (isController()) {
            return this;
        }
        return level.getBlockEntity(controller) instanceof DistillingTankBlockEntity c && c.isController() ? c : null;
    }

    /** Is this a working column (a box at least MIN_HEIGHT tall)? Single blocks / short boxes just hold fluid. */
    public boolean formed() {
        DistillingTankBlockEntity c = controllerBE();
        return c != null && c.sy >= MIN_HEIGHT;
    }

    /** This block's layer: 0 = boiler. */
    public int layer() {
        return controller == null ? 0 : worldPosition.getY() - controller.getY();
    }

    public List<DistillingTankBlockEntity> layerMembers(int y) {
        List<DistillingTankBlockEntity> out = new ArrayList<>();
        DistillingTankBlockEntity c = controllerBE();
        if (c == null) {
            if (y == 0) {
                out.add(this);
            }
            return out;
        }
        for (int x = 0; x < c.sx; x++) {
            for (int z = 0; z < c.sz; z++) {
                if (level.getBlockEntity(c.worldPosition.offset(x, y, z)) instanceof DistillingTankBlockEntity m) {
                    out.add(m);
                }
            }
        }
        return out;
    }

    private void checkStructure() {
        Block me = ModBlocks.DISTILLING_TANK.get();
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
                if (level.getBlockState(n).is(me)) {
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
        List<DistillingTankBlockEntity> found = new ArrayList<>();
        for (BlockPos p : seen) {
            if (level.getBlockEntity(p) instanceof DistillingTankBlockEntity be) {
                found.add(be);
                be.recheck = false;
            }
            x0 = Math.min(x0, p.getX()); y0 = Math.min(y0, p.getY()); z0 = Math.min(z0, p.getZ());
            x1 = Math.max(x1, p.getX()); y1 = Math.max(y1, p.getY()); z1 = Math.max(z1, p.getZ());
        }
        int dx = x1 - x0 + 1, dy = y1 - y0 + 1, dz = z1 - z0 + 1;
        boolean valid = !tooBig && found.size() == seen.size() && dx * dy * dz == seen.size()
                && dx <= MAX_WIDTH && dz <= MAX_WIDTH && dy <= MAX_HEIGHT;
        if (!valid) {
            found.forEach(m -> m.setController(m.worldPosition, 1, 1, 1));
            return;
        }
        BlockPos min = new BlockPos(x0, y0, z0);
        for (DistillingTankBlockEntity m : found) {
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
            boolean top = rel.getY() == y - 1;
            boolean valve = top && rel.getX() == x / 2 && rel.getZ() == z / 2;
            boolean formed = y >= MIN_HEIGHT;
            BlockState st = getBlockState().setValue(DistillingTankBlock.VALVE, valve && formed);
            if (st != getBlockState()) {
                level.setBlock(worldPosition, st, Block.UPDATE_CLIENTS);
            }
            if (!isController()) {
                active = null;
                batches = 0;
                progress = 0;
            }
            refreshOutlets();
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
        if (checkTimer % PUSH_TICKS == 0 && sy >= MIN_HEIGHT) {
            pushOutputs();
        }
        if (speed <= 0 || sy < MIN_HEIGHT) {
            return;
        }
        DistillingRecipe r = recipe(active);
        if (r == null) {
            active = null;
            startBatch();
            return;
        }
        if (heatLevel < r.heat()) {
            return;   // too cool for this one: wait
        }
        progress += speed;
        if (progress >= r.time()) {
            if (outputsFit(r, batches)) {
                for (int i = 0; i < r.fractions().size(); i++) {
                    FluidStack f = r.fractions().get(i);
                    fillLayer(i + 1, f.copyWithAmount(f.getAmount() * batches), IFluidHandler.FluidAction.EXECUTE);
                }
                if (r.residue().isPresent()) {
                    for (int b = 0; b < batches; b++) {
                        if (level.random.nextFloat() < r.chance()) {
                            residue.insertItem(0, r.residue().get().copy(), false);
                        }
                    }
                }
                active = null;
                batches = 0;
                progress = 0;
                setWorking(false);
            }
        }
        setChanged();
    }

    /**
     * Outlets without pumps (Kragnoth, 2026-11-22: "each layer of distilling easily pumped and put into storage"): every
     * fraction level pushes its fluid into any container touching its outside faces (a Create fluid tank, another
     * column's boiler...). Pumps and pipes work on any side too. The boiler never pushes.
     */
    private void pushOutputs() {
        // a level pushes as one tank: whichever of its blocks touches the container, the fluid can come from any of them
        for (int y = 1; y < sy; y++) {
            for (DistillingTankBlockEntity m : layerMembers(y)) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    FluidStack have = layerFluid(y);
                    if (have.isEmpty()) {
                        break;
                    }
                    BlockPos n = m.worldPosition.relative(d);
                    if (level.getBlockEntity(n) instanceof DistillingTankBlockEntity o && o.controller != null && o.controller.equals(controller)) {
                        continue;
                    }
                    IFluidHandler h = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, n, d.getOpposite());
                    if (h == null) {
                        continue;
                    }
                    int took = h.fill(have.copyWithAmount(Math.min(PUSH_MB, have.getAmount())), IFluidHandler.FluidAction.EXECUTE);
                    if (took > 0) {
                        drainLayer(y, have.copyWithAmount(took));
                    }
                }
            }
        }
    }

    private void refreshHeat() {
        int total = 0, min = 3;
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                BlockPos below = worldPosition.offset(x, -1, z);
                float h = BoilerHeater.findHeat(level, below, level.getBlockState(below));
                int lvl = h < 0 ? 0 : h >= 2 ? 3 : h >= 1 ? 2 : 1;
                total += lvl;
                min = Math.min(min, lvl);
            }
        }
        speed = (float) total / (sx * sz);
        heatLevel = min;   // a recipe's minimum heat must be met under the whole footprint
        sendData();
    }

    @Nullable
    private DistillingRecipe recipe(@Nullable ResourceLocation id) {
        if (id == null || level == null) {
            return null;
        }
        return level.getRecipeManager().byKey(id).map(h -> h.value() instanceof DistillingRecipe d ? d : null).orElse(null);
    }

    private boolean outputsFit(DistillingRecipe r, int n) {
        for (int i = 0; i < r.fractions().size(); i++) {
            FluidStack f = r.fractions().get(i);
            if (fillLayer(i + 1, f.copyWithAmount(f.getAmount() * n), IFluidHandler.FluidAction.SIMULATE) < f.getAmount() * n) {
                return false;
            }
        }
        return true;
    }

    private void startBatch() {
        FluidStack in = layerFluid(0);
        if (in.isEmpty()) {
            return;
        }
        for (RecipeHolder<DistillingRecipe> h : level.getRecipeManager().getAllRecipesFor(ModRecipes.DISTILLING_TYPE.get())) {
            DistillingRecipe r = h.value();
            if (!r.input().ingredient().test(in) || sy < r.minHeight() || heatLevel < r.heat()) {
                continue;
            }
            int n = Math.min(sx * sz, in.getAmount() / r.input().amount());
            while (n > 0 && !outputsFit(r, n)) {
                n--;
            }
            if (n <= 0) {
                continue;
            }
            drainLayer(0, in.copyWithAmount(r.input().amount() * n));
            active = h.id();
            batches = n;
            progress = 0;
            setWorking(true);
            return;
        }
    }

    private void setWorking(boolean on) {
        for (int y = 0; y < sy; y++) {
            for (DistillingTankBlockEntity m : layerMembers(y)) {
                BlockState s = m.getBlockState();
                if (s.getValue(DistillingTankBlock.WORKING) != on) {
                    level.setBlock(m.worldPosition, s.setValue(DistillingTankBlock.WORKING, on), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ layer storage

    public FluidStack layerFluid(int y) {
        FluidStack kind = FluidStack.EMPTY;
        int n = 0;
        for (DistillingTankBlockEntity m : layerMembers(y)) {
            FluidStack f = m.tank.getFluid();
            if (!f.isEmpty() && (kind.isEmpty() || FluidStack.isSameFluidSameComponents(kind, f))) {
                kind = f;
                n += f.getAmount();
            }
        }
        return kind.isEmpty() ? FluidStack.EMPTY : kind.copyWithAmount(n);
    }

    public int layerCapacity(int y) {
        return TANK_PER_BLOCK * layerMembers(y).size();
    }

    private int fillLayer(int y, FluidStack stack, IFluidHandler.FluidAction action) {
        int done = 0;
        for (DistillingTankBlockEntity m : layerMembers(y)) {
            if (done < stack.getAmount()) {
                done += m.tank.fill(stack.copyWithAmount(stack.getAmount() - done), action);
            }
        }
        return done;
    }

    private int drainLayer(int y, FluidStack want) {
        int left = want.getAmount();
        for (DistillingTankBlockEntity m : layerMembers(y)) {
            if (left > 0) {
                left -= m.tank.drain(want.copyWithAmount(left), IFluidHandler.FluidAction.EXECUTE).getAmount();
            }
        }
        return want.getAmount() - left;
    }

    private boolean isRecipeInput(FluidStack f) {
        return level != null && level.getRecipeManager().getAllRecipesFor(ModRecipes.DISTILLING_TYPE.get()).stream()
                .anyMatch(h -> h.value().input().ingredient().test(f));
    }

    /** What automation and buckets see on this block: its layer's tank (boiler: fill only; fractions: drain only). */
    public IFluidHandler automationFluids() {
        return new LayerHandler(layer());
    }

    /** Residue items, taken from the boiler layer. */
    @Nullable
    public IItemHandler automationItems() {
        DistillingTankBlockEntity c = controllerBE();
        return layer() == 0 && c != null ? new ResidueOut(c.residue) : null;
    }

    public List<ItemStack> contentsToDrop() {
        return isController() && !residue.getStackInSlot(0).isEmpty() ? List.of(residue.getStackInSlot(0).copy()) : List.of();
    }

    private final class LayerHandler implements IFluidHandler {
        private final int y;

        LayerHandler(int y) {
            this.y = y;
        }

        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int t) { return layerFluid(y); }
        @Override public int getTankCapacity(int t) { return layerCapacity(y); }
        @Override public boolean isFluidValid(int t, FluidStack s) { return y == 0 && isRecipeInput(s); }

        @Override
        public int fill(FluidStack res, FluidAction action) {
            if (y != 0 || res.isEmpty() || !isRecipeInput(res)) {
                return 0;
            }
            FluidStack in = layerFluid(0);
            if (!in.isEmpty() && !FluidStack.isSameFluidSameComponents(in, res)) {
                return 0;   // one input fluid at a time
            }
            return fillLayer(0, res, action);
        }

        @Override
        public FluidStack drain(FluidStack res, FluidAction action) {
            if (y == 0 || res.isEmpty()) {
                return FluidStack.EMPTY;
            }
            int done = 0;
            for (DistillingTankBlockEntity m : layerMembers(y)) {
                if (done < res.getAmount() && FluidStack.isSameFluidSameComponents(m.tank.getFluid(), res)) {
                    done += m.tank.drain(res.copyWithAmount(res.getAmount() - done), action).getAmount();
                }
            }
            return done == 0 ? FluidStack.EMPTY : res.copyWithAmount(done);
        }

        @Override
        public FluidStack drain(int max, FluidAction action) {
            FluidStack kind = layerFluid(y);
            return kind.isEmpty() ? FluidStack.EMPTY : drain(kind.copyWithAmount(Math.min(max, kind.getAmount())), action);
        }
    }

    private record ResidueOut(ItemStackHandler h) implements IItemHandler {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int s) { return h.getStackInSlot(0); }
        @Override public ItemStack insertItem(int s, ItemStack stack, boolean sim) { return stack; }
        @Override public ItemStack extractItem(int s, int amount, boolean sim) { return h.extractItem(0, amount, sim); }
        @Override public int getSlotLimit(int s) { return 64; }
        @Override public boolean isItemValid(int s, ItemStack stack) { return false; }
    }

    // ------------------------------------------------------------------------------------------------ info

    public int[] size() {
        DistillingTankBlockEntity c = controllerBE();
        return c == null ? new int[]{1, 1, 1} : new int[]{c.sx, c.sy, c.sz};
    }

    public float speed() {
        DistillingTankBlockEntity c = controllerBE();
        return c == null ? 0 : c.speed;
    }

    @Nullable
    public DistillingRecipe activeRecipe() {
        DistillingTankBlockEntity c = controllerBE();
        return c == null ? null : c.recipe(c.active);
    }

    public float progressFraction() {
        DistillingTankBlockEntity c = controllerBE();
        DistillingRecipe r = c == null ? null : c.recipe(c.active);
        return r == null ? -1 : Math.min(1F, c.progress / r.time());
    }

    public int batchCount() {
        DistillingTankBlockEntity c = controllerBE();
        return c == null ? 0 : c.batches;
    }

    @Nullable
    public Component blocker() {
        DistillingTankBlockEntity c = controllerBE();
        if (c == null) {
            return null;
        }
        if (c.sy < MIN_HEIGHT) {
            return Component.translatable("creativeengineering.still.blocker.too_short");
        }
        if (c.speed <= 0) {
            return Component.translatable("creativeengineering.vat.blocker.no_heat");
        }
        DistillingRecipe r = c.recipe(c.active);
        if (r != null && c.heatLevel < r.heat()) {
            return Component.translatable("creativeengineering.still.blocker.cool", r.heat());
        }
        if (r == null && c.layerFluid(0).isEmpty()) {
            return Component.translatable("creativeengineering.still.blocker.empty");
        }
        if (r == null) {
            for (RecipeHolder<DistillingRecipe> h : level.getRecipeManager().getAllRecipesFor(ModRecipes.DISTILLING_TYPE.get())) {
                if (h.value().input().ingredient().test(c.layerFluid(0))) {
                    if (c.sy < h.value().minHeight()) {
                        return Component.translatable("creativeengineering.still.blocker.height", h.value().minHeight());
                    }
                    if (c.heatLevel < h.value().heat()) {
                        return Component.translatable("creativeengineering.still.blocker.cool", h.value().heat());
                    }
                    return Component.translatable("creativeengineering.still.blocker.full");
                }
            }
        }
        return r == null ? Component.translatable("creativeengineering.still.blocker.full") : null;
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
        int n = 0;
        for (int y = 1; y < size()[1]; y++) {
            n += layerFluid(y).getAmount();
        }
        return n;
    }

    @Override
    public Float readoutTank() {
        int[] s = size();
        return s[1] < 2 ? 0F : (float) readoutOutput() / (TANK_PER_BLOCK * s[0] * s[2] * (s[1] - 1));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        int[] s = size();
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.distilling_tank")));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.size", s[0], s[2], s[1])).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.vat.heat", String.format("%.2f", speed()))).withStyle(ChatFormatting.GRAY));
        for (int y = s[1] - 1; y >= 0; y--) {
            FluidStack f = layerFluid(y);
            Component name = f.isEmpty() ? Component.translatable("creativeengineering.vat.empty") : f.getHoverName();
            tooltip.add(Component.literal("     ").append(Component.translatable(y == 0 ? "creativeengineering.still.boiler" : "creativeengineering.still.layer",
                    y, name, f.getAmount(), layerCapacity(y))).withStyle(y == layer() ? ChatFormatting.WHITE : ChatFormatting.GRAY));
        }
        float p = progressFraction();
        if (p >= 0) {
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.still.progress", batchCount(), Math.round(p * 100)))
                    .withStyle(ChatFormatting.GOLD));
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
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.put("Residue", residue.serializeNBT(registries));
        tag.putFloat("Speed", speed);
        tag.putInt("Heat", heatLevel);
        if (active != null) {
            tag.putString("Active", active.toString());
        }
        tag.putInt("Batches", batches);
        tag.putFloat("Progress", progress);
        tag.putByte("Outlets", (byte) outlets);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        BlockPos old = controller;
        int oldSize = sx * 100 + sy * 10 + sz, oldOutlets = outlets;
        outlets = tag.getByte("Outlets");
        controller = tag.contains("Controller") ? NbtUtils.readBlockPos(tag, "Controller").orElse(null) : null;
        int[] s = tag.getIntArray("Size");
        if (s.length == 3) {
            sx = s[0]; sy = s[1]; sz = s[2];
        }
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        residue.deserializeNBT(registries, tag.getCompound("Residue"));
        speed = tag.getFloat("Speed");
        heatLevel = tag.getInt("Heat");
        active = tag.contains("Active") ? ResourceLocation.parse(tag.getString("Active")) : null;
        batches = tag.getInt("Batches");
        progress = tag.getFloat("Progress");
        if (clientPacket && level != null) {
            boolean reshaped = oldSize != sx * 100 + sy * 10 + sz || !Objects.equals(old, controller);
            CTRefresh.onClientController(this, reshaped ? null : old, controller);
            if (reshaped || oldOutlets != outlets) {
                greebles = null;
                if (level.isClientSide) {
                    dev.engine_room.flywheel.lib.visualization.VisualizationHelper.queueUpdate(this);   // rebuild the dressing
                }
            }
        }
    }

    @Nullable private List<Plumbing.Part> greebles;
    /** Faces (N, E, S, W bits) of this fraction-level block that touch a container: drawn with a bolted coupling. */
    protected int outlets;

    /** Server: which outside faces of a fraction level touch a fluid container (another of this column doesn't count). */
    public void refreshOutlets() {
        if (level == null || level.isClientSide) {
            return;
        }
        int bits = 0;
        if (controller != null && layer() >= 1 && sy >= MIN_HEIGHT) {
            Direction[] order = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            for (int i = 0; i < 4; i++) {
                BlockPos n = worldPosition.relative(order[i]);
                if (level.getBlockEntity(n) instanceof DistillingTankBlockEntity o && Objects.equals(o.controller, controller)) {
                    continue;
                }
                if (level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, n, order[i].getOpposite()) != null) {
                    bits |= 1 << i;
                }
            }
        }
        if (bits != outlets) {
            outlets = bits;
            sendData();
        }
    }

    /** Client: the pipes, collars, gauge and valve to draw on this block (Plumbing), cached until the column reshapes. */
    public List<Plumbing.Part> greebles() {
        if (greebles == null) {
            greebles = controller == null ? List.of() : Plumbing.parts(controller, sx, sy, sz, worldPosition.subtract(controller), outlets);
        }
        return greebles;
    }
}
