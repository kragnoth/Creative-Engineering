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
 * A wheel that can carry a Drive Belt (Kragnoth, 2026-11-22): the flywheel (big) or a Drive Pulley (small), linked
 * vertically 2..8 blocks apart on the same horizontal axis. The link is a real Create kinetic connection with a
 * ratio: the pulley turns RATIO x the flywheel's speed (and the flywheel 1 / RATIO of a driving pulley), so Create's
 * own speed / stress rules apply. The belt is cosmetic and passes through blocks.
 */
public abstract class BeltWheelBlockEntity extends KineticBlockEntity {
    public static final float RATIO = 4;
    public static final int MIN_GAP = 2, MAX_GAP = 8;
    /** Belt radii in pixels: flywheel rim and pulley drum (4:1, matching RATIO). */
    public static final float BIG_RADIUS = 22.4F, SMALL_RADIUS = 5.6F;

    /** Offset to the other wheel, or null when no belt is fitted. */
    @Nullable protected BlockPos partnerOffset;

    protected BeltWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean isBig();

    @Nullable
    public BlockPos partner() {
        return partnerOffset == null ? null : worldPosition.offset(partnerOffset);
    }

    public boolean hasBelt() {
        return partnerOffset != null;
    }

    public static Direction.Axis axis(BlockState s) {
        return s.getValue(BlockStateProperties.HORIZONTAL_AXIS);
    }

    @Nullable
    public BeltWheelBlockEntity partnerBE() {
        BlockPos p = partner();
        if (p == null || level == null || !level.isLoaded(p)) {
            return null;
        }
        return level.getBlockEntity(p) instanceof BeltWheelBlockEntity o && worldPosition.equals(o.partner()) ? o : null;
    }

    /** Why a belt can't join a and b, or null if it can. */
    @Nullable
    public static String linkProblem(Level level, BlockPos a, BlockPos b) {
        if (!(level.getBlockEntity(a) instanceof BeltWheelBlockEntity wa) || !(level.getBlockEntity(b) instanceof BeltWheelBlockEntity wb)) {
            return "not_wheels";
        }
        if (wa.isBig() == wb.isBig()) {
            return "need_both";
        }
        if (wa.hasBelt() || wb.hasBelt()) {
            return "taken";
        }
        int gap = Math.abs(a.getY() - b.getY());
        if (a.getX() != b.getX() || a.getZ() != b.getZ() || gap < MIN_GAP || gap > MAX_GAP) {
            return "alignment";
        }
        if (axis(wa.getBlockState()) != axis(wb.getBlockState())) {
            return "axis";
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
        wa.partnerOffset = b.subtract(a);
        wb.partnerOffset = a.subtract(b);
        wa.attachKinetics();
        wb.attachKinetics();
        wa.notifyUpdate();
        wb.notifyUpdate();
        return true;
    }

    /** Takes the belt off (both ends); drops the belt item at this wheel if asked. */
    public void unlink(boolean drop) {
        unlink(drop, false);
    }

    /** removing: this wheel is being broken, so it isn't re-attached to the kinetic network. */
    public void unlink(boolean drop, boolean removing) {
        BeltWheelBlockEntity other = partnerBE();
        if (partnerOffset == null) {
            return;
        }
        detachKinetics();
        partnerOffset = null;
        if (other != null) {
            other.detachKinetics();
            other.partnerOffset = null;
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
        if (level != null && !level.isClientSide && partnerOffset != null && level.isLoaded(partner()) && partnerBE() == null) {
            unlink(true);   // the other wheel is gone or turned: the belt falls off
        }
    }

    // ---- Create kinetics: the belt is a connection with a speed ratio

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        BlockPos p = partner();
        if (p != null && !neighbours.contains(p)) {
            neighbours.add(p);
        }
        return super.addPropagationLocations(block, state, neighbours);
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
            boolean connectedViaAxes, boolean connectedViaCogs) {
        if (partnerOffset != null && diff.equals(partnerOffset) && target instanceof BeltWheelBlockEntity o
                && worldPosition.equals(o.partner())) {
            return isBig() ? RATIO : 1 / RATIO;   // open belt: both wheels turn the same way
        }
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        return (partnerOffset != null && other.getBlockPos().equals(partner())) || super.isCustomConnection(other, state, otherState);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (partnerOffset != null) {
            tag.put("BeltPartner", NbtUtils.writeBlockPos(partnerOffset));
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        partnerOffset = tag.contains("BeltPartner") ? NbtUtils.readBlockPos(tag, "BeltPartner").orElse(null) : null;
    }
}
