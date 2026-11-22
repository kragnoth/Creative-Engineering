package com.kragnoth.creativeengineering.client.distilling;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity;
import com.kragnoth.creativeengineering.content.distilling.Plumbing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/** Fallback when Flywheel instancing is off: draws the same dressing as DistillingTankVisual. */
public class DistillingTankRenderer extends SafeBlockEntityRenderer<DistillingTankBlockEntity> {
    public DistillingTankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(DistillingTankBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        for (Plumbing.Part p : be.greebles()) {
            var model = CEPartials.STILL.get(p.model());
            if (model != null) {
                CachedBuffers.partial(model, be.getBlockState()).rotateCentered(AngleHelper.rad(-p.yRot()), Direction.UP)
                        .light(light).renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            }
        }
    }
}
