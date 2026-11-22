package com.kragnoth.creativeengineering.content.petroleum;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Infinite crude oil veins (docs/petroleum-spec.md; Kragnoth, 2026-11-22: "prospect for infinite veins ... place down a
 * nodding donkey in a chunk and create oil"). Overworld only. The world is split into REGION x REGION chunk regions;
 * each region has one vein with probability CHANCE, in one of its chunks, flowing RATE_MIN..RATE_MAX mB/s forever. All
 * of it follows from the world seed, so nothing is stored and every server agrees. Tests can force a vein.
 */
public final class OilVeins {
    private OilVeins() {}

    public static final int REGION = 8, RATE_MIN = 20, RATE_MAX = 60;
    public static final float CHANCE = 0.85F;
    private static final Map<Long, Integer> FORCED = new HashMap<>();

    /** Test hook: pretend this chunk has a vein of this rate (0 = forced dry). */
    public static void force(ChunkPos chunk, int rate) {
        FORCED.put(chunk.toLong(), Math.max(0, rate));
    }

    /** Test hook: back to the world's own veins for this chunk. */
    public static void unforce(ChunkPos chunk) {
        FORCED.remove(chunk.toLong());
    }

    /** mB per second of the vein under this chunk, or 0. */
    public static int rate(Level level, ChunkPos chunk) {
        Integer forced = FORCED.get(chunk.toLong());
        if (forced != null) {
            return forced;
        }
        if (!(level instanceof ServerLevel sl) || level.dimension() != Level.OVERWORLD) {
            return 0;
        }
        int rx = Math.floorDiv(chunk.x, REGION), rz = Math.floorDiv(chunk.z, REGION);
        Random rnd = new Random(sl.getSeed() ^ (rx * 341873128712L + rz * 132897987541L) ^ 0x0115_C0DEL);
        if (rnd.nextFloat() >= CHANCE) {
            return 0;
        }
        int vx = rx * REGION + rnd.nextInt(REGION), vz = rz * REGION + rnd.nextInt(REGION);
        return vx == chunk.x && vz == chunk.z ? RATE_MIN + rnd.nextInt(RATE_MAX - RATE_MIN + 1) : 0;
    }

    /** The nearest vein chunk within radius chunks (by chunk distance), or null. */
    @Nullable
    public static ChunkPos nearest(Level level, ChunkPos from, int radius) {
        ChunkPos best = null;
        double bestD = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos c = new ChunkPos(from.x + dx, from.z + dz);
                double d = dx * dx + dz * dz;
                if (d < bestD && rate(level, c) > 0) {
                    bestD = d;
                    best = c;
                }
            }
        }
        return best;
    }
}
