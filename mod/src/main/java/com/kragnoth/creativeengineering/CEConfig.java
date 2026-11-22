package com.kragnoth.creativeengineering;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client settings (config/creativeengineering-client.toml). Purely visual: nothing here changes how machines work. */
public final class CEConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue ANIMATE_ENGINES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("visuals");
        ANIMATE_ENGINES = b.comment("Animate diesel engine valve gear (the rocker arms nod while running).",
                        "Purely visual: off freezes them. Stress, fuel and speed are unaffected.")
                .define("animateEngines", true);
        b.pop();
        CLIENT_SPEC = b.build();
    }

    private CEConfig() {}

    /** Safe before the config has loaded. */
    public static boolean animateEngines() {
        try {
            return ANIMATE_ENGINES.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }
}
