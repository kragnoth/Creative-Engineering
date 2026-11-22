package com.kragnoth.creativeengineering.client.petroleum;

import com.kragnoth.creativeengineering.client.engine.CEPartials;
import com.kragnoth.creativeengineering.content.petroleum.PumpjackRig;
import com.kragnoth.creativeengineering.content.petroleum.SamsonPostBlockEntity;
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
 * Samson Post: the walking beam (one 16 px piece per block of rear arm + reach), saddle, horsehead, equalizer, the two
 * pitman arms down to the gearbox's crank pins, and the polished rod into the stroked block, all posed from the rig.
 */
public class SamsonPostVisual extends AbstractBlockEntityVisual<SamsonPostBlockEntity> implements SimpleDynamicVisual {
    static final int MAX_BEAM = 4;
    private final TransformedInstance saddle, horsehead, equalizer, rod;
    private final TransformedInstance[] beam = new TransformedInstance[MAX_BEAM], pitman = new TransformedInstance[2];
    private final List<TransformedInstance> all = new ArrayList<>();

    public SamsonPostVisual(VisualizationContext ctx, SamsonPostBlockEntity be, float pt) {
        super(ctx, be, pt);
        saddle = make(CEPartials.PUMPJACK_SADDLE);
        horsehead = make(CEPartials.PUMPJACK_HORSEHEAD);
        equalizer = make(CEPartials.PUMPJACK_EQUALIZER);
        rod = make(CEPartials.PUMPJACK_ROD);
        for (int i = 0; i < MAX_BEAM; i++) {
            beam[i] = make(CEPartials.PUMPJACK_BEAM);
        }
        pitman[0] = make(CEPartials.PUMPJACK_PITMAN);
        pitman[1] = make(CEPartials.PUMPJACK_PITMAN);
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
        PumpjackPoser.onBeam(at(saddle), f, p, 0, 0).setChanged();
        for (int i = 0; i < MAX_BEAM; i++) {
            if (i < rig.a() + rig.b()) {
                PumpjackPoser.onBeam(at(beam[i]), f, p, 0, -16 * rig.a() + 16 * i).setChanged();
            } else {
                beam[i].setZeroTransform().setChanged();
            }
        }
        PumpjackPoser.onBeam(at(horsehead), f, p, PumpjackRig.LIP_Y, 16 * rig.b()).setChanged();
        PumpjackPoser.onBeam(at(equalizer), f, p, 0, -16 * rig.a()).setChanged();
        for (int i = 0; i < 2; i++) {
            if (rig.gearbox() != null) {
                PumpjackPoser.pitman(at(pitman[i]), f, p, PumpjackPoser.PITMAN_X[i]).setChanged();
            } else {
                pitman[i].setZeroTransform().setChanged();
            }
        }
        if (rig.well() != null) {
            PumpjackPoser.rod(at(rod), f, p, rig.b()).setChanged();
        } else {
            rod.setZeroTransform().setChanged();
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
        all.forEach(consumer);
    }
}
