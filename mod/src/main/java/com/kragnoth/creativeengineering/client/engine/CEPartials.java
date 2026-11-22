package com.kragnoth.creativeengineering.client.engine;

import com.kragnoth.creativeengineering.CreativeEngineering;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/** Moving parts drawn outside the block model (as Create's AllPartialModels). Created early so they get baked. */
public final class CEPartials {
    /** The flywheel model (built lying along X), spun by Create's rotating visual. */
    public static final PartialModel FLYWHEEL = PartialModel.of(CreativeEngineering.id("block/diesel_flywheel"));
    /** Crankshaft through the crankcase (built along X): shaft, crank webs and pin, end stubs. */
    public static final PartialModel CRANKSHAFT = PartialModel.of(CreativeEngineering.id("block/diesel_crankshaft"));
    /** Distilling Tank dressing (content/distilling/Plumbing part names -> models). */
    public static final java.util.Map<String, PartialModel> STILL = java.util.stream.Stream.of(
            "distilling_tank_pipe_left", "distilling_tank_pipe_left_top", "distilling_tank_pipe_left_boiler",
            "distilling_tank_pipe_right", "distilling_tank_pipe_right_top", "distilling_tank_pipe_right_boiler",
            "distilling_tank_pipe_band", "distilling_tank_collar", "distilling_tank_gauge", "distilling_tank_valve", "distilling_tank_outlet")
            .collect(java.util.stream.Collectors.toMap(n -> n, n -> PartialModel.of(CreativeEngineering.id("block/" + n))));
    public static final PartialModel CYLINDER_ROCKER = PartialModel.of(CreativeEngineering.id("block/diesel_cylinder_rocker"));

    /** Pumpjack linkage (docs/petroleum-spec.md): cranks (gearbox); on the Walking Beam Bearing the beam pieces, saddle,
     *  horsehead, equalizer, pitman and rod segments (16 px each, repeated) and the bridle. */
    public static final PartialModel PUMPJACK_CRANK = PartialModel.of(CreativeEngineering.id("block/pumpjack_crank"));
    public static final PartialModel PUMPJACK_BEAM = PartialModel.of(CreativeEngineering.id("block/pumpjack_beam"));
    public static final PartialModel PUMPJACK_HORSEHEAD = PartialModel.of(CreativeEngineering.id("block/pumpjack_horsehead"));
    public static final PartialModel PUMPJACK_EQUALIZER = PartialModel.of(CreativeEngineering.id("block/pumpjack_equalizer"));
    public static final PartialModel PUMPJACK_SADDLE = PartialModel.of(CreativeEngineering.id("block/pumpjack_saddle"));
    public static final PartialModel PUMPJACK_PITMAN = PartialModel.of(CreativeEngineering.id("block/pumpjack_pitman"));
    public static final PartialModel PUMPJACK_ROD = PartialModel.of(CreativeEngineering.id("block/pumpjack_rod"));
    public static final PartialModel PUMPJACK_BRIDLE = PartialModel.of(CreativeEngineering.id("block/pumpjack_bridle"));

    private CEPartials() {}

    public static void init() {
        // loading the class creates the partials
    }
}
