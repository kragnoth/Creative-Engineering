package com.kragnoth.creativeengineering.content.distilling;

import com.kragnoth.creativeengineering.content.shared.PipeRuns;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;

/**
 * The tap's gravity feed: every few ticks it follows the pipe run from its front (PipeRuns) and moves up to RATE mB from
 * the level behind it (that level's drain-only handler) into the run's tanks that the pipe enters at the tap's height
 * or lower, shared out between them. Its front face shows an empty fluid handler so Create pipes connect to it.
 */
public class TapBlockEntity extends SmartBlockEntity {
    public static final int TICKS = 4, RATE = 50;   // 50 mB every 4 ticks: 250 mB/s per tap
    private static final int RESCAN = 40;
    private final List<PipeRuns.Endpoint> targets = new ArrayList<>();
    private int timer, rescan;

    public TapBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    /** What pipes see on the front: a connection point, nothing to pull or push (the tap does the moving). */
    public IFluidHandler frontHandler(Direction side) {
        return side == getBlockState().getValue(TapBlock.FACING) ? EmptyFluidHandler.INSTANCE : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || ++timer < TICKS) {
            return;
        }
        timer = 0;
        BlockState state = getBlockState();
        Direction out = state.getValue(TapBlock.FACING);
        boolean flowing = false;
        if (state.getValue(TapBlock.OPEN) && !state.getValue(TapBlock.POWERED)) {
            if (--rescan <= 0) {
                rescan = RESCAN / TICKS;
                targets.clear();
                for (PipeRuns.Endpoint e : PipeRuns.endpoints(level, worldPosition, out, be -> be == this)) {
                    if (e.pos().getY() <= worldPosition.getY()) {   // gravity: same height or lower
                        targets.add(e);
                    }
                }
            }
            IFluidHandler source = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(out.getOpposite()), out);
            if (source != null && !targets.isEmpty()) {
                flowing = feed(source);
            }
        }
        if (flowing != state.getValue(TapBlock.FLOWING)) {
            level.setBlock(worldPosition, state.setValue(TapBlock.FLOWING, flowing), Block.UPDATE_CLIENTS);
        }
    }

    private boolean feed(IFluidHandler source) {
        FluidStack have = source.drain(RATE, IFluidHandler.FluidAction.SIMULATE);
        if (have.isEmpty()) {
            return false;
        }
        int share = Math.max(1, have.getAmount() / targets.size()), moved = 0;
        for (PipeRuns.Endpoint e : targets) {
            if (e.handler() == source) {
                continue;
            }
            int fit = e.handler().fill(have.copyWithAmount(Math.min(share, have.getAmount() - moved)), IFluidHandler.FluidAction.SIMULATE);
            if (fit <= 0) {
                continue;
            }
            FluidStack taken = source.drain(have.copyWithAmount(fit), IFluidHandler.FluidAction.EXECUTE);
            moved += e.handler().fill(taken, IFluidHandler.FluidAction.EXECUTE);
            if (moved >= have.getAmount()) {
                break;
            }
        }
        return moved > 0;
    }
}
