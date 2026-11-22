package com.kragnoth.creativeengineering.client.distilling;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity;
import com.kragnoth.creativeengineering.content.distilling.Plumbing;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** The Distilling Tank's pipes, collars, gauge and valve as static instances: built once, rebuilt when the column reshapes. */
public class DistillingTankVisual extends AbstractBlockEntityVisual<DistillingTankBlockEntity> {
    private final List<TransformedInstance> parts = new ArrayList<>();

    public DistillingTankVisual(VisualizationContext ctx, DistillingTankBlockEntity be, float partialTick) {
        super(ctx, be, partialTick);
        build();
    }

    private void build() {
        parts.forEach(TransformedInstance::delete);
        parts.clear();
        for (Plumbing.Part p : blockEntity.greebles()) {
            var model = CEPartials.STILL.get(p.model());
            if (model == null) {
                continue;
            }
            TransformedInstance i = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(model)).createInstance();
            i.setIdentityTransform().translate(getVisualPosition()).center().rotateYDegrees(-p.yRot()).uncenter().setChanged();
            parts.add(i);
        }
        relight(parts.toArray(new TransformedInstance[0]));
    }

    @Override
    public void update(float partialTick) {
        build();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(parts.toArray(new TransformedInstance[0]));
    }

    @Override
    protected void _delete() {
        parts.forEach(TransformedInstance::delete);
        parts.clear();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        parts.forEach(consumer);
    }
}
