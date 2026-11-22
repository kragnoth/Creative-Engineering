package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.content.petroleum.PumpjackRig;
import dev.engine_room.flywheel.lib.transform.Affine;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * Places the Pumpjack's moving parts for a rig pose, shared by the instanced visuals and the renderer fallbacks. The
 * crank is placed from the gearbox's origin; everything else from the Walking Beam Bearing's. Beam pieces are modelled
 * about a local origin; long members (pitman arms, polished rod) are 16 px segments repeated along their length, so no
 * texture is stretched far and every model stays in vanilla's -16..32 range.
 */
final class PumpjackPoser {
    private PumpjackPoser() {}

    static final float P = 1 / 16F;
    /** Pitman arms run from the crank pins at these x (px) to the equalizer. */
    static final float[] PITMAN_X = {1.5F, 14.5F};
    static final int MAX_SEGMENTS = 8;

    static <T extends Affine<T>> T face(T t, Direction f) {
        return t.center().rotateYDegrees(PumpjackRig.yRot(f)).uncenter();
    }

    /** Crank about the shaft (y 8, z 8); the model is built for a 4 px throw and scaled out to the rig's. */
    static <T extends Affine<T>> T crank(T t, Direction f, float theta, float throwPx) {
        float k = throwPx / 4F;
        return face(t, f).translate(0, 8 * P, 8 * P).rotateXDegrees(theta * Mth.RAD_TO_DEG).scale(1, k, k).translate(0, -8 * P, -8 * P);
    }

    /** About the pivot, then dz px along the beam and dy px off its pivot line, scaled by s about that point. */
    static <T extends Affine<T>> T onBeam(T t, Direction f, PumpjackRig.Pose p, float dy, float dz, float s) {
        return face(t, f).translate(0, PumpjackRig.PIVOT_Y * P, PumpjackRig.PIVOT_Z * P).rotateXDegrees(p.beamDeg())
                .translate(0, dy * P, dz * P).translate(8 * P, 0, 0).scale(s).translate(-8 * P, 0, 0);
    }

    /** One block of walking beam, its box section scaled by k about its centre line (bigger rigs, heavier beam). */
    static <T extends Affine<T>> T beamPiece(T t, Direction f, PumpjackRig.Pose p, float dz, float k) {
        return face(t, f).translate(0, PumpjackRig.PIVOT_Y * P, PumpjackRig.PIVOT_Z * P).rotateXDegrees(p.beamDeg())
                .translate(8 * P, P, dz * P).scale(k, k, 1).translate(-8 * P, -P, 0);
    }

    static int segments(float y0, float z0, float y1, float z1) {
        return Mth.clamp(Mth.ceil(Mth.sqrt((y1 - y0) * (y1 - y0) + (z1 - z0) * (z1 - z0)) / 16F), 1, MAX_SEGMENTS);
    }

    /** Segment i of n of a bar from (y0, z0) to (y1, z1) at x (px); the segment model runs 0..16 up +Y about x 0, z 0. */
    static <T extends Affine<T>> T segment(T t, Direction f, float x, float y0, float z0, float y1, float z1, int i, int n) {
        float dy = y1 - y0, dz = z1 - z0, len = Mth.sqrt(dy * dy + dz * dz), step = len / n;
        return face(t, f).translate(x * P, y0 * P, z0 * P).rotateXDegrees((float) Math.atan2(dz, dy) * Mth.RAD_TO_DEG)
                .translate(0, i * step * P, 0).scale(1, step / 16F, 1);
    }

    /** Where the polished rod runs: from the bridle under the horsehead lip down into the stuffing box (px, bearing space). */
    static float rodTop(PumpjackRig.Pose p) {
        return p.lipY() - 1;
    }
}
