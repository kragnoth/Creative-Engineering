package com.kragnoth.creativeengineering.client.engine;

import com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Flywheel / Drive Sprocket renderer (0.11.2, Kragnoth: "the belts are a bit stretchy... use Create's chains?").
 * The wheels spin as instanced visuals (this renderer's super is the fallback when instancing is off). The flywheel end
 * also draws the Drive Chain every frame with Create's own chain-conveyor chain (ChainConveyorRenderer.renderChain:
 * vanilla chain texture on crossed strips, repeating, so it never stretches), laid along the loop round both wheels
 * and running at the wheels' surface speed. Cosmetic: it passes through blocks.
 */
public class DriveBeltRenderer extends KineticBlockEntityRenderer<BeltWheelBlockEntity> {
    public DriveBeltRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(BeltWheelBlockEntity be, BlockState state) {
        boolean x = BeltWheelBlockEntity.axis(state) == Direction.Axis.X;
        if (be.isBig()) {   // flywheel model is built along X
            SuperByteBuffer buf = CachedBuffers.partial(CEPartials.FLYWHEEL, state);
            return x ? buf : buf.rotateCentered(AngleHelper.rad(90), Direction.UP);
        }
        // sprocket = Create's small cogwheel, modelled upright (Y): lay it onto the block's axis
        SuperByteBuffer buf = CachedBuffers.partial(AllPartialModels.COGWHEEL, state);
        return x ? buf.rotateCentered(AngleHelper.rad(-90), Direction.SOUTH) : buf.rotateCentered(AngleHelper.rad(90), Direction.EAST);
    }

    @Override
    protected void renderSafe(BeltWheelBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, pt, ms, buffer, light, overlay);   // skips itself when instancing draws the wheel
        for (BlockPos pp : be.partners()) {
            BeltWheelBlockEntity other = be.partnerAt(pp);
            if (other == null || be.getBlockPos().compareTo(pp) > 0) {
                continue;   // each chain is drawn by one end: the lower position
            }
            BlockPos partner = other.getBlockPos();
            boolean alongX = BeltWheelBlockEntity.axis(be.getBlockState()) == Direction.Axis.X;
            // offset to the other wheel in the wheel plane (w across the axis, y up), pixels
            float ow = (alongX ? partner.getZ() - be.getBlockPos().getZ() : partner.getX() - be.getBlockPos().getX()) * 16F;
            float oy = (partner.getY() - be.getBlockPos().getY()) * 16F;
            float dist = (float) Math.hypot(ow, oy), uw = ow / dist, uy = oy / dist;
            float r1 = be.chainRadius();
            List<float[]> loop = loop(r1, other.chainRadius(), dist);
            for (float[] p : loop) {   // the loop is built with the other wheel straight "up": turn it to point at it
                float w = p[0], y = p[1];
                p[0] = uy * w + uw * y;
                p[1] = -uw * w + uy * y;
            }

            // chain travel (px along the loop) = this wheel's surface travel. Create turns a wheel by +angle about the
            // positive axis; in this plane that is clockwise for X and anticlockwise for Z, and the loop is walked clockwise
            // round this wheel (a rotation keeps that).
            double angle = AnimationTickHolder.getRenderTime(be.getLevel()) * be.getSpeed() * 3.0 / 10 / 180 * Math.PI;
            double sigma = alongX ? -1 : 1, walk = -1;
            double travel = sigma * walk * angle * r1;
            float shift = (float) (((travel % 16) + 16) % 16);

            float pos = 0;
            for (int i = 0; i < loop.size(); i++) {
                float[] a = loop.get(i), b = loop.get((i + 1) % loop.size());
                float len = (float) Math.hypot(b[0] - a[0], b[1] - a[1]);
                if (len < 1e-3F) {
                    continue;
                }
                Vector3f from = toBlock(a[0], a[1], alongX), to = toBlock(b[0], b[1], alongX);
                Vector3f dir = new Vector3f(to).sub(from).normalize();
                ms.pushPose();
                ms.translate(from.x, from.y, from.z);
                ms.mulPose(new Quaternionf().rotationTo(0, 1, 0, dir.x, dir.y, dir.z));
                ms.translate(-0.5, 0, -0.5);   // renderChain centres itself on (0.5, 0.5)
                ChainConveyorRenderer.renderChain(ms, buffer, (pos - shift) / 16F, len / 16F, light, light, false);
                ms.popPose();
                pos += len;
            }
        }
    }

    private static Vector3f toBlock(float w, float y, boolean alongX) {
        float bw = 0.5F + w / 16, by = 0.5F + y / 16;
        return alongX ? new Vector3f(0.5F, by, bw) : new Vector3f(bw, by, 0.5F);
    }

    /**
     * The chain as a closed loop in the wheel plane, in pixels from this wheel's centre, with the other wheel at
     * (0, dy) above it: wrap round this wheel's far side, straight run, wrap round the other's far side, straight run
     * back. Each point is {w, y, normal w, normal y}; the caller turns it toward the real other wheel.
     */
    static List<float[]> loop(float r1, float r2, float dy) {
        float s = Math.signum(dy), ny = (r1 - r2) / dy, nw = (float) Math.sqrt(Math.max(0, 1 - ny * ny));
        double aPlus = Math.atan2(ny, nw), aMinus = Math.atan2(ny, -nw);
        double away = Math.atan2(-s, 0), toward = Math.atan2(s, 0);
        List<float[]> pts = new ArrayList<>();
        arc(pts, 0, 0, r1, aPlus, aMinus, away, r1 > 12 ? 24 : 10);
        arc(pts, 0, dy, r2, aMinus, aPlus, toward, r2 > 12 ? 24 : 10);
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
        for (BlockPos p : be.partners()) {
            box = box.minmax(new AABB(p).inflate(1.5));
        }
        return box;
    }
}
