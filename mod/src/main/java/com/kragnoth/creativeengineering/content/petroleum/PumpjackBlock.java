package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Pumpjack (docs/petroleum-spec.md): one kinetic block that turns rotation into an up-and-down stroke, like Create's
 * steam engine in reverse. Skid, reduction gearbox and samson post are its model; the walking beam overhangs the block
 * in front (HORIZONTAL_FACING) and its horsehead strokes whatever is there, if it is StrokeDriven (a Wellhead).
 * Rotation comes in on either side: the crank axle runs across the machine. Wrench: turn / pick up, like any Create block.
 */
public class PumpjackBlock extends HorizontalKineticBlock implements IBE<PumpjackBlockEntity> {
    private static final VoxelShaper SHAPE = VoxelShaper.forHorizontal(Shapes.or(Block.box(0, 0, 0, 16, 2, 16),
            Block.box(4, 2, 4, 12, 13, 12), Block.box(0, 2, 13, 2, 16, 16), Block.box(14, 2, 13, 16, 16, 16)), Direction.SOUTH);

    public PumpjackBlock(Properties p) {
        super(p);
    }

    /** Facing away from the player: the beam reaches over the block beyond, like placing a Deployer. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction f = ctx.getHorizontalDirection();
        if (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()) {
            f = f.getOpposite();
        }
        return defaultBlockState().setValue(HORIZONTAL_FACING, f);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Class<PumpjackBlockEntity> getBlockEntityClass() {
        return PumpjackBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpjackBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PUMPJACK.get();
    }
}
