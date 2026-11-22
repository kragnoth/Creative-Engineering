package com.kragnoth.creativeengineering.client.ct;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * Fermenting Vat "big picture" textures (Kragnoth, 2026-11-22: like a CTM resource pack / Create's tanks). Every
 * face size has its own picture (walls 1..3 x 1..4, lid 1..3 x 1..3) in fermenting_vat_big / fermenting_vat_lid_big;
 * each block's wall and lid quads are remapped onto its own 16x16 slice of the picture for its vat's size.
 * Wall cell (W, H) sits at ((W-1)*48, (H-1)*64) in the 256x256 sheet; lid cell (W, D) at ((W-1)*48, (D-1)*48).
 * Only quads using the vat's placeholder sprites are touched (the valve and feet keep their Create textures).
 */
public class VatModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<Slot> SLOT = new ModelProperty<>();
    private static final ResourceLocation WALL = CreativeEngineering.id("block/fermenting_vat"),
            LID = CreativeEngineering.id("block/fermenting_vat_lid"),
            WALL_BIG = CreativeEngineering.id("block/fermenting_vat_big"),
            LID_BIG = CreativeEngineering.id("block/fermenting_vat_lid_big");

    /** Where this block sits in its vat: relative x / y / z and the vat's size. */
    private record Slot(int x, int y, int z, int w, int h, int d) {}

    public VatModel(BakedModel original) {
        super(original);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        data = super.getModelData(level, pos, state, data);
        if (level.getBlockEntity(pos) instanceof FermentingVatBlockEntity be && be.controllerPos() != null) {
            BlockPos rel = pos.subtract(be.controllerPos());
            int[] s = be.clientSize();
            if (rel.getX() >= 0 && rel.getY() >= 0 && rel.getZ() >= 0 && rel.getX() < s[0] && rel.getY() < s[1] && rel.getZ() < s[2]) {
                return data.derive().with(SLOT, new Slot(rel.getX(), rel.getY(), rel.getZ(), s[0], s[1], s[2])).build();
            }
        }
        return data;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
            @Nullable RenderType type) {
        List<BakedQuad> quads = super.getQuads(state, side, rand, data, type);
        Slot slot = data.get(SLOT);
        if (slot == null) {
            slot = new Slot(0, 0, 0, 1, 1, 1);
        }
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite wall = atlas.getSprite(WALL), lid = atlas.getSprite(LID);
        List<BakedQuad> out = new ArrayList<>(quads.size());
        for (BakedQuad q : quads) {
            Direction f = q.getDirection();
            if (q.getSprite() == wall && f.getAxis().isHorizontal()) {
                // column counted from the viewer's left looking at the face; row from the top of the vat
                int col, width;
                switch (f) {
                    case NORTH -> { col = slot.w - 1 - slot.x; width = slot.w; }
                    case SOUTH -> { col = slot.x; width = slot.w; }
                    case EAST -> { col = slot.d - 1 - slot.z; width = slot.d; }
                    default -> { col = slot.z; width = slot.d; }
                }
                out.add(remap(q, wall, atlas.getSprite(WALL_BIG), (width - 1) * 48 + col * 16, (slot.h - 1) * 64 + (slot.h - 1 - slot.y) * 16));
            } else if (q.getSprite() == lid && f == Direction.UP) {
                out.add(remap(q, lid, atlas.getSprite(LID_BIG), (slot.w - 1) * 48 + slot.x * 16, (slot.d - 1) * 48 + slot.z * 16));
            } else {
                out.add(q);
            }
        }
        return out;
    }

    /** Moves the quad's UVs from the 16x16 placeholder onto a 16x16 window at (px, py) of the 256x256 sheet. */
    private static BakedQuad remap(BakedQuad q, TextureAtlasSprite from, TextureAtlasSprite to, int px, int py) {
        int[] v = q.getVertices().clone();
        for (int i = 0; i < 4; i++) {
            float fu = (BakedQuadHelper.getU(v, i) - from.getU0()) / (from.getU1() - from.getU0());
            float fv = (BakedQuadHelper.getV(v, i) - from.getV0()) / (from.getV1() - from.getV0());
            BakedQuadHelper.setU(v, i, to.getU((px + fu * 16) / 256F));
            BakedQuadHelper.setV(v, i, to.getV((py + fv * 16) / 256F));
        }
        return new BakedQuad(v, q.getTintIndex(), q.getDirection(), to, q.isShade(), q.hasAmbientOcclusion());
    }
}
