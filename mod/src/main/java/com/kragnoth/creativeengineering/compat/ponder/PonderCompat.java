package com.kragnoth.creativeengineering.compat.ponder;

import net.createmod.ponder.foundation.PonderIndex;

/** Registers Creative Engineering's Ponder plugin. Only called (client side) when Ponder is loaded. */
public final class PonderCompat {
    private PonderCompat() {}

    public static void init() {
        PonderIndex.addPlugin(new CEPonderPlugin());
    }
}
