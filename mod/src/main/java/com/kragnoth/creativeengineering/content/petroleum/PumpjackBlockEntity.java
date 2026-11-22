package com.kragnoth.creativeengineering.content.petroleum;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Pumpjack drive. While turning it strokes the block in front (StrokeDriven) every tick with power min(1, RPM / 64);
 * what that block does with it is its own business (a Wellhead pumps). Animation helpers and the linkage pose live here
 * so the visual and the renderer fallback agree.
 */
public class PumpjackBlockEntity extends KineticBlockEntity {
    public static final int FULL_RPM = 64;
    /** Shaft turns per crank turn (the gearbox's reduction): 64 RPM is 16 strokes a minute. */
    public static final float REDUCTION = 4F;

    public PumpjackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction facing() {
        return getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
    }

    public BlockPos head() {
        return worldPosition.relative(facing());
    }

    public float power() {
        return Math.min(1F, Math.abs(getSpeed()) / FULL_RPM);
    }

    /** The block under the horsehead, if it can be stroked (both sides: the client hides the rod without one). */
    public StrokeDriven target() {
        if (level == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(head());
        return be instanceof StrokeDriven d ? d : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || getSpeed() == 0) {
            return;
        }
        StrokeDriven t = target();
        if (t != null) {
            t.stroke(power());
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        super.addToGoggleTooltip(tooltip, sneaking);
        tooltip.add(Component.literal("    ").append(Component.translatable("block.creativeengineering.pumpjack")).withStyle(ChatFormatting.WHITE));
        StrokeDriven t = target();
        tooltip.add(Component.literal("     ").append(t == null ? Component.translatable("creativeengineering.pumpjack.no_target")
                : Component.translatable("creativeengineering.pumpjack.driving", level.getBlockState(head()).getBlock().getName()))
                .withStyle(t == null ? ChatFormatting.RED : ChatFormatting.GOLD));
        if (t != null && Math.abs(getSpeed()) < FULL_RPM) {
            tooltip.add(Component.literal("     ").append(Component.translatable("creativeengineering.pumpjack.slow", FULL_RPM)).withStyle(ChatFormatting.DARK_GRAY));
        }
        return true;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2, 2, 2);   // the beam overhangs the block in front and rises 2 high
    }

    // ------------------------------------------------------------------------------------------------ animation
    /** Crank angle in radians for this frame (client). Slower than the shaft by REDUCTION; frozen when stopped. */
    public float crankAngle(float partialTick) {
        float time = AnimationTickHolder.getRenderTime(level);
        return (time * getSpeed() * 3F / 10F / REDUCTION) % 360F * Mth.DEG_TO_RAD;
    }

    /**
     * Linkage pose in pixels of the block's model space (built facing south; the stroked block is z 16..32). Crank on X
     * through the shaft (y 8, z 8), radius 2.5; beam pivot on the samson post (y 28, z 15); the beam's rear pin is
     * (-1, -7) and the horsehead's lower lip (-6, 9) from the pivot, so the polished rod hangs at z 24, over the block in
     * front's centre.
     */
    public record Pose(float beamDeg, float pinY, float pinZ, float pitmanLen, float pitmanDeg, float rodTop) {}

    public static final float CRANK_R = 2.5F, PIVOT_Y = 28, PIVOT_Z = 15, REAR = 7, LIP_Y = -6, LIP_Z = 9;

    public static Pose pose(float crank) {
        float pinY = 8 + CRANK_R * Mth.cos(crank), pinZ = 8 + CRANK_R * Mth.sin(crank);
        float phi = (float) Math.asin((pinY - 8) / REAR);
        float c = Mth.cos(phi), s = Mth.sin(phi);
        float rearY = PIVOT_Y + (-1 * c + REAR * s), rearZ = PIVOT_Z + (-1 * s - REAR * c);
        float dy = rearY - pinY, dz = rearZ - pinZ;
        float lipY = PIVOT_Y + (LIP_Y * c - LIP_Z * s);
        return new Pose(phi * Mth.RAD_TO_DEG, pinY, pinZ, Mth.sqrt(dy * dy + dz * dz), (float) Math.atan2(dz, dy) * Mth.RAD_TO_DEG, lipY);
    }

    /** Flywheel's (counter-clockwise) y rotation for the facing; the model faces south. */
    public static float yRot(Direction f) {
        return switch (f) {
            case EAST -> 90;
            case NORTH -> 180;
            case WEST -> -90;
            default -> 0;
        };
    }
}
