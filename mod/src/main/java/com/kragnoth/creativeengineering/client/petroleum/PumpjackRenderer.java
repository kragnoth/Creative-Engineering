package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/** Fallback when Flywheel instancing is off: Create's shaft plus the cranks. */
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
        PumpjackPoser.crank(CachedBuffers.partial(CEPartials.PUMPJACK_CRANK, be.getBlockState()), be.facing(), be.crankAngle(pt),
                be.rig() == null ? 4 : be.rig().crankR())
                .light(light).renderInto(ms, buffers.getBuffer(RenderType.solid()));
    }
}
