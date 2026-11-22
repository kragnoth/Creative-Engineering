package com.kragnoth.creativeengineering.client.engine;

import static com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity.PIVOT_X;
import static com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity.PIVOT_Y;
import static com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity.PIVOT_Z;

import com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Fallback when Flywheel instancing is off: draws the rocker the classic way. Skips itself when the visual is active. */
public class CylinderRenderer implements BlockEntityRenderer<DieselCylinderBlockEntity> {
    public CylinderRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(DieselCylinderBlockEntity be, float pt, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        CachedBuffers.partial(CEPartials.CYLINDER_ROCKER, be.getBlockState())
                .center()
                .rotateYDegrees(DieselCylinderBlockEntity.yRot(be.getBlockState()))
                .rotateXDegrees(45)
                .uncenter()
                .translate(PIVOT_X, PIVOT_Y, PIVOT_Z)
                .rotateZDegrees(be.rockerAngle(pt))
                .translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z)
                .light(light)
                .renderInto(ms, buffers.getBuffer(RenderType.solid()));
    }
}
