package com.kragnoth.creativeengineering.content.shared;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Follows a Create pipe run (Create's pipes, glass / encased forms, our steel pipes) from one face of a block to every
 * container at its far ends: up to Create's pump range; a closed valve or a pump ends that branch. Used by the diesel
 * engine's pump-less fuel lines and the Distilling Tank's taps. Nothing flows here: callers move fluid directly.
 */
public final class PipeRuns {
    private PipeRuns() {}

    /** A container at the end of a run: where the pipe enters it and its handler on that face. */
    public record Endpoint(BlockPos pos, IFluidHandler handler) {}

    public static List<Endpoint> endpoints(Level level, BlockPos from, Direction side, Predicate<BlockEntity> skip) {
        List<Endpoint> out = new ArrayList<>();
        BlockPos start = from.relative(side);
        FluidTransportBehaviour first = FluidPropagator.getPipe(level, start);
        if (first == null) {
            add(level, start, side.getOpposite(), skip, out);
            return out;
        }
        if (!first.canHaveFlowToward(level.getBlockState(start), side.getOpposite())) {
            return out;   // the pipe isn't connected to this face
        }
        int range = FluidPropagator.getPumpRange();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        ArrayDeque<Integer> dist = new ArrayDeque<>();
        queue.add(start);
        dist.add(1);
        seen.add(start);
        seen.add(from);
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
                    add(level, n, dir.getOpposite(), skip, out);
                }
            }
        }
        return out;
    }

    private static void add(Level level, BlockPos pos, Direction face, Predicate<BlockEntity> skip, List<Endpoint> out) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && skip.test(be)) {
            return;
        }
        IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, face);
        if (h != null && out.stream().noneMatch(e -> e.handler() == h)) {
            out.add(new Endpoint(pos, h));
        }
    }
}
