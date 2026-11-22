package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.petroleum.BeamBearingBlockEntity;
import com.kragnoth.creativeengineering.content.petroleum.PumpjackRig;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction;

/**
 * Walking Beam Bearing: the beam (one piece per block of rear arm + reach), saddle, horsehead (scaled with the reach),
 * equalizer, the two pitman arms down to the gearbox's crank pins and the polished rod into the well, posed each frame
 * from the rig. Instances beyond what the rig needs are parked (zero transform).
 */
public class BeamBearingVisual extends AbstractBlockEntityVisual<BeamBearingBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance saddle, horsehead, equalizer, bridle;
    private final TransformedInstance[] beam = new TransformedInstance[PumpjackRig.MAX_A + PumpjackRig.MAX_B];
    private final TransformedInstance[][] pitman = new TransformedInstance[2][PumpjackPoser.MAX_SEGMENTS];
    private final TransformedInstance[] rod = new TransformedInstance[PumpjackPoser.MAX_SEGMENTS];
    private final List<TransformedInstance> all = new ArrayList<>();

    public BeamBearingVisual(VisualizationContext ctx, BeamBearingBlockEntity be, float pt) {
        super(ctx, be, pt);
        saddle = make(CEPartials.PUMPJACK_SADDLE);
        horsehead = make(CEPartials.PUMPJACK_HORSEHEAD);
        equalizer = make(CEPartials.PUMPJACK_EQUALIZER);
        bridle = make(CEPartials.PUMPJACK_BRIDLE);
        for (int i = 0; i < beam.length; i++) {
            beam[i] = make(CEPartials.PUMPJACK_BEAM);
        }
        for (int i = 0; i < PumpjackPoser.MAX_SEGMENTS; i++) {
            pitman[0][i] = make(CEPartials.PUMPJACK_PITMAN);
            pitman[1][i] = make(CEPartials.PUMPJACK_PITMAN);
            rod[i] = make(CEPartials.PUMPJACK_ROD);
        }
        animate(pt);
    }

    private TransformedInstance make(PartialModel m) {
        TransformedInstance i = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(m)).createInstance();
        all.add(i);
        return i;
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
        PumpjackRig rig = blockEntity.rig();
        PumpjackRig.Pose p = rig.pose(blockEntity.crankAngle(rig, pt));
        Direction f = blockEntity.facing();
        float s = rig.headScale();
        PumpjackPoser.onBeam(at(saddle), f, p, 0, 0, 1).setChanged();
        for (int i = 0; i < beam.length; i++) {
            if (i < rig.a() + rig.b()) {
                PumpjackPoser.beamPiece(at(beam[i]), f, p, -16 * rig.a() + 16 * i, rig.beamScale()).setChanged();
            } else {
                beam[i].setZeroTransform().setChanged();
            }
        }
        PumpjackPoser.onBeam(at(horsehead), f, p, -6 * s, 16 * rig.b(), s).setChanged();
        PumpjackPoser.onBeam(at(equalizer), f, p, 0, -16 * rig.a(), 1).setChanged();
        for (int side = 0; side < 2; side++) {
            int n = rig.gearbox() == null ? 0 : PumpjackPoser.segments(p.pinY(), p.pinZ(), p.rearY(), p.rearZ());
            for (int i = 0; i < PumpjackPoser.MAX_SEGMENTS; i++) {
                if (i < n) {
                    PumpjackPoser.segment(at(pitman[side][i]), f, PumpjackPoser.PITMAN_X[side], p.pinY(), p.pinZ(), p.rearY(), p.rearZ(), i, n).setChanged();
                } else {
                    pitman[side][i].setZeroTransform().setChanged();
                }
            }
        }
        float z = 8 + 16 * rig.b(), top = PumpjackPoser.rodTop(p);
        int n = rig.well() == null ? 0 : PumpjackPoser.segments(p.rodBottom(), z, top, z);
        for (int i = 0; i < PumpjackPoser.MAX_SEGMENTS; i++) {
            if (i < n) {
                PumpjackPoser.segment(at(rod[i]), f, 8, p.rodBottom(), z, top, z, i, n).setChanged();
            } else {
                rod[i].setZeroTransform().setChanged();
            }
        }
        if (rig.well() != null) {
            PumpjackPoser.face(at(bridle), f).translate(0, top * PumpjackPoser.P, z * PumpjackPoser.P).setChanged();
        } else {
            bridle.setZeroTransform().setChanged();
        }
    }

    private TransformedInstance at(TransformedInstance i) {
        return i.setIdentityTransform().translate(getVisualPosition());
    }

    @Override
    public void updateLight(float partialTick) {
        relight(all.toArray(new TransformedInstance[0]));
    }

    @Override
    protected void _delete() {
        all.forEach(TransformedInstance::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(saddle);
    }
}
