package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Fallback when Flywheel instancing is off: Create's shaft plus the linkage, drawn the classic way. */
public class PumpjackRenderer extends KineticBlockEntityRenderer<PumpjackBlockEntity> {
    public PumpjackRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected BlockState getRenderedBlockState(PumpjackBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    @Override
    protected void renderSafe(PumpjackBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        super.renderSafe(be, pt, ms, buffers, light, overlay);
        float a = be.crankAngle(pt);
        PumpjackBlockEntity.Pose p = PumpjackBlockEntity.pose(a);
        Direction f = be.facing();
        BlockState s = be.getBlockState();
        VertexConsumer vc = buffers.getBuffer(RenderType.solid());
        draw(PumpjackLinkage.beam(CachedBuffers.partial(CEPartials.PUMPJACK_BEAM, s), f, p), light, ms, vc);
        draw(PumpjackLinkage.crank(CachedBuffers.partial(CEPartials.PUMPJACK_CRANK, s), f, a), light, ms, vc);
        draw(PumpjackLinkage.pitman(CachedBuffers.partial(CEPartials.PUMPJACK_PITMAN, s), f, p, 0.5F), light, ms, vc);
        draw(PumpjackLinkage.pitman(CachedBuffers.partial(CEPartials.PUMPJACK_PITMAN, s), f, p, 15.5F), light, ms, vc);
        draw(PumpjackLinkage.rod(CachedBuffers.partial(CEPartials.PUMPJACK_ROD, s), f, p), light, ms, vc);
    }

    private static void draw(SuperByteBuffer b, int light, PoseStack ms, VertexConsumer vc) {
        b.light(light).renderInto(ms, vc);
    }

    @Override
    public boolean shouldRenderOffScreen(PumpjackBlockEntity be) {
        return true;
    }
}
