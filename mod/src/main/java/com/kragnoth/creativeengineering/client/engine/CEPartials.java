package com.kragnoth.creativeengineering.client.engine;

import com.kragnoth.creativeengineering.CreativeEngineering;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/** Moving parts drawn outside the block model (as Create's AllPartialModels). Created early so they get baked. */
public final class CEPartials {
    /** The flywheel model (built lying along X), spun by Create's rotating visual. */
    public static final PartialModel FLYWHEEL = PartialModel.of(CreativeEngineering.id("block/diesel_flywheel"));
    /** Crankshaft through the crankcase (built along X): shaft, crank webs and pin, end stubs. */
    public static final PartialModel CRANKSHAFT = PartialModel.of(CreativeEngineering.id("block/diesel_crankshaft"));
    /** Drive Pulley drum on its shaft (built along X). */
    public static final PartialModel DRIVE_PULLEY = PartialModel.of(CreativeEngineering.id("block/drive_pulley"));
    public static final PartialModel CYLINDER_ROCKER = PartialModel.of(CreativeEngineering.id("block/diesel_cylinder_rocker"));

    private CEPartials() {}

    public static void init() {
        // loading the class creates the partials
    }
}
