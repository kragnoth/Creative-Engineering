package com.kragnoth.creativeengineering.content.engine;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Where a crankcase side can draw fuel from without a pump (Kragnoth, 2026-11-22): a container touching that side, or
 * every container at the far ends of a Create pipe run attached to it. The run is followed through pipes (and their
 * glass / encased forms) up to Create's pump range; a closed valve or a Create pump ends that branch (a pump's own line
 * is its business). Other crankcases are never sources. Fuel moves straight from source to reservoir. The run itself
 * is followed by the shared PipeRuns.
 */
public final class FuelLines {
    private FuelLines() {}

    public static List<IFluidHandler> sources(Level level, BlockPos crankcase, Direction side) {
        return com.kragnoth.creativeengineering.content.shared.PipeRuns.endpoints(level, crankcase, side,
                be -> be instanceof DieselEngineBlockEntity).stream().map(com.kragnoth.creativeengineering.content.shared.PipeRuns.Endpoint::handler).toList();
    }
}
