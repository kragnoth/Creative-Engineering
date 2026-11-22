package com.kragnoth.creativeengineering.content.petroleum;

import com.google.gson.JsonElement;
import com.kragnoth.creativeengineering.CreativeEngineering;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * What a Wellhead brings up, data-driven: data/&lt;ns&gt;/well_sources/*.json. Each source names a fluid and either a
 * fixed rate (mB/s at full stroke) or "oil_vein": true (the rate of the OilVeins vein under the chunk, if any), and may
 * limit itself to a dimension and to biomes (ids or #tags). The highest-priority source that applies wins.
 * <pre>{ "fluid": "minecraft:water", "rate": 20, "dimension": "minecraft:overworld", "biomes": ["#c:is_desert"], "priority": 10 }</pre>
 */
public final class WellSources extends SimpleJsonResourceReloadListener {
    public record Source(ResourceLocation id, ResourceLocation fluid, int rate, boolean oilVein, Optional<ResourceLocation> dimension,
                         List<String> biomes, int priority) {
        static final Codec<Source> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("fluid").forGetter(Source::fluid),
                Codec.INT.optionalFieldOf("rate", 0).forGetter(Source::rate),
                Codec.BOOL.optionalFieldOf("oil_vein", false).forGetter(Source::oilVein),
                ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(Source::dimension),
                Codec.STRING.listOf().optionalFieldOf("biomes", List.of()).forGetter(Source::biomes),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Source::priority)
        ).apply(i, (f, r, v, d, b, p) -> new Source(null, f, r, v, d, b, p)));

        Source withId(ResourceLocation id) {
            return new Source(id, fluid, rate, oilVein, dimension, biomes, priority);
        }

        boolean matches(Level level, Holder<Biome> biome) {
            if (dimension.isPresent() && !level.dimension().location().equals(dimension.get())) {
                return false;
            }
            if (biomes.isEmpty()) {
                return true;
            }
            for (String b : biomes) {
                if (b.startsWith("#") ? biome.is(TagKey.create(Registries.BIOME, ResourceLocation.parse(b.substring(1))))
                        : biome.is(ResourceLocation.parse(b))) {
                    return true;
                }
            }
            return false;
        }
    }

    /** A resolved well: the fluid and its mB/s at full stroke, and the source it came from. */
    public record Yield(Fluid fluid, int rate, ResourceLocation source) {}

    private static List<Source> sources = List.of();

    private WellSources() {
        super(new com.google.gson.Gson(), "well_sources");
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new WellSources());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        List<Source> out = new ArrayList<>();
        files.forEach((id, json) -> Source.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(e -> CreativeEngineering.LOGGER.error("well source {}: {}", id, e))
                .ifPresent(s -> out.add(s.withId(id))));
        out.sort(Comparator.comparingInt(Source::priority).reversed().thenComparing(s -> s.id().toString()));
        sources = List.copyOf(out);
    }

    /** What a Wellhead at pos brings up, or null for a dry spot. */
    @Nullable
    public static Yield at(Level level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        for (Source s : sources) {
            if (!s.matches(level, biome)) {
                continue;
            }
            Fluid f = BuiltInRegistries.FLUID.get(s.fluid());
            if (f == Fluids.EMPTY) {
                continue;
            }
            int rate = s.oilVein() ? OilVeins.rate(level, new ChunkPos(pos)) : s.rate();
            if (rate > 0) {
                return new Yield(f, rate, s.id());
            }
        }
        return null;
    }
}
