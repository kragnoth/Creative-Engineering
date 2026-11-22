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
 * is its business). Other crankcases are never sources. Fuel moves straight from source to reservoir.
 */
public final class FuelLines {
    private FuelLines() {}

    public static List<IFluidHandler> sources(Level level, BlockPos crankcase, Direction side) {
        List<IFluidHandler> out = new ArrayList<>();
        BlockPos start = crankcase.relative(side);
        FluidTransportBehaviour first = FluidPropagator.getPipe(level, start);
        if (first == null) {
            addContainer(level, start, side.getOpposite(), out);
            return out;
        }
        if (!first.canHaveFlowToward(level.getBlockState(start), side.getOpposite())) {
            return out;   // the pipe isn't connected to the crankcase
        }
        int range = FluidPropagator.getPumpRange();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        ArrayDeque<Integer> dist = new ArrayDeque<>();
        queue.add(start);
        dist.add(1);
        seen.add(start);
        seen.add(crankcase);
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            int d = dist.poll();
            BlockState s = level.getBlockState(p);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, p);
            if (pipe == null || PumpBlock.isPump(s)) {
                continue;
            }
            if (s.getBlock() instanceof FluidValveBlock && !s.getValue(FluidValveBlock.ENABLED)) {
                continue;
            }
            for (Direction dir : FluidPropagator.getPipeConnections(s, pipe)) {
                BlockPos n = p.relative(dir);
                if (!seen.add(n) || !level.isLoaded(n)) {
                    continue;
                }
                FluidTransportBehaviour next = FluidPropagator.getPipe(level, n);
                if (next != null) {
                    if (d < range && next.canHaveFlowToward(level.getBlockState(n), dir.getOpposite())) {
                        queue.add(n);
                        dist.add(d + 1);
                    }
                } else {
                    addContainer(level, n, dir.getOpposite(), out);
                }
            }
        }
        return out;
    }

    private static void addContainer(Level level, BlockPos pos, Direction face, List<IFluidHandler> out) {
        if (level.getBlockEntity(pos) instanceof DieselEngineBlockEntity) {
            return;
        }
        IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, face);
        if (h != null && !out.contains(h)) {
            out.add(h);
        }
    }
}
