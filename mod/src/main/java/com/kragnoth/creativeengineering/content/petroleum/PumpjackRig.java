package com.kragnoth.creativeengineering.content.petroleum;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * How a Pumpjack's separate parts find each other (docs/petroleum-spec.md): all in a row along the gearbox's facing,
 * Crank Gearbox, then a Samson Post 1 or 2 blocks on (the rear arm, a), then a stroked block 1 or 2 blocks past the post
 * (the reach, b). Nothing forms: each part only looks for the others where they'd have to stand, like Create's steam
 * engine looking for its boiler. stroke = b / a: a longer reach strokes deeper (more out, more stress).
 * Also holds the linkage pose shared by the server-free visuals and renderers.
 */
public record PumpjackRig(@Nullable BlockPos gearbox, BlockPos post, @Nullable BlockPos well, int a, int b, Direction facing) {
    public float stroke() {
        return (float) b / a;
    }

    @Nullable
    public static PumpjackRig fromGearbox(Level level, BlockPos gearbox, Direction f) {
        for (int a = 1; a <= 2; a++) {
            BlockPos post = gearbox.relative(f, a);
            if (isPost(level.getBlockState(post), f)) {
                return withWell(level, gearbox, post, a, f);
            }
        }
        return null;
    }

    /** From the post's side (its visual): the gearbox may be missing (then the beam rests level). */
    public static PumpjackRig fromPost(Level level, BlockPos post, Direction f) {
        for (int a = 1; a <= 2; a++) {
            BlockPos g = post.relative(f.getOpposite(), a);
            BlockState s = level.getBlockState(g);
            if (s.getBlock() instanceof PumpjackBlock && s.getValue(PumpjackBlock.HORIZONTAL_FACING) == f) {
                return withWell(level, g, post, a, f);
            }
        }
        return withWell(level, null, post, 1, f);
    }

    private static PumpjackRig withWell(Level level, @Nullable BlockPos g, BlockPos post, int a, Direction f) {
        for (int b = 1; b <= 2; b++) {
            BlockPos w = post.relative(f, b);
            if (level.getBlockEntity(w) instanceof StrokeDriven) {
                return new PumpjackRig(g, post, w, a, b, f);
            }
        }
        return new PumpjackRig(g, post, null, a, 1, f);
    }

    static boolean isPost(BlockState s, Direction f) {
        return s.getBlock() instanceof SamsonPostBlock && s.getValue(SamsonPostBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER
                && s.getValue(SamsonPostBlock.FACING) == f;
    }

    // ------------------------------------------------------------------------------------------------ linkage
    /** Crank radius and the walking beam's pivot, in pixels of the post's model space (machine built facing south). */
    public static final float CRANK_R = 4, PIVOT_Y = 41, PIVOT_Z = 8, LIP_Y = -6;

    /**
     * Pose for crank angle theta (radians). Post space: the gearbox's shaft is at (y 8, z 8 - 16a); the beam's rear pin
     * is (-1, -16a) and the horsehead's lip (-6, 16b) from the pivot, so the polished rod hangs over the stroked block.
     */
    public record Pose(float beamDeg, float pinY, float pinZ, float pitmanLen, float pitmanDeg, float rodTop) {}

    public Pose pose(float theta) {
        float pinY = 8 + CRANK_R * Mth.cos(theta), pinZ = 8 - 16 * a + CRANK_R * Mth.sin(theta);
        float phi = (float) Math.asin((pinY - 8) / (16F * a));
        float c = Mth.cos(phi), s = Mth.sin(phi);
        float rearY = PIVOT_Y - c + 16 * a * s, rearZ = PIVOT_Z - s - 16 * a * c;
        float dy = rearY - pinY, dz = rearZ - pinZ;
        float lipY = PIVOT_Y + LIP_Y * c - 16 * b * s;
        return new Pose(phi * Mth.RAD_TO_DEG, pinY, pinZ, Mth.sqrt(dy * dy + dz * dz), (float) Math.atan2(dz, dy) * Mth.RAD_TO_DEG, lipY);
    }

    /** Flywheel's (counter-clockwise) y rotation for a facing; models face south. */
    public static float yRot(Direction f) {
        return switch (f) {
            case EAST -> 90;
            case NORTH -> 180;
            case WEST -> -90;
            default -> 0;
        };
    }
}
