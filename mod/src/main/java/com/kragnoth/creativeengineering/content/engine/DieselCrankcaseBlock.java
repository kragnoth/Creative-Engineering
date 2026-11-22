package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Diesel crankcase. FACING points at the output end (away from the player who places it). Crankcases in a line with
 * the same facing are one engine: the crankshaft connects between them and out of the back. The front face has NO
 * shaft: it is the read-only starter port (the engine only checks whether the block there turns), so a hand crank
 * never joins the running engine's network.
 */
public class DieselCrankcaseBlock extends HorizontalKineticBlock implements IBE<DieselEngineBlockEntity> {
    public DieselCrankcaseBlock(Properties p) {
        super(p);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(HORIZONTAL_FACING, ctx.getHorizontalDirection());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction out = state.getValue(HORIZONTAL_FACING);
        if (face == out) {
            return true;
        }
        if (face == out.getOpposite()) {   // only into another crankcase of the same engine
            BlockState behind = world.getBlockState(pos.relative(face));
            return behind.getBlock() == this && behind.getValue(HORIZONTAL_FACING) == out;
        }
        return false;
    }

    @Override
    public Class<DieselEngineBlockEntity> getBlockEntityClass() {
        return DieselEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DieselEngineBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DIESEL_ENGINE.get();
    }
}
