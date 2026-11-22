package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.content.petroleum.PumpjackRig;
import dev.engine_room.flywheel.lib.transform.Affine;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * Places the Pumpjack's moving parts for a rig pose, shared by the instanced visuals and the renderer fallbacks. Each
 * method expects the transform at the owning block's origin (gearbox for the crank, Samson Post for everything else).
 * Beam pieces are modelled about a local origin (pivot / ends) so every model stays in vanilla's -16..32 range.
 */
final class PumpjackPoser {
    private PumpjackPoser() {}

    static final float P = 1 / 16F;
    /** Pitman arms run from the crank pins at these x (px) to the equalizer. */
    static final float[] PITMAN_X = {1.5F, 14.5F};
    /** Pitman model length (px), stretched to the pose's length. */
    static final float PITMAN_MODEL = 32;

    static <T extends Affine<T>> T face(T t, Direction f) {
        return t.center().rotateYDegrees(PumpjackRig.yRot(f)).uncenter();
    }

    static <T extends Affine<T>> T crank(T t, Direction f, float theta) {
        return face(t, f).translate(0, 8 * P, 8 * P).rotateXDegrees(theta * Mth.RAD_TO_DEG).translate(0, -8 * P, -8 * P);
    }

    /** About the pivot, then out to dz px along the beam and dy px below its pivot line. */
    static <T extends Affine<T>> T onBeam(T t, Direction f, PumpjackRig.Pose p, float dy, float dz) {
        return face(t, f).translate(0, PumpjackRig.PIVOT_Y * P, PumpjackRig.PIVOT_Z * P).rotateXDegrees(p.beamDeg()).translate(0, dy * P, dz * P);
    }

    static <T extends Affine<T>> T pitman(T t, Direction f, PumpjackRig.Pose p, float x) {
        return face(t, f).translate(x * P, p.pinY() * P, p.pinZ() * P).rotateXDegrees(p.pitmanDeg()).scale(1, p.pitmanLen() / PITMAN_MODEL, 1);
    }

    /** Polished rod (modelled with its top at y 16, z 8), hanging from the horsehead's lip over the stroked block. */
    static <T extends Affine<T>> T rod(T t, Direction f, PumpjackRig.Pose p, int b) {
        return face(t, f).translate(0, (p.rodTop() - 16) * P, 16 * b * P);
    }
}
