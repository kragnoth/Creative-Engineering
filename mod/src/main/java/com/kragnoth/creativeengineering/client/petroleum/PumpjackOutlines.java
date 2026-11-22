package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.content.petroleum.PumpjackRig;
import com.simibubi.create.AllSpecialTextures;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Oritech-style guide, Create's outliner: shows where a Pumpjack's Samson Post and Wellhead go (green = in place). */
public final class PumpjackOutlines {
    private PumpjackOutlines() {}

    public static void show(Level level, BlockPos gearbox, Direction f) {
        PumpjackRig rig = PumpjackRig.fromGearbox(level, gearbox, f);
        BlockPos post = rig != null ? rig.post() : gearbox.relative(f);
        BlockPos well = rig != null && rig.well() != null ? rig.well() : post.relative(f);
        outline("pumpjack_post_" + gearbox.asLong(), new AABB(post).expandTowards(0, 1, 0), rig != null);
        outline("pumpjack_well_" + gearbox.asLong(), new AABB(well), rig != null && rig.well() != null);
    }

    private static void outline(String key, AABB box, boolean present) {
        Outliner.getInstance().showAABB(key, box.deflate(1 / 64D), 100)
                .lineWidth(1 / 16F)
                .colored(present ? 0x7FD47F : 0xEEEEEE)
                .withFaceTexture(AllSpecialTextures.SELECTION);
    }
}
