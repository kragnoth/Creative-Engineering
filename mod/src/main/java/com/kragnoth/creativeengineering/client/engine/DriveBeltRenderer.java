package com.kragnoth.creativeengineering.client.engine;

import com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Flywheel / Drive Pulley renderer. The spinning wheel itself is Create's job (instanced visual, or this renderer's
 * fallback when instancing is off). The big wheel's renderer also draws the Drive Belt every frame: the two straight
 * runs tangent to both wheels plus the wraps round each, in Create's belt texture. Cosmetic: it passes through blocks.
 */
public class DriveBeltRenderer extends KineticBlockEntityRenderer<BeltWheelBlockEntity> {
    private static final ResourceLocation BELT = ResourceLocation.fromNamespaceAndPath("create", "block/belt");
    private static final float HALF_WIDTH = 2.25F / 16, LIFT = 0.25F;

    public DriveBeltRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(BeltWheelBlockEntity be, BlockState state) {
        SuperByteBuffer buf = CachedBuffers.partial(be.isBig() ? CEPartials.FLYWHEEL : CEPartials.DRIVE_PULLEY, state);
        if (BeltWheelBlockEntity.axis(state) == Direction.Axis.Z) {
            buf.rotateCentered(AngleHelper.rad(90), Direction.UP);
        }
        return buf;
    }

    @Override
    protected void renderSafe(BeltWheelBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, pt, ms, buffer, light, overlay);   // skips itself when instancing draws the wheel
        if (!be.isBig() || !be.hasBelt() || be.partnerBE() == null) {
            return;
        }
        BlockPos partner = be.partner();
        float dy = (partner.getY() - be.getBlockPos().getY()) * 16F;   // pixels, pulley relative to flywheel
        float r1 = BeltWheelBlockEntity.BIG_RADIUS + LIFT, r2 = BeltWheelBlockEntity.SMALL_RADIUS + LIFT;
        List<float[]> loop = loop(r1, r2, dy);
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(BELT);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        boolean alongX = BeltWheelBlockEntity.axis(be.getBlockState()) == Direction.Axis.X;
        PoseStack.Pose pose = ms.last();
        float v = 0;
        for (int i = 0; i < loop.size(); i++) {
            float[] a = loop.get(i), b = loop.get((i + 1) % loop.size());
            float len = (float) Math.hypot(b[0] - a[0], b[1] - a[1]);
            float v0 = v % 16, v1 = v0 + Math.min(len, 16 - v0);
            v += len;
            float nw = (a[2] + b[2]) / 2, ny = (a[3] + b[3]) / 2;
            quad(vc, pose, alongX, a, b, nw, ny, sprite, v0 / 16, v1 / 16, light);
        }
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose, boolean alongX, float[] a, float[] b, float nw, float ny,
            TextureAtlasSprite s, float v0, float v1, int light) {
        float c = 0.5F;
        float[][] corners = {{a[0], a[1], c - HALF_WIDTH, s.getU0(), s.getV(v0)}, {a[0], a[1], c + HALF_WIDTH, s.getU1(), s.getV(v0)},
                {b[0], b[1], c + HALF_WIDTH, s.getU1(), s.getV(v1)}, {b[0], b[1], c - HALF_WIDTH, s.getU0(), s.getV(v1)}};
        for (float[] k : corners) {
            float w = 0.5F + k[0] / 16, y = 0.5F + k[1] / 16;
            float x = alongX ? k[2] : w, z = alongX ? w : k[2];
            vc.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(k[3], k[4]).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light).setNormal(pose, alongX ? 0 : nw, ny, alongX ? nw : 0);
        }
    }

    /**
     * The belt as a closed loop in the wheel plane (w = horizontal across the axis, y = up), in pixels from the
     * flywheel's centre: wrap round the flywheel's far side, straight run, wrap round the pulley's far side, straight
     * run back. Each point is {w, y, normal w, normal y}.
     */
    static List<float[]> loop(float r1, float r2, float dy) {
        float s = Math.signum(dy), ny = (r1 - r2) / dy, nw = (float) Math.sqrt(Math.max(0, 1 - ny * ny));
        double aPlus = Math.atan2(ny, nw), aMinus = Math.atan2(ny, -nw);
        double away = Math.atan2(-s, 0), toward = Math.atan2(s, 0);
        List<float[]> pts = new ArrayList<>();
        arc(pts, 0, 0, r1, aPlus, aMinus, away, 20);
        arc(pts, 0, dy, r2, aMinus, aPlus, toward, 8);
        return pts;
    }

    private static void arc(List<float[]> pts, float cw, float cy, float r, double from, double to, double via, int steps) {
        double span = norm(to - from), viaSpan = norm(via - from);
        if (viaSpan > span) {
            span -= 2 * Math.PI;   // go the other way round, through 'via'
        }
        for (int i = 0; i <= steps; i++) {
            double a = from + span * i / steps;
            float cos = (float) Math.cos(a), sin = (float) Math.sin(a);
            pts.add(new float[]{cw + r * cos, cy + r * sin, cos, sin});
        }
    }

    private static double norm(double a) {
        a %= 2 * Math.PI;
        return a < 0 ? a + 2 * Math.PI : a;
    }

    @Override
    public boolean shouldRenderOffScreen(BeltWheelBlockEntity be) {
        return be.hasBelt();
    }

    @Override
    public AABB getRenderBoundingBox(BeltWheelBlockEntity be) {
        AABB box = new AABB(be.getBlockPos()).inflate(1.5);
        return be.hasBelt() ? box.minmax(new AABB(be.partner()).inflate(1)) : box;
    }
}
