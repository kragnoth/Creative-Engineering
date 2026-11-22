package com.kragnoth.creativeengineering.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.kragnoth.creativeengineering.CreativeEngineering;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;

/**
 * Atlas source "creativeengineering:recolor": loads other textures (e.g. Create's fluid tank) at resource-load time
 * and writes colour-shifted copies under our own names: desaturate, set contrast / brightness, multiply by a tint.
 * Nothing of the source is shipped in our jar, and a resource pack that restyles the source restyles ours too
 * (Kragnoth, 2026-11-22: "Create tanks, colour-shifted to industrial steel", like TFMG flavours Create's).
 * Used from assets/minecraft/atlases/blocks.json (dev/gen_data.py).
 */
public record RecolorSpriteSource(List<Entry> sprites, int tint, float saturation, float contrast, float brightness) implements SpriteSource {
    /**
     * Optional "fills": flat-paint a rectangle with the colour of one sample pixel, after recolouring. Coordinates are
     * fractions of the image (0..1) so a higher-resolution resource pack still lines up. Used to blank off Create's
     * valve shaft socket for the Tap, which has no shaft (Kragnoth: "no giant black holes on the sides").
     */
    public record Fill(List<Float> rect, List<Float> from) {
        static final Codec<Fill> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.listOf().fieldOf("rect").forGetter(Fill::rect),
                Codec.FLOAT.listOf().fieldOf("from").forGetter(Fill::from)).apply(i, Fill::new));

        void apply(NativeImage img) {
            int w = img.getWidth(), h = img.getHeight();
            int col = img.getPixelRGBA(Math.min(w - 1, (int) (from.get(0) * w)), Math.min(h - 1, (int) (from.get(1) * h)));
            for (int y = Math.round(rect.get(1) * h); y < Math.round(rect.get(3) * h); y++) {
                for (int x = Math.round(rect.get(0) * w); x < Math.round(rect.get(2) * w); x++) {
                    img.setPixelRGBA(x, y, col);
                }
            }
        }
    }

    public record Entry(ResourceLocation source, ResourceLocation target, List<Fill> fills) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("source").forGetter(Entry::source),
                ResourceLocation.CODEC.fieldOf("target").forGetter(Entry::target),
                Fill.CODEC.listOf().optionalFieldOf("fills", List.of()).forGetter(Entry::fills)).apply(i, Entry::new));
    }

    public static final MapCodec<RecolorSpriteSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Entry.CODEC.listOf().fieldOf("sprites").forGetter(RecolorSpriteSource::sprites),
            Codec.STRING.xmap(s -> Integer.parseInt(s.replace("#", ""), 16), c -> String.format("#%06X", c))
                    .optionalFieldOf("tint", 0xFFFFFF).forGetter(RecolorSpriteSource::tint),
            Codec.FLOAT.optionalFieldOf("saturation", 0F).forGetter(RecolorSpriteSource::saturation),
            Codec.FLOAT.optionalFieldOf("contrast", 1F).forGetter(RecolorSpriteSource::contrast),
            Codec.FLOAT.optionalFieldOf("brightness", 0F).forGetter(RecolorSpriteSource::brightness)
    ).apply(i, RecolorSpriteSource::new));

    private static final FileToIdConverter TEXTURES = new FileToIdConverter("textures", ".png");
    public static SpriteSourceType TYPE;

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output) {
        for (Entry e : sprites) {
            ResourceLocation file = TEXTURES.idToFile(e.source());
            Optional<Resource> res = manager.getResource(file);
            if (res.isEmpty()) {
                CreativeEngineering.LOGGER.warn("recolor: missing source texture {}", file);
                continue;
            }
            LazyLoadedImage image = new LazyLoadedImage(file, res.get(), 1);
            output.add(e.target(), new Supplier(image, e.target(), e.fills(), this));
        }
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }

    /** Same maths as tools/stylekit preview: luma -> contrast/brightness -> mix back saturation -> multiply tint. */
    int recolor(int abgr) {
        int a = FastColor.ABGR32.alpha(abgr);
        if (a == 0) {
            return abgr;
        }
        float r = FastColor.ABGR32.red(abgr) / 255F, g = FastColor.ABGR32.green(abgr) / 255F, b = FastColor.ABGR32.blue(abgr) / 255F;
        float l = 0.299F * r + 0.587F * g + 0.114F * b;
        float l2 = Math.clamp((l - 0.5F) * contrast + 0.5F + brightness, 0F, 1F);
        float tr = ((tint >> 16) & 0xFF) / 255F, tg = ((tint >> 8) & 0xFF) / 255F, tb = (tint & 0xFF) / 255F;
        return FastColor.ABGR32.color(a, c((l2 * (1 - saturation) + b * saturation) * tb), c((l2 * (1 - saturation) + g * saturation) * tg),
                c((l2 * (1 - saturation) + r * saturation) * tr));
    }

    private static int c(float v) {
        return Math.clamp(Math.round(v * 255F), 0, 255);
    }

    private record Supplier(LazyLoadedImage image, ResourceLocation id, List<Fill> fills, RecolorSpriteSource source) implements SpriteSource.SpriteSupplier {
        @Override
        @Nullable
        public SpriteContents apply(SpriteResourceLoader loader) {
            try {
                NativeImage img = image.get().mappedCopy(source::recolor);
                for (Fill f : fills) {
                    f.apply(img);
                }
                return new SpriteContents(id, new FrameSize(img.getWidth(), img.getHeight()), img, ResourceMetadata.EMPTY);
            } catch (IOException | IllegalArgumentException ex) {
                CreativeEngineering.LOGGER.error("recolor: failed for {}", id, ex);
                return null;
            } finally {
                image.release();
            }
        }

        @Override
        public void discard() {
            image.release();
        }
    }
}
