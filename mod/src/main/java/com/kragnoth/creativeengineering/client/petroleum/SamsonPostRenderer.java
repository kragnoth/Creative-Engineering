package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.petroleum.PumpjackRig;
import com.kragnoth.creativeengineering.content.petroleum.SamsonPostBlockEntity;
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

/** Fallback when Flywheel instancing is off: the Samson Post's beam and linkage, drawn the classic way. */
public class SamsonPostRenderer implements BlockEntityRenderer<SamsonPostBlockEntity> {
    public SamsonPostRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(SamsonPostBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        PumpjackRig rig = be.rig();
        PumpjackRig.Pose p = rig.pose(be.crankAngle(rig, pt));
        Direction f = be.facing();
        BlockState s = be.getBlockState();
        VertexConsumer vc = buffers.getBuffer(RenderType.solid());
        draw(PumpjackPoser.onBeam(part(CEPartials.PUMPJACK_SADDLE, s), f, p, 0, 0), light, ms, vc);
        for (int i = 0; i < rig.a() + rig.b(); i++) {
            draw(PumpjackPoser.onBeam(part(CEPartials.PUMPJACK_BEAM, s), f, p, 0, -16 * rig.a() + 16 * i), light, ms, vc);
        }
        draw(PumpjackPoser.onBeam(part(CEPartials.PUMPJACK_HORSEHEAD, s), f, p, PumpjackRig.LIP_Y, 16 * rig.b()), light, ms, vc);
        draw(PumpjackPoser.onBeam(part(CEPartials.PUMPJACK_EQUALIZER, s), f, p, 0, -16 * rig.a()), light, ms, vc);
        if (rig.gearbox() != null) {
            for (float x : PumpjackPoser.PITMAN_X) {
                draw(PumpjackPoser.pitman(part(CEPartials.PUMPJACK_PITMAN, s), f, p, x), light, ms, vc);
            }
        }
        if (rig.well() != null) {
            draw(PumpjackPoser.rod(part(CEPartials.PUMPJACK_ROD, s), f, p, rig.b()), light, ms, vc);
        }
    }

    private static SuperByteBuffer part(PartialModel m, BlockState s) {
        return CachedBuffers.partial(m, s);
    }

    private static void draw(SuperByteBuffer b, int light, PoseStack ms, VertexConsumer vc) {
        b.light(light).renderInto(ms, vc);
    }

    @Override
    public boolean shouldRenderOffScreen(SamsonPostBlockEntity be) {
        return true;
    }
}
