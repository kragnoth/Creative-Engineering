package com.kragnoth.creativeengineering.content.distilling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;

/**
 * The Distilling Tank's dressing (0.12.2; Kragnoth: "steel tank look, with proper greebling, like industrial pipes
 * routing around on the outside"): Create copper pipe lanes up the outside (each against the left / right edge of a
 * chosen block of a face, bending into the wall at the boiler and over the roof edge at the top), header pipes across
 * some faces off the block seams, brass collars every third level, a gauge high up, and the relief valve with its
 * gooseneck top-middle. Seeded by the controller position and size: every block of a column agrees, and a column always
 * looks the same. At least two faces get a lane. Pure decoration, drawn by the client (visual / renderer), never in
 * block states.
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
        Random rnd = new Random(controller.asLong() * 31L + sx * 961L + sy * 31L + sz);
        List<int[]>[] lanes = new List[4];
        int[] band = new int[4];
        int faces = 0;
        for (int f = 0; f < 4; f++) {
            int width = f % 2 == 0 ? sx : sz;
            lanes[f] = new ArrayList<>();
            for (int i = rnd.nextInt(3); i > 0; i--) {
                lanes[f].add(new int[]{rnd.nextInt(width), rnd.nextInt(2)});
            }
            faces += lanes[f].isEmpty() ? 0 : 1;
            band[f] = sy >= 3 && rnd.nextFloat() < 0.6F ? 1 + rnd.nextInt(sy - 2) : -1;
        }
        for (int f = 0; faces < 2 && f < 4; f++) {
            if (lanes[f].isEmpty()) {
                lanes[f].add(new int[]{(f % 2 == 0 ? sx : sz) / 2, f % 2});
                faces++;
            }
        }
        int y = rel.getY();
        boolean[] on = {rel.getZ() == 0, rel.getX() == sx - 1, rel.getZ() == sz - 1, rel.getX() == 0};
        int[] along = {rel.getX(), rel.getZ(), sx - 1 - rel.getX(), sz - 1 - rel.getZ()};
        String suffix = y == 0 ? "_boiler" : y == sy - 1 ? "_top" : "";
        for (int f = 0; f < 4; f++) {
            if ((outlets & (1 << f)) != 0) {
                out.add(new Part("distilling_tank_outlet", ROT[f]));
            }
            if (!on[f]) {
                continue;
            }
            for (int[] lane : lanes[f]) {
                if (lane[0] == along[f]) {
                    out.add(new Part("distilling_tank_pipe_" + (lane[1] == 0 ? "left" : "right") + suffix, ROT[f]));
                }
            }
            if (band[f] == y) {
                out.add(new Part("distilling_tank_pipe_band", ROT[f]));
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
