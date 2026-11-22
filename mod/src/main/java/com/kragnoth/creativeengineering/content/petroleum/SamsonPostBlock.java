package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Samson Post: the Pumpjack's two-block-tall trestle, placed like a door (lower + upper half). Its block entity draws
 * the walking beam, horsehead, pitman arms and polished rod, pivoting about 2.5 blocks up, driven by the Crank Gearbox
 * behind it (PumpjackRig). FACING points along the machine, gearbox -> well.
 */
public class SamsonPostBlock extends Block implements IBE<SamsonPostBlockEntity>, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape LOWER = Shapes.or(Block.box(1, 0, 1, 15, 1.5, 15), Block.box(3, 1.5, 4, 13, 16, 12));
    private static final VoxelShape UPPER = Block.box(3, 0, 4, 13, 16, 12);

    public SamsonPostBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.SOUTH).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, HALF);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        if (pos.getY() >= ctx.getLevel().getMaxBuildHeight() - 1 || !ctx.getLevel().getBlockState(pos.above()).canBeReplaced(ctx)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    /** Like a door: a half without its partner goes too (the lower half carries the loot). */
    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState other, LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (dir.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (dir == Direction.UP)) {
            return other.is(this) && other.getValue(HALF) != half ? state.setValue(FACING, other.getValue(FACING)) : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, dir, other, level, pos, otherPos);
    }

    /** Creative: breaking the top must not drop the bottom (vanilla's double-plant rule). */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            if (level.getBlockState(below).is(this)) {
                level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, below, Block.getId(level.getBlockState(below)));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Wrench: turn both halves together; sneak-wrench picks up the whole post. */
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? ctx.getClickedPos() : ctx.getClickedPos().below();
        BlockState l = level.getBlockState(lower);
        if (!l.is(this)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Direction f = l.getValue(FACING).getClockWise();
            level.setBlock(lower.above(), l.setValue(FACING, f).setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.setBlock(lower, l.setValue(FACING, f), Block.UPDATE_ALL);
            IWrenchable.playRotateSound(level, lower);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext ctx) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lower = ctx.getClickedPos().below();
            BlockState l = ctx.getLevel().getBlockState(lower);
            if (l.is(this)) {
                return IWrenchable.super.onSneakWrenched(l, new UseOnContext(ctx.getLevel(), ctx.getPlayer(), ctx.getHand(), ctx.getItemInHand(),
                        new net.minecraft.world.phys.BlockHitResult(ctx.getClickLocation().subtract(0, 1, 0), ctx.getClickedFace(), lower, false)));
            }
        }
        return IWrenchable.super.onSneakWrenched(state, ctx);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? IBE.super.newBlockEntity(pos, state) : null;
    }

    @Override
    public Class<SamsonPostBlockEntity> getBlockEntityClass() {
        return SamsonPostBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SamsonPostBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SAMSON_POST.get();
    }
}
