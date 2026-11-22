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
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import java.util.function.Consumer;
import net.minecraft.core.Direction;

/** Instanced Pumpjack: Create's shaft through the gearbox, plus the nodding beam, cranks, pitman arms and rod. */
public class PumpjackVisual extends SingleAxisRotatingVisual<PumpjackBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance beam, crank, pitmanL, pitmanR, rod;
    private float last = Float.NaN;
    private boolean lastTarget;

    public PumpjackVisual(VisualizationContext ctx, PumpjackBlockEntity be, float pt) {
        super(ctx, be, pt, Models.partial(AllPartialModels.SHAFT));
        beam = make(CEPartials.PUMPJACK_BEAM);
        crank = make(CEPartials.PUMPJACK_CRANK);
        pitmanL = make(CEPartials.PUMPJACK_PITMAN);
        pitmanR = make(CEPartials.PUMPJACK_PITMAN);
        rod = make(CEPartials.PUMPJACK_ROD);
        animate(pt);
    }

    private TransformedInstance make(PartialModel m) {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(m)).createInstance();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
        float a = blockEntity.crankAngle(pt);
        boolean hasTarget = blockEntity.target() != null;
        if (a == last && hasTarget == lastTarget) {
            return;
        }
        last = a;
        lastTarget = hasTarget;
        Direction f = blockEntity.facing();
        PumpjackBlockEntity.Pose p = PumpjackBlockEntity.pose(a);
        PumpjackLinkage.beam(beam.setIdentityTransform().translate(getVisualPosition()), f, p).setChanged();
        PumpjackLinkage.crank(crank.setIdentityTransform().translate(getVisualPosition()), f, a).setChanged();
        PumpjackLinkage.pitman(pitmanL.setIdentityTransform().translate(getVisualPosition()), f, p, PumpjackLinkage.PITMAN_X[0]).setChanged();
        PumpjackLinkage.pitman(pitmanR.setIdentityTransform().translate(getVisualPosition()), f, p, PumpjackLinkage.PITMAN_X[1]).setChanged();
        PumpjackLinkage.rod(rod.setIdentityTransform().translate(getVisualPosition()), f, p).setChanged();
        if (!hasTarget) {
            rod.setZeroTransform().setChanged();   // no wellhead: the rod is not fitted
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(beam, crank, pitmanL, pitmanR, rod);
    }

    @Override
    protected void _delete() {
        super._delete();
        beam.delete();
        crank.delete();
        pitmanL.delete();
        pitmanR.delete();
        rod.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(beam);
        consumer.accept(crank);
    }
}
