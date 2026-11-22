package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity;
import dev.engine_room.flywheel.lib.transform.Affine;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * Places the Pumpjack's moving parts for a pose (PumpjackBlockEntity.pose), shared by the instanced visual and the
 * renderer fallback. Each method expects the transform already at the gearbox block's origin.
 */
final class PumpjackLinkage {
    private PumpjackLinkage() {}

    static final float P = 1 / 16F;

    static <T extends Affine<T>> T face(T t, Direction facing) {
        return t.center().rotateYDegrees(PumpjackBlockEntity.yRot(facing)).uncenter();
    }

    /** Beam partial is modelled in the frame block's space, pivot (8, 28, 8); the pivot sits at (8, 28, 24) here. */
    static <T extends Affine<T>> T beam(T t, Direction f, PumpjackBlockEntity.Pose p) {
        return face(t, f).translate(8 * P, 28 * P, 24 * P).rotateXDegrees(p.beamDeg()).translate(-8 * P, -28 * P, -8 * P);
    }

    static <T extends Affine<T>> T crank(T t, Direction f, float crank) {
        return face(t, f).translate(0, 8 * P, 8 * P).rotateXDegrees(crank * Mth.RAD_TO_DEG).translate(0, -8 * P, -8 * P);
    }

    /** Pitman arm (modelled upright, 18 px, centred on x/z 0) from the crank pin at x to the beam's rear pin. */
    static <T extends Affine<T>> T pitman(T t, Direction f, PumpjackBlockEntity.Pose p, float x) {
        return face(t, f).translate(x * P, p.pinY() * P, p.pinZ() * P).rotateXDegrees(p.pitmanDeg()).scale(1, p.pitmanLen() / 18F, 1);
    }

    /** Polished rod, top at the horsehead's lip, hanging over the wellhead (z 40). */
    static <T extends Affine<T>> T rod(T t, Direction f, PumpjackBlockEntity.Pose p) {
        return face(t, f).translate(0, p.rodTop() * P, 32 * P);
    }
}
