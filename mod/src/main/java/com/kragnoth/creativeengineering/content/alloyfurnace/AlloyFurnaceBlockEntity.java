package com.kragnoth.creativeengineering.content.alloyfurnace;

import com.kragnoth.creativeengineering.content.shared.Port;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.world.phys.Vec3;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * One Alloy Furnace Brick (docs/steelworks-spec.md 4b). Any SOLID box of bricks, 2 to 4 blocks on each side
 * (2x2x2 up to 4x4x4, e.g. 2x3x4), forms one furnace; anything else stays loose bricks. The min corner is the
 * controller and holds all items; on forming it gathers whatever its members held.
 *
 * <ul>
 *   <li>Heat: Blaze Burners under EVERY column of the bottom layer. The weakest burner sets which recipes run
 *       (heated / superheated); the average sets speed (x1 .. x2).</li>
 *   <li>Fans blowing into the bottom layer are extra speed: +25% each, up to 4. Not required.</li>
 *   <li>No fuel of its own: the Blaze Burners are the running cost, as with Create's Basin.</li>
 *   <li>Bulk: volume / 2 batches at once (4 for 2x2x2, 32 for 4x4x4).</li>
 * </ul>
 */
public class AlloyFurnaceBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation,
        com.kragnoth.creativeengineering.content.shared.MachineReadout {
    public static final int MIN = 2, MAX = 4, MAX_BRICKS = MAX * MAX * MAX;
    /** Slot 9 was the fuel slot up to 0.6.5; it's kept only so old saves line up, and is emptied by hand use. */
    public static final int INPUTS = 9, LEGACY = 9, OUT_START = 10, SLOTS = 19;
    private static final int CHECK_TICKS = 20;

    /** One running batch: the recipe, what it consumed (refunded if the furnace breaks), progress in ticks. */
    static final class Batch {
        final ResourceLocation recipe;
        final List<ItemStack> consumed;
        float progress;

        Batch(ResourceLocation recipe, List<ItemStack> consumed, float progress) {
            this.recipe = recipe;
            this.consumed = consumed;
            this.progress = progress;
        }
    }

    @Nullable protected BlockPos controller; // null = loose brick
    protected int sx = 1, sy = 1, sz = 1;
    protected boolean recheck = true;
    protected FilteringBehaviour filter;

    protected final ItemStackHandler inv = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    protected final List<Batch> batches = new ArrayList<>();
    protected float heatAvg;
    protected int heatMin;
    protected int fans;
    protected int checkTimer;

    private final AlloyFurnaceAutomation automation = new AlloyFurnaceAutomation(this);
    private final IItemHandler outputs = new RangedWrapper(inv, OUT_START, SLOTS);

    public AlloyFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).setInsertionHandler(
                (t, side, simulate) -> ItemHandlerHelper.insertItem(automationItems(), t.stack, simulate)));
        // Recipe filter, like a Basin's: shown on output-hatch bricks only (wrench a brick to the output look).
        filter = new FilteringBehaviour(this, new HatchFilterSlot())
                .onlyActiveWhen(() -> getBlockState().getValue(Port.PROPERTY) == Port.OUTPUT)
                .withCallback(f -> {
                    AlloyFurnaceBlockEntity c = getControllerBE();
                    if (c != null) {
                        c.checkTimer = CHECK_TICKS; // pick the new filter up on the next tick
                    }
                });
        behaviours.add(filter);
    }

    /** The filter slot sits over the output hatch on each side face. */
    static final class HatchFilterSlot extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16.25); // just proud of the hatch, so the filter item reads
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }

    /** Filters set on this furnace's output hatches; empty = run anything. Refreshed with heat (controller only). */
    private final List<FilteringBehaviour> recipeFilters = new ArrayList<>();

    private boolean allowed(AlloyRecipes.Job job) {
        if (recipeFilters.isEmpty()) {
            return true;
        }
        for (FilteringBehaviour f : recipeFilters) {
            if (f.test(job.result())) {
                return true;
            }
        }
        return false;
    }

    public int filterCount() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        return c == null ? 0 : c.recipeFilters.size();
    }

    // ------------------------------------------------------------------------------------------------ forming

    public void requestRecheck() {
        recheck = true;
    }

    public boolean isFormed() {
        return controller != null;
    }

    public boolean isController() {
        return worldPosition.equals(controller);
    }

    @Nullable
    public BlockPos controllerPos() {
        return controller;
    }

    @Nullable
    public AlloyFurnaceBlockEntity getControllerBE() {
        if (controller == null || level == null) {
            return null;
        }
        if (isController()) {
            return this;
        }
        return level.getBlockEntity(controller) instanceof AlloyFurnaceBlockEntity c && c.isController() ? c : null;
    }

    public int[] size() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        return c == null ? new int[]{1, 1, 1} : new int[]{c.sx, c.sy, c.sz};
    }

    /** Flood-fills the connected bricks; forms them if they make a solid 2..4 box, else leaves them all loose. */
    private void checkStructure() {
        Block brick = ModBlocks.ALLOY_FURNACE_BRICKS.get();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(worldPosition);
        seen.add(worldPosition);
        boolean tooBig = false;
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (seen.contains(n)) {
                    continue;
                }
                if (!isVirtual() && !level.isLoaded(n)) {
                    return; // try again once the neighbour chunk is in
                }
                if (level.getBlockState(n).is(brick)) {
                    seen.add(n);
                    if (seen.size() > MAX_BRICKS) {
                        tooBig = true;
                        break;
                    }
                    queue.add(n);
                }
            }
            if (tooBig) {
                break;
            }
        }
        List<AlloyFurnaceBlockEntity> members = new ArrayList<>();
        int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, z0 = Integer.MAX_VALUE;
        int x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE, z1 = Integer.MIN_VALUE;
        for (BlockPos p : seen) {
            if (level.getBlockEntity(p) instanceof AlloyFurnaceBlockEntity be) {
                members.add(be);
                be.recheck = false;
            }
            x0 = Math.min(x0, p.getX()); y0 = Math.min(y0, p.getY()); z0 = Math.min(z0, p.getZ());
            x1 = Math.max(x1, p.getX()); y1 = Math.max(y1, p.getY()); z1 = Math.max(z1, p.getZ());
        }
        int dx = x1 - x0 + 1, dy = y1 - y0 + 1, dz = z1 - z0 + 1;
        boolean valid = !tooBig && members.size() == seen.size() && dx * dy * dz == seen.size()
                && in(dx) && in(dy) && in(dz);
        if (!valid) {
            members.forEach(m -> m.setController(null, 1, 1, 1));
            return;
        }
        BlockPos min = new BlockPos(x0, y0, z0);
        AlloyFurnaceBlockEntity c = (AlloyFurnaceBlockEntity) level.getBlockEntity(min);
        boolean newlyFormed = !min.equals(c.controller) || c.sx != dx || c.sy != dy || c.sz != dz;
        for (AlloyFurnaceBlockEntity m : members) {
            m.setController(min, dx, dy, dz);
            if (m != c) {
                c.absorb(m);
            }
        }
        if (newlyFormed) {
            level.playSound(null, min, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.35F, 0.6F);
            c.checkTimer = CHECK_TICKS;
        }
    }

    private static boolean in(int d) {
        return d >= MIN && d <= MAX;
    }

    private void setController(@Nullable BlockPos c, int x, int y, int z) {
        boolean changed = c == null ? controller != null : !c.equals(controller);
        controller = c;
        sx = x; sy = y; sz = z;
        if (c == null) {
            setState(AlloyFurnaceBlock.LIT, false);
            setState(AlloyFurnaceBlock.WORKING, false);
        }
        if (changed) {
            invalidateCapabilities();
        }
        setChanged();
        sendData();
    }

    /** Pull a member's leftovers (from an earlier shape) into this controller; what doesn't fit stays put. */
    private void absorb(AlloyFurnaceBlockEntity m) {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack s = m.inv.getStackInSlot(i);
            if (!s.isEmpty()) {
                m.inv.setStackInSlot(i, ItemHandlerHelper.insertItem(inv, s, false));
            }
        }
        batches.addAll(m.batches);
        m.batches.clear();
    }

    // ------------------------------------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (recheck && (!level.isClientSide || isVirtual())) { // Ponder's virtual world forms too
            recheck = false;
            checkStructure();
        }
        if (level.isClientSide) {
            return;
        }
        if (!isController()) {
            return;
        }
        if (++checkTimer >= CHECK_TICKS) {
            checkTimer = 0;
            refreshHeatAndAir();
        }
        if (heatMin >= 1) {
            work();
        }
    }

    protected void refreshHeatAndAir() {
        float total = 0;
        int min = 2;
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                BlockPos below = worldPosition.offset(x, -1, z);
                float h = BoilerHeater.findHeat(level, below, level.getBlockState(below));
                int lvl = h >= 2 ? 2 : h >= 1 ? 1 : 0;
                total += lvl;
                min = Math.min(min, lvl);
            }
        }
        heatAvg = total / (sx * sz);
        heatMin = min;
        int n = 0;
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                BlockPos p = worldPosition.offset(x, 0, z);
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockEntity(p.relative(d)) instanceof EncasedFanBlockEntity fan
                            && fan.getAirFlowDirection() == d.getOpposite()) {
                        n++;
                    }
                }
            }
        }
        fans = n;
        recipeFilters.clear();
        forEachMember(m -> {
            if (m.filter != null && m.filter.isActive() && !m.filter.getFilter().isEmpty()) {
                recipeFilters.add(m.filter);
            }
        });
        boolean lit = heatMin >= 1, busy = lit && !batches.isEmpty();
        forEachMember(m -> {
            m.setState(AlloyFurnaceBlock.LIT, lit);
            m.setState(AlloyFurnaceBlock.WORKING, busy);
        });
        sendData();
    }

    public float speed() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        if (c == null || c.heatMin < 1) {
            return 0;
        }
        return c.heatAvg * (1F + 0.25F * Math.min(c.fans, 4));
    }

    public int capacity() {
        int[] s = size();
        return isFormed() ? Math.max(1, s[0] * s[1] * s[2] / 2) : 0;
    }

    private void work() {
        List<AlloyRecipes.Job> jobs = AlloyRecipes.all(level);
        int cap = capacity();
        while (batches.size() < cap && startBatch(jobs)) {
            // keep starting while there are ingredients and room
        }
        float speed = speed();
        for (int i = 0; i < batches.size(); i++) {
            Batch b = batches.get(i);
            AlloyRecipes.Job job = find(jobs, b.recipe);
            if (job == null) { // recipe gone after a reload: give the ingredients back as output
                b.consumed.forEach(s -> ItemHandlerHelper.insertItem(outputs, s, false));
                batches.remove(i--);
                continue;
            }
            b.progress = Math.min(b.progress + speed, job.time());
            if (b.progress >= job.time() && ItemHandlerHelper.insertItem(outputs, job.result().copy(), true).isEmpty()) {
                ItemHandlerHelper.insertItem(outputs, job.result().copy(), false);
                batches.remove(i--);
            }
        }
        setChanged();
    }

    @Nullable
    private static AlloyRecipes.Job find(List<AlloyRecipes.Job> jobs, ResourceLocation id) {
        for (AlloyRecipes.Job j : jobs) {
            if (j.id().equals(id)) {
                return j;
            }
        }
        return null;
    }

    private boolean startBatch(List<AlloyRecipes.Job> jobs) {
        for (AlloyRecipes.Job job : jobs) {
            if (job.heat() > heatMin || !allowed(job) || leftoverAfter(job) == null) {
                continue;
            }
            List<ItemStack> consumed = new ArrayList<>();
            take(job, consumed);
            batches.add(new Batch(job.id(), consumed, 0));
            return true;
        }
        return false;
    }

    /** What each input slot would hold after taking this job's ingredients, or null if they aren't all there. */
    @Nullable
    private int[] leftoverAfter(AlloyRecipes.Job job) {
        int[] left = new int[INPUTS];
        for (int i = 0; i < INPUTS; i++) {
            left[i] = inv.getStackInSlot(i).getCount();
        }
        for (SizedIngredient need : job.inputs()) {
            int n = need.count();
            for (int i = 0; i < INPUTS && n > 0; i++) {
                if (left[i] > 0 && need.ingredient().test(inv.getStackInSlot(i))) {
                    int t = Math.min(n, left[i]);
                    left[i] -= t;
                    n -= t;
                }
            }
            if (n > 0) {
                return null;
            }
        }
        return left;
    }

    private void take(AlloyRecipes.Job job, List<ItemStack> consumed) {
        for (SizedIngredient need : job.inputs()) {
            int n = need.count();
            for (int i = 0; i < INPUTS && n > 0; i++) {
                ItemStack s = inv.getStackInSlot(i);
                if (!s.isEmpty() && need.ingredient().test(s)) {
                    int t = Math.min(n, s.getCount());
                    consumed.add(s.copyWithCount(t));
                    inv.extractItem(i, t, false);
                    n -= t;
                }
            }
        }
    }


    // ------------------------------------------------------------------------------------------------ views & info

    private void forEachMember(java.util.function.Consumer<AlloyFurnaceBlockEntity> action) {
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    if (level.getBlockEntity(worldPosition.offset(x, y, z)) instanceof AlloyFurnaceBlockEntity m) {
                        action.accept(m);
                    }
                }
            }
        }
    }

    private void setState(BooleanProperty prop, boolean value) {
        BlockState st = getBlockState();
        if (level != null && st.hasProperty(prop) && st.getValue(prop) != value) {
            level.setBlock(worldPosition, st.setValue(prop, value), Block.UPDATE_CLIENTS);
        }
    }

    ItemStackHandler items() {
        return inv;
    }

    /** What automation and the side-less query see: the whole furnace (19 slots), or nothing while loose. */
    public IItemHandler automationItems() {
        return isFormed() ? automation : EmptyItemHandler.INSTANCE;
    }

    /** Everything this brick itself holds, including batch ingredients (dropped when it's broken). */
    public List<ItemStack> contentsToDrop() {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            out.add(inv.getStackInSlot(i).copy());
        }
        batches.forEach(b -> b.consumed.forEach(s -> out.add(s.copy())));
        return out;
    }

    /** Empty-hand use (Millstone-style): outputs first, then inputs (then any pre-0.6.6 fuel left behind). */
    public List<ItemStack> takeForHand() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        List<ItemStack> out = new ArrayList<>();
        if (c == null) {
            return out;
        }
        for (int[] range : new int[][]{{OUT_START, SLOTS}, {0, INPUTS}, {LEGACY, LEGACY + 1}}) {
            for (int i = range[0]; i < range[1]; i++) {
                ItemStack s = c.inv.getStackInSlot(i);
                if (!s.isEmpty()) {
                    out.add(s.copy());
                    c.inv.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            if (!out.isEmpty()) {
                break;
            }
        }
        return out;
    }

    public int[] batchUse() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        return c == null ? new int[]{0, 0} : new int[]{c.batches.size(), c.capacity()};
    }

    public float averageProgress() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        if (c == null || c.batches.isEmpty() || level == null) {
            return 0;
        }
        List<AlloyRecipes.Job> jobs = AlloyRecipes.all(level);
        float sum = 0;
        for (Batch b : c.batches) {
            AlloyRecipes.Job j = find(jobs, b.recipe);
            sum += j == null ? 0 : Math.min(1F, b.progress / j.time());
        }
        return sum / c.batches.size();
    }

    public int heatMin() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        return c == null ? 0 : c.heatMin;
    }

    public int fans() {
        AlloyFurnaceBlockEntity c = getControllerBE();
        return c == null ? 0 : c.fans;
    }

    /** Why this furnace can't work now, or null (goggles, Jade, Tarbound T7). */
    @Nullable
    public Component automationBlocker(List<ItemStack> expectedOutputs) {
        AlloyFurnaceBlockEntity c = getControllerBE();
        if (c == null) {
            return Component.translatable("creativeengineering.alloy_furnace.blocker.loose");
        }
        if (c.heatMin < 1) {
            return Component.translatable("creativeengineering.alloy_furnace.blocker.no_heat");
        }
        for (int i = OUT_START; i < SLOTS; i++) {
            if (c.inv.getStackInSlot(i).isEmpty()) {
                return null;
            }
        }
        return Component.translatable("creativeengineering.coke_oven.blocker.output_full");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        int[] s = size(), use = batchUse();
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.alloy_furnace_bricks")));
        if (isFormed()) {
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.coke_oven.size", s[0], s[2], s[1])).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.alloy_furnace.heat." + heatMin())).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.alloy_furnace.fans", fans())).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.blast_furnace.speed", String.format("%.2f", speed()))).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.alloy_furnace.batches", use[0], use[1])).withStyle(ChatFormatting.GRAY));
            if (filterCount() > 0) {
                tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.alloy_furnace.filtered", filterCount())).withStyle(ChatFormatting.GOLD));
            }
        }
        Component b = automationBlocker(List.of());
        if (b != null) {
            tooltip.add(Component.literal("     ").append(b).withStyle(ChatFormatting.RED));
        }
        return true;
    }

    @Override
    public Component readoutStatus() {
        return com.kragnoth.creativeengineering.content.shared.MachineReadout.status(automationBlocker(List.of()), batchUse()[0] > 0);
    }

    @Override
    public Float readoutProgress() {
        return batchUse()[0] > 0 ? averageProgress() : null;
    }

    @Override
    public int readoutOutput() {
        int n = 0;
        IItemHandler view = automationItems();
        for (int i = OUT_START; i < view.getSlots(); i++) {
            n += view.getStackInSlot(i).getCount();
        }
        return n;
    }

    // ------------------------------------------------------------------------------------------------ save / sync

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (controller != null) {
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
            tag.putIntArray("Size", new int[]{sx, sy, sz});
        }
        tag.putFloat("HeatAvg", heatAvg);
        tag.putInt("HeatMin", heatMin);
        tag.putInt("Fans", fans);
        tag.putInt("Batches", batches.size());
        if (!clientPacket) {
            tag.put("Items", inv.serializeNBT(registries));
            ListTag list = new ListTag();
            for (Batch b : batches) {
                CompoundTag t = new CompoundTag();
                t.putString("Recipe", b.recipe.toString());
                t.putFloat("Progress", b.progress);
                ListTag used = new ListTag();
                b.consumed.forEach(s -> used.add(s.save(registries)));
                t.put("Consumed", used);
                list.add(t);
            }
            tag.put("Work", list);
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        BlockPos old = controller;
        controller = tag.contains("Controller") ? NbtUtils.readBlockPos(tag, "Controller").orElse(null) : null;
        int[] s = tag.getIntArray("Size");
        if (s.length == 3) {
            sx = s[0]; sy = s[1]; sz = s[2];
        }
        heatAvg = tag.getFloat("HeatAvg");
        heatMin = tag.getInt("HeatMin");
        fans = tag.getInt("Fans");
        if (!clientPacket) {
            inv.deserializeNBT(registries, tag.getCompound("Items"));
            batches.clear();
            for (Tag t0 : tag.getList("Work", Tag.TAG_COMPOUND)) {
                CompoundTag t = (CompoundTag) t0;
                List<ItemStack> used = new ArrayList<>();
                for (Tag u : t.getList("Consumed", Tag.TAG_COMPOUND)) {
                    ItemStack.parse(registries, u).ifPresent(used::add);
                }
                batches.add(new Batch(ResourceLocation.parse(t.getString("Recipe")), used, t.getFloat("Progress")));
            }
        } else if (level != null && !java.util.Objects.equals(old, controller)) {
            // connected textures depend on which furnace this brick belongs to: re-render
            com.kragnoth.creativeengineering.content.shared.CTRefresh.onClientController(this, old, controller);
        }
    }
}
