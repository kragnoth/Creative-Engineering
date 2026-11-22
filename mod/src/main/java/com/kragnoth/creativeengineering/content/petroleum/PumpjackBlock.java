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
 * Pumpjack, its Crank Gearbox (docs/petroleum-spec.md): one kinetic block that turns rotation into a crank stroke. A
 * Samson Post 1-2 blocks ahead (HORIZONTAL_FACING) carries the walking beam; the stroked block (a Wellhead) stands 1-2
 * blocks past the post (PumpjackRig). Each part is its own block; nothing forms. Rotation comes in on either side.
 * Oritech-style guidance, without assembly: right-click with an empty hand outlines where the parts go; right-click
 * with a Samson Post or Wellhead places it in the next spot.
 */
public class PumpjackBlock extends HorizontalKineticBlock implements IBE<PumpjackBlockEntity> {
    private static final VoxelShaper SHAPE = VoxelShaper.forHorizontal(Shapes.or(Block.box(0, 0, 0, 16, 2, 16),
            Block.box(4, 2, 3, 12, 14, 13)), Direction.SOUTH);

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

    /** Right-click with a Samson Post or a Wellhead: place it where the rig needs it next (1 block on, the short rig). */
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, net.minecraft.world.level.Level level,
                                                               BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
                                                               net.minecraft.world.phys.BlockHitResult hit) {
        boolean post = stack.is(com.kragnoth.creativeengineering.registry.ModItems.SAMSON_POST.get());
        boolean well = stack.is(com.kragnoth.creativeengineering.registry.ModItems.WELLHEAD.get());
        if (!post && !well) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        Direction f = state.getValue(HORIZONTAL_FACING);
        PumpjackRig rig = PumpjackRig.fromGearbox(level, pos, f);
        BlockPos at = post ? (rig == null ? pos.relative(f) : null) : (rig != null && rig.well() == null ? rig.post().relative(f) : null);
        if (at == null || !level.getBlockState(at).canBeReplaced() || (post && !level.getBlockState(at.above()).canBeReplaced())) {
            if (level.isClientSide) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(at == null
                        ? (post ? "creativeengineering.pumpjack.has_post" : rig == null ? "creativeengineering.pumpjack.post_first" : "creativeengineering.pumpjack.has_well")
                        : "creativeengineering.pumpjack.blocked"), true);
            }
            return net.minecraft.world.ItemInteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            if (post) {
                BlockState s = com.kragnoth.creativeengineering.registry.ModBlocks.SAMSON_POST.get().defaultBlockState().setValue(SamsonPostBlock.FACING, f);
                level.setBlock(at, s, Block.UPDATE_ALL);
                level.setBlock(at.above(), s.setValue(SamsonPostBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            } else {
                level.setBlock(at, com.kragnoth.creativeengineering.registry.ModBlocks.WELLHEAD.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            BlockState placed = level.getBlockState(at);
            level.playSound(null, at, placed.getSoundType().getPlaceSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1, 0.8F);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Empty hand: outline where the Samson Post and the Wellhead go (client only; nothing is placed or formed). */
    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
                                                                  net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (level.isClientSide) {
            com.kragnoth.creativeengineering.client.petroleum.PumpjackOutlines.show(level, pos, state.getValue(HORIZONTAL_FACING));
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
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
