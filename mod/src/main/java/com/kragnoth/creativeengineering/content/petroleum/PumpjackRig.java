package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.CreativeEngineering;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * How a Pumpjack's parts find each other (docs/petroleum-spec.md). Nothing forms: each part only looks where the others
 * would have to stand, like Create's steam engine looking for its boiler. Along the gearbox's facing, at ground level:
 * <ul>
 *   <li>the Crank Gearbox;</li>
 *   <li>a column the player builds from Create girders or industrial iron (tag pumpjack_column), 2-6 high, 1-3 blocks
 *       on (the rear arm, a), with the Walking Beam Bearing on top (its height, h);</li>
 *   <li>the stroked block (a Wellhead) 1-6 blocks past the column (the reach, b).</li>
 * </ul>
 * The crank throw grows with the rear arm (4 px, 6 px from a = 2), so stroke = (throw / a) x b / 4: 1 for the
 * smallest rig, up to 6. The horsehead grows with the reach. A long reach swings the horsehead lower, so it needs a
 * taller column: a rig whose horsehead would hit the wellhead is "too short" and does not run.
 */
public record PumpjackRig(@Nullable BlockPos gearbox, BlockPos bearing, BlockPos base, @Nullable BlockPos well,
                          int a, int b, int h, boolean tooShort, Direction facing) {
    public static final TagKey<Block> COLUMN = TagKey.create(Registries.BLOCK, CreativeEngineering.id("pumpjack_column"));
    public static final int MIN_H = 2, MAX_H = 6, MAX_A = 3, MAX_B = 6;
    /** Beam pivot in the bearing block (px); the wellhead's stuffing box top (px above its block's base). */
    public static final float PIVOT_Y = 10, PIVOT_Z = 8, WELL_TOP = 17, CLEARANCE = 6;

    public float crankR() {
        return crankR(a);
    }

    public static float crankR(int a) {
        return a <= 1 ? 4 : 6;
    }

    public float stroke() {
        return crankR() / a * b / 4F;
    }

    /** Horsehead scale: 1 at reach 1, up to 2.25 at reach 6. */
    public float headScale() {
        return 1 + (b - 1) * 0.25F;
    }

    /** Walking beam section scale: 1 for the smallest rig, heavier as the beam gets longer (up to 2). */
    public float beamScale() {
        return Math.min(2F, 1 + 0.2F * (a + b - 2));
    }

    public boolean runs() {
        return gearbox != null && well != null && !tooShort;
    }

    @Nullable
    public static PumpjackRig fromGearbox(Level level, BlockPos gearbox, Direction f) {
        for (int a = 1; a <= MAX_A; a++) {
            BlockPos base = gearbox.relative(f, a);
            int h = column(level, base);
            if (h >= MIN_H && isBearing(level.getBlockState(base.above(h)), f)) {
                return withWell(level, gearbox, base, a, h, f);
            }
        }
        return null;
    }

    /** From the bearing's side (its visual and goggles): the gearbox may be missing (the beam then rests level). */
    public static PumpjackRig fromBearing(Level level, BlockPos bearing, Direction f) {
        int h = 0;
        while (h < MAX_H + 1 && level.getBlockState(bearing.below(h + 1)).is(COLUMN)) {
            h++;
        }
        BlockPos base = bearing.below(h);
        for (int a = 1; a <= MAX_A; a++) {
            BlockPos g = base.relative(f.getOpposite(), a);
            BlockState s = level.getBlockState(g);
            if (s.getBlock() instanceof PumpjackBlock && s.getValue(PumpjackBlock.HORIZONTAL_FACING) == f) {
                return withWell(level, g, base, a, h, f);
            }
        }
        return withWell(level, null, base, 1, h, f);
    }

    /** Column blocks stacked from base upward, uncapped (for the placement guide). */
    public static int columnHeight(Level level, BlockPos base) {
        int h = 0;
        while (h < 16 && level.getBlockState(base.above(h)).is(COLUMN)) {
            h++;
        }
        return h;
    }

    /** Column height from base upward (girders / industrial iron), or 0 past MAX_H. */
    public static int column(Level level, BlockPos base) {
        int h = 0;
        while (h < MAX_H + 1 && level.getBlockState(base.above(h)).is(COLUMN)) {
            h++;
        }
        return h <= MAX_H ? h : 0;
    }

    private static PumpjackRig withWell(Level level, @Nullable BlockPos g, BlockPos base, int a, int h, Direction f) {
        for (int b = 1; b <= MAX_B; b++) {
            BlockPos w = base.relative(f, b);
            if (level.getBlockEntity(w) instanceof StrokeDriven) {
                return new PumpjackRig(g, base.above(h), base, w, a, b, h, !clears(a, b, h), f);
            }
        }
        return new PumpjackRig(g, base.above(h), base, null, a, 1, h, false, f);
    }

    static boolean isBearing(BlockState s, Direction f) {
        return s.getBlock() instanceof BeamBearingBlock && s.getValue(BeamBearingBlock.FACING) == f;
    }

    /** Does the horsehead's lowest swing stay clear of the wellhead's stuffing box? */
    public static boolean clears(int a, int b, int h) {
        float s = 1 + (b - 1) * 0.25F;
        double phiMax = Math.asin(crankR(a) / (16F * a));
        double lipLow = 16 * h + PIVOT_Y - 6 * s * Math.cos(phiMax) - 16 * b * Math.sin(phiMax);
        return lipLow - WELL_TOP >= CLEARANCE;
    }

    // ------------------------------------------------------------------------------------------------ linkage
    /**
     * Pose for crank angle theta (radians), in pixels of the bearing block's model space (machine built facing south).
     * The gearbox's shaft is at (y 8 - 16h, z 8 - 16a); the beam's rear pin is (-2, -16a) and the horsehead's lip
     * (-6 x headScale, 16b) from the pivot (y 10, z 8); the polished rod hangs at z 8 + 16b down into the wellhead.
     */
    public record Pose(float beamDeg, float pinY, float pinZ, float rearY, float rearZ, float lipY, float rodBottom) {}

    public Pose pose(float theta) {
        float r = crankR(), ay = 8 - 16 * h, az = 8 - 16 * a;
        float pinY = ay + r * Mth.cos(theta), pinZ = az + r * Mth.sin(theta);
        float phi = (float) Math.asin(Mth.clamp((pinY - ay) / (16F * a), -1, 1));
        float c = Mth.cos(phi), s = Mth.sin(phi);
        float rearY = PIVOT_Y - 2 * c + 16 * a * s, rearZ = PIVOT_Z - 2 * s - 16 * a * c;
        float lipY = PIVOT_Y - 6 * headScale() * c - 16 * b * s;
        return new Pose(phi * Mth.RAD_TO_DEG, pinY, pinZ, rearY, rearZ, lipY, -16 * h + 12);
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
