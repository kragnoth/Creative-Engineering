package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.petroleum.BeamBearingBlockEntity;
import com.kragnoth.creativeengineering.content.petroleum.PumpjackRig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Fallback when Flywheel instancing is off: the bearing's beam and linkage, drawn the classic way. */
public class BeamBearingRenderer implements BlockEntityRenderer<BeamBearingBlockEntity> {
    public BeamBearingRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(BeamBearingBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        PumpjackRig rig = be.rig();
        PumpjackRig.Pose p = rig.pose(be.crankAngle(rig, pt));
        Direction f = be.facing();
        BlockState st = be.getBlockState();
        VertexConsumer vc = buffers.getBuffer(RenderType.solid());
        float s = rig.headScale();
        draw(PumpjackPoser.onBeam(part(CEPartials.PUMPJACK_SADDLE, st), f, p, 0, 0, 1), light, ms, vc);
        for (int i = 0; i < rig.a() + rig.b(); i++) {
            draw(PumpjackPoser.beamPiece(part(CEPartials.PUMPJACK_BEAM, st), f, p, -16 * rig.a() + 16 * i, rig.beamScale()), light, ms, vc);
        }
        draw(PumpjackPoser.onBeam(part(CEPartials.PUMPJACK_HORSEHEAD, st), f, p, -6 * s, 16 * rig.b(), s), light, ms, vc);
        draw(PumpjackPoser.onBeam(part(CEPartials.PUMPJACK_EQUALIZER, st), f, p, 0, -16 * rig.a(), 1), light, ms, vc);
        if (rig.gearbox() != null) {
            int n = PumpjackPoser.segments(p.pinY(), p.pinZ(), p.rearY(), p.rearZ());
            for (float x : PumpjackPoser.PITMAN_X) {
                for (int i = 0; i < n; i++) {
                    draw(PumpjackPoser.segment(part(CEPartials.PUMPJACK_PITMAN, st), f, x, p.pinY(), p.pinZ(), p.rearY(), p.rearZ(), i, n), light, ms, vc);
                }
            }
        }
        if (rig.well() != null) {
            float z = 8 + 16 * rig.b(), top = PumpjackPoser.rodTop(p);
            int n = PumpjackPoser.segments(p.rodBottom(), z, top, z);
            for (int i = 0; i < n; i++) {
                draw(PumpjackPoser.segment(part(CEPartials.PUMPJACK_ROD, st), f, 8, p.rodBottom(), z, top, z, i, n), light, ms, vc);
            }
            draw(PumpjackPoser.face(part(CEPartials.PUMPJACK_BRIDLE, st), f).translate(0, top * PumpjackPoser.P, z * PumpjackPoser.P), light, ms, vc);
        }
    }

    private static SuperByteBuffer part(PartialModel m, BlockState s) {
        return CachedBuffers.partial(m, s);
    }

    private static void draw(SuperByteBuffer b, int light, PoseStack ms, VertexConsumer vc) {
        b.light(light).renderInto(ms, vc);
    }

    @Override
    public boolean shouldRenderOffScreen(BeamBearingBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
