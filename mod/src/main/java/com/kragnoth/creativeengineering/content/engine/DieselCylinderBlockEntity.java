package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.CEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds nothing: exists so the cylinder's rocker arm can be drawn moving (Flywheel visual, or a renderer fallback).
 * The rocker nods once per firing (4-stroke at 64 RPM: about every 2 seconds), each cylinder at its own point in the
 * cycle. Purely visual and client-side.
 */
public class DieselCylinderBlockEntity extends BlockEntity {
    /** Rocker pivot in model space (16ths, upright cylinder before its 45 degree lean). */
    public static final float PIVOT_X = 8 / 16F, PIVOT_Y = 20.5F / 16F, PIVOT_Z = 8.25F / 16F;
    private static final float FIRINGS_PER_SECOND = 64 / 60F / 2F;
    private static final float MAX_NOD_DEG = 7;

    public DieselCylinderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Rocker angle in degrees for this frame (0 when stopped or when animation is turned off). */
    public float rockerAngle(float partialTick) {
        if (level == null || !getBlockState().getValue(DieselCylinderBlock.RUNNING) || !CEConfig.animateEngines()) {
            return 0;
        }
        float t = (level.getGameTime() + partialTick) / 20F;
        float phase = (worldPosition.hashCode() & 7) * Mth.TWO_PI / 8F;
        float s = Mth.sin(t * FIRINGS_PER_SECOND * Mth.TWO_PI + phase);
        return s > 0 ? MAX_NOD_DEG * s * s : 0;   // lift, hold briefly, settle; nothing on the down half
    }

    /** Model y-rotation (degrees, Flywheel's counter-clockwise) for the cylinder's facing; model leans toward +Z. */
    public static float yRot(BlockState state) {
        return switch (state.getValue(DieselCylinderBlock.FACING)) {
            case WEST -> -90;
            case NORTH -> 180;
            case EAST -> 90;
            default -> 0;
        };
    }
}
