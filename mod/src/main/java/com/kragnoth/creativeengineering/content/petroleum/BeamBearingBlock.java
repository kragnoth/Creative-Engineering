package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Walking Beam Bearing: the saddle bearing that crowns a Pumpjack's column. Build the column yourself from Create metal
 * girders or industrial iron (2-6 high), put this on top; its block entity draws the walking beam, horsehead, pitman
 * arms and polished rod, sized to the rig it finds (PumpjackRig). FACING points along the machine, toward the well.
 * Wrench turns it like any Create block.
 */
public class BeamBearingBlock extends Block implements IBE<BeamBearingBlockEntity>, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 2, 15, 3, 14), Block.box(3, 3, 5, 13, 12, 11));

    public BeamBearingBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public BlockState getRotatedBlockState(BlockState state, Direction face) {
        return state.setValue(FACING, state.getValue(FACING).getClockWise());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public Class<BeamBearingBlockEntity> getBlockEntityClass() {
        return BeamBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeamBearingBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BEAM_BEARING.get();
    }
}
