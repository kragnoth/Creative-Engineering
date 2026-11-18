package com.kragnoth.creativeengineering.content.shared;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

/** What Create's Display Links can read off any of our machines (compat/create/CEDisplaySources). Any brick works. */
public interface MachineReadout {
    /** Working / Idle, or why it stopped (same text as goggles and Jade). */
    Component readoutStatus();

    /** Average progress of what's running, 0..1, or null when nothing is. */
    @Nullable
    Float readoutProgress();

    /** Finished items waiting to be taken out. */
    int readoutOutput();

    /** Coke oven creosote 0..1; null for machines without a tank. */
    @Nullable
    default Float readoutTank() {
        return null;
    }

    static Component status(@Nullable Component blocker, boolean busy) {
        if (blocker != null) {
            return blocker;
        }
        return Component.translatable("creativeengineering.readout." + (busy ? "working" : "idle"));
    }
}
