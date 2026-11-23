package com.kragnoth.creativeengineering.content.petroleum;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.world.level.Level;

/**
 * Lubricated Create machines (docs/petroleum-spec.md). An Oil Can's dose (DOSE mB) lubricates any kinetic block for
 * DURATION ticks (a Minecraft day), stacking up to MAX; while lubricated its stress impact on the network is cut by
 * a quarter. The state lives on every KineticBlockEntity via mixin (KineticBlockEntityLubeMixin: saved, synced for
 * goggles, expires on its own) and the cut is applied where Create sums network stress (KineticNetworkLubeMixin).
 */
public final class Lubrication {
    private Lubrication() {}

    public static final int DOSE = 50;
    public static final long DURATION = 24000, MAX = 3 * DURATION;
    public static final float STRESS_FACTOR = 0.75F;

    /** Implemented on KineticBlockEntity by mixin. */
    public interface Lubricated {
        long creativeengineering$lubedUntil();

        void creativeengineering$setLubedUntil(long gameTime);
    }

    public static boolean isLubricated(KineticBlockEntity be) {
        Level level = be.getLevel();
        return level != null && be instanceof Lubricated l && l.creativeengineering$lubedUntil() > level.getGameTime();
    }

    /** Ticks left (0 when dry). */
    public static long remaining(KineticBlockEntity be) {
        Level level = be.getLevel();
        return level != null && be instanceof Lubricated l ? Math.max(0, l.creativeengineering$lubedUntil() - level.getGameTime()) : 0;
    }

    /** One dose: extends the lubrication (capped), refreshes the network's stress and the clients. False when topped up. */
    public static boolean apply(KineticBlockEntity be) {
        if (!(be instanceof Lubricated l) || be.getLevel() == null) {
            return false;
        }
        long now = be.getLevel().getGameTime();
        long from = Math.max(now, l.creativeengineering$lubedUntil());
        if (from - now > MAX - DURATION) {
            return false;
        }
        l.creativeengineering$setLubedUntil(from + DURATION);
        refresh(be);
        return true;
    }

    static void refresh(KineticBlockEntity be) {
        if (be.hasNetwork()) {
            be.getOrCreateNetwork().updateStress();
        }
        be.sendData();
        be.setChanged();
    }

    /** Mixin hook: called once a second on the server; clears an expired lubrication and gives the stress back. */
    public static void expire(KineticBlockEntity be) {
        if (be instanceof Lubricated l && l.creativeengineering$lubedUntil() > 0 && l.creativeengineering$lubedUntil() <= be.getLevel().getGameTime()) {
            l.creativeengineering$setLubedUntil(0);
            refresh(be);
        }
    }
}
