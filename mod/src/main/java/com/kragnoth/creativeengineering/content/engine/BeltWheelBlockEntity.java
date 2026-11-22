package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.registry.ModItems;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * A wheel that can carry a Drive Chain (Kragnoth, 2026-11-22): the flywheel (big) or a Drive Sprocket (small). Any two
 * wheels can be chained (0.11.3: "anywhere"), as long as they turn on the same horizontal axis and sit in the same
 * plane across it, far enough apart not to overlap and at most MAX_REACH blocks. The link is a real Create kinetic
 * connection with a ratio: flywheel to sprocket 4x, sprocket to flywheel 1/4, like to like 1:1, so Create's own
 * speed / stress rules apply. The chain is cosmetic and passes through blocks.
 */
public abstract class BeltWheelBlockEntity extends KineticBlockEntity {
    public static final float RATIO = 4;
    public static final int MAX_REACH = 32;
    /** Where the chain runs, in pixels from a wheel's centre: on the flywheel rim, in the sprocket's teeth. */
    public static final float BIG_RADIUS = 23F, SMALL_RADIUS = 8F;

    /** Chains per wheel: two lets a sprocket relay rotation on (Kragnoth, 2026-11-22: transfer stress interestingly). */
    public static final int MAX_CHAINS = 2;
    /** Offsets to the wheels this one is chained to. */
    protected final List<BlockPos> partnerOffsets = new java.util.ArrayList<>();

    protected BeltWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean isBig();

    public float chainRadius() {
        return isBig() ? BIG_RADIUS : SMALL_RADIUS;
    }

    public List<BlockPos> partners() {
        return partnerOffsets.stream().map(worldPosition::offset).toList();
    }

    public boolean hasBelt() {
        return !partnerOffsets.isEmpty();
    }

    public boolean isFull() {
        return partnerOffsets.size() >= MAX_CHAINS;
    }

    public boolean isChainedTo(BlockPos other) {
        return partnerOffsets.contains(other.subtract(worldPosition));
    }

    public static Direction.Axis axis(BlockState s) {
        return s.getValue(BlockStateProperties.HORIZONTAL_AXIS);
    }

    /** The chained wheel at p if it is loaded and chained back to this one. */
    @Nullable
    public BeltWheelBlockEntity partnerAt(BlockPos p) {
        if (level == null || !level.isLoaded(p)) {
            return null;
        }
        return level.getBlockEntity(p) instanceof BeltWheelBlockEntity o && o.isChainedTo(worldPosition) ? o : null;
    }

    /** Why a chain can't join a and b, or null if it can. */
    @Nullable
    public static String linkProblem(Level level, BlockPos a, BlockPos b) {
        if (!(level.getBlockEntity(a) instanceof BeltWheelBlockEntity wa) || !(level.getBlockEntity(b) instanceof BeltWheelBlockEntity wb)
                || a.equals(b)) {
            return "not_wheels";
        }
        if (wa.isFull() || wb.isFull() || wa.isChainedTo(b)) {
            return "taken";
        }
        Direction.Axis ax = axis(wa.getBlockState());
        if (ax != axis(wb.getBlockState())) {
            return "axis";
        }
        if (ax == Direction.Axis.X ? a.getX() != b.getX() : a.getZ() != b.getZ()) {
            return "plane";
        }
        double dist = Math.sqrt(a.distSqr(b));
        if (dist * 16 < wa.chainRadius() + wb.chainRadius()) {   // rims would overlap
            return "too_close";
        }
        if (dist > MAX_REACH) {
            return "too_far";
        }
        return null;
    }

    public static boolean link(Level level, BlockPos a, BlockPos b) {
        if (linkProblem(level, a, b) != null) {
            return false;
        }
        BeltWheelBlockEntity wa = (BeltWheelBlockEntity) level.getBlockEntity(a), wb = (BeltWheelBlockEntity) level.getBlockEntity(b);
        wa.detachKinetics();
        wb.detachKinetics();
        wa.partnerOffsets.add(b.subtract(a));
        wb.partnerOffsets.add(a.subtract(b));
        wa.attachKinetics();
        wb.attachKinetics();
        wa.notifyUpdate();
        wb.notifyUpdate();
        return true;
    }

    /** Takes every chain off this wheel (and the other ends); drops one chain item each if asked. */
    public void unlink(boolean drop) {
        unlink(drop, false);
    }

    /** removing: this wheel is being broken, so it isn't re-attached to the kinetic network. */
    public void unlink(boolean drop, boolean removing) {
        for (BlockPos p : partners()) {
            unlinkFrom(p, drop, removing);
        }
    }

    private void unlinkFrom(BlockPos p, boolean drop, boolean removing) {
        BlockPos off = p.subtract(worldPosition);
        if (!partnerOffsets.contains(off)) {
            return;
        }
        BeltWheelBlockEntity other = partnerAt(p);
        detachKinetics();
        partnerOffsets.remove(off);
        if (other != null) {
            other.detachKinetics();
            other.partnerOffsets.remove(worldPosition.subtract(p));
            other.attachKinetics();
            other.notifyUpdate();
        }
        if (!removing && !isRemoved()) {
            attachKinetics();
            notifyUpdate();
        }
        if (drop && level != null && !level.isClientSide) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    new ItemStack(ModItems.DRIVE_BELT.get()));
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide) {
            return;
        }
        for (BlockPos p : partners()) {
            if (level.isLoaded(p) && partnerAt(p) == null) {
                unlinkFrom(p, true, false);   // the other wheel is gone or turned: that chain falls off
            }
        }
    }

    // ---- Create kinetics: the belt is a connection with a speed ratio

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        for (BlockPos p : partners()) {
            if (!neighbours.contains(p)) {
                neighbours.add(p);
            }
        }
        return super.addPropagationLocations(block, state, neighbours);
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
            boolean connectedViaAxes, boolean connectedViaCogs) {
        if (partnerOffsets.contains(diff) && target instanceof BeltWheelBlockEntity o && o.isChainedTo(worldPosition)) {
            // open chain: both wheels turn the same way, speeds in inverse proportion to their sizes
            return isBig() == o.isBig() ? 1 : isBig() ? RATIO : 1 / RATIO;
        }
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        return isChainedTo(other.getBlockPos()) || super.isCustomConnection(other, state, otherState);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (BlockPos o : partnerOffsets) {
            list.add(NbtUtils.writeBlockPos(o));
        }
        tag.put("Chains", list);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        partnerOffsets.clear();
        for (net.minecraft.nbt.Tag t : tag.getList("Chains", net.minecraft.nbt.Tag.TAG_INT_ARRAY)) {
            if (t instanceof net.minecraft.nbt.IntArrayTag a && a.size() == 3) {
                partnerOffsets.add(new BlockPos(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()));
            }
        }
        if (tag.contains("BeltPartner")) {   // 0.11.0 - 0.11.2 saves: one chain
            NbtUtils.readBlockPos(tag, "BeltPartner").ifPresent(o -> {
                if (!partnerOffsets.contains(o)) {
                    partnerOffsets.add(o);
                }
            });
        }
    }
}
