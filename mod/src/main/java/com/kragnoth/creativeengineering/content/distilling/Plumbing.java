package com.kragnoth.creativeengineering.content.distilling;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * The Distilling Tank's dressing: brass collars every third level, a gauge high up, the relief valve top-middle, and a
 * bolted coupling where a tank touches a fraction level. (The copper pipe lanes and header bands of 0.12.2 were removed
 * in 0.21.1: Kragnoth found them silly.) Pure decoration, drawn by the client (visual / renderer), never in block states.
 */
public final class Plumbing {
    private Plumbing() {}

    /** A model to draw at this block, turned yRot degrees (models are built on the north face). */
    public record Part(String model, int yRot) {}

    private static final int[] ROT = {0, 90, 180, 270};   // north, east, south, west

    /** outlets: faces (N, E, S, W bits) of this block touching a container, each drawn with a bolted coupling. */
    public static List<Part> parts(BlockPos controller, int sx, int sy, int sz, BlockPos rel, int outlets) {
        List<Part> out = new ArrayList<>();
        if (sy < DistillingTankBlockEntity.MIN_HEIGHT) {
            return out;
        }
        int y = rel.getY();
        boolean[] on = {rel.getZ() == 0, rel.getX() == sx - 1, rel.getZ() == sz - 1, rel.getX() == 0};
        int[] along = {rel.getX(), rel.getZ(), sx - 1 - rel.getX(), sz - 1 - rel.getZ()};
        for (int f = 0; f < 4; f++) {
            if ((outlets & (1 << f)) != 0) {
                out.add(new Part("distilling_tank_outlet", ROT[f]));
            }
            if (!on[f]) {
                continue;
            }
            if (y % 3 == 2 && y != sy - 1) {
                out.add(new Part("distilling_tank_collar", ROT[f]));
            }
            if (f == 0 && y == sy - 1 && along[f] == sx / 2) {
                out.add(new Part("distilling_tank_gauge", ROT[f]));
            }
        }
        if (y == sy - 1 && rel.getX() == sx / 2 && rel.getZ() == sz / 2) {
            out.add(new Part("distilling_tank_valve", 0));
        }
        return out;
    }
}
