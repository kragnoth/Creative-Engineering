package com.kragnoth.creativeengineering.client.engine;

import static com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity.PIVOT_X;
import static com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity.PIVOT_Y;
import static com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity.PIVOT_Z;

import com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import java.util.function.Consumer;

/** Instanced rocker arm (GPU, near-free at scale), the way Create animates its steam engine linkage. */
public class CylinderVisual extends AbstractBlockEntityVisual<DieselCylinderBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance rocker;
    private float lastAngle = Float.NaN;

    public CylinderVisual(VisualizationContext ctx, DieselCylinderBlockEntity be, float partialTick) {
        super(ctx, be, partialTick);
        rocker = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CEPartials.CYLINDER_ROCKER)).createInstance();
        animate(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
        float a = blockEntity.rockerAngle(pt);
        if (a == lastAngle) {
            return;   // stopped or frozen: no per-frame work
        }
        lastAngle = a;
        rocker.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotateYDegrees(DieselCylinderBlockEntity.yRot(blockState))
                .rotateXDegrees(45)
                .uncenter()
                .translate(PIVOT_X, PIVOT_Y, PIVOT_Z)
                .rotateZDegrees(a)
                .translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z)
                .setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rocker);
    }

    @Override
    protected void _delete() {
        rocker.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(rocker);
    }
}
