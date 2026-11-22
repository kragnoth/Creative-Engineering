package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Pumpjack (docs/petroleum-spec.md): the gearbox end of a three-block nodding donkey. It is the block you place; the
 * Frame (samson post) and Wellhead follow along HORIZONTAL_FACING, away from the player. Rotation comes in on either
 * side of the gearbox (a shaft running alongside the machine), like the crank axle of a real pumpjack.
 */
public class PumpjackBlock extends HorizontalKineticBlock implements IBE<PumpjackBlockEntity> {
    private static final VoxelShaper SHAPE = VoxelShaper.forHorizontal(Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16), Block.box(4, 2, 3, 12, 13, 13)), Direction.SOUTH);

    public PumpjackBlock(Properties p) {
        super(p);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction f = ctx.getHorizontalDirection();
        BlockPos pos = ctx.getClickedPos();
        for (int i = 1; i <= 2; i++) {
            if (!ctx.getLevel().getBlockState(pos.relative(f, i)).canBeReplaced(ctx)) {
                if (ctx.getPlayer() != null && ctx.getLevel().isClientSide) {
                    ctx.getPlayer().displayClientMessage(Component.translatable("creativeengineering.pumpjack.blocked"), true);
                }
                return null;
            }
        }
        return defaultBlockState().setValue(HORIZONTAL_FACING, f);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        Direction f = state.getValue(HORIZONTAL_FACING);
        level.setBlock(pos.relative(f, 1), ModBlocks.PUMPJACK_FRAME.get().defaultBlockState().setValue(PumpjackPartBlock.FACING, f), Block.UPDATE_ALL);
        level.setBlock(pos.relative(f, 2), ModBlocks.PUMPJACK_WELLHEAD.get().defaultBlockState().setValue(PumpjackPartBlock.FACING, f), Block.UPDATE_ALL);
    }

    /** Takes the frame and wellhead with it (they drop nothing; the gearbox drops the Pumpjack). */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            Direction f = state.getValue(HORIZONTAL_FACING);
            for (int i = 1; i <= 2; i++) {
                BlockPos p = pos.relative(f, i);
                BlockState s = level.getBlockState(p);
                if (s.getBlock() instanceof PumpjackPartBlock part && part.offset == i && s.getValue(PumpjackPartBlock.FACING) == f) {
                    level.removeBlock(p, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE.get(state.getValue(HORIZONTAL_FACING));
    }

    /** The crank axle runs across the machine: shafts connect on the gearbox's two sides. */
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
