package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import java.util.function.Consumer;

/** Crank Gearbox: Create's shaft through it and the counterweighted cranks, turning at a quarter of the shaft. */
public class PumpjackVisual extends SingleAxisRotatingVisual<PumpjackBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance crank;
    private float last = Float.NaN;

    public PumpjackVisual(VisualizationContext ctx, PumpjackBlockEntity be, float pt) {
        super(ctx, be, pt, Models.partial(AllPartialModels.SHAFT));
        crank = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CEPartials.PUMPJACK_CRANK)).createInstance();
        animate(pt);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
        float a = blockEntity.crankAngle(pt);
        if (a == last) {
            return;
        }
        last = a;
        PumpjackPoser.crank(crank.setIdentityTransform().translate(getVisualPosition()), blockEntity.facing(), a).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(crank);
    }

    @Override
    protected void _delete() {
        super._delete();
        crank.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(crank);
    }
}
