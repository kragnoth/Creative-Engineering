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
 * player-built column (girders / industrial iron) 1-3 blocks ahead (HORIZONTAL_FACING) carries the Walking Beam
 * Bearing; the stroked block (a Wellhead) stands 1-6 blocks past the column (PumpjackRig). Each part is its own block;
 * nothing forms. Rotation comes in on either side. Oritech-style guidance, without assembly: right-click with an empty
 * hand outlines where the parts go; right-click with a bearing or Wellhead places it in the next spot.
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

    /**
     * Right-click with a Walking Beam Bearing or a Wellhead: place it where the rig needs it next. The bearing goes on
     * top of a 2-6 high column built 1 block on; the Wellhead goes 1 block past the column (the short rig).
     */
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, net.minecraft.world.level.Level level,
                                                               BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
                                                               net.minecraft.world.phys.BlockHitResult hit) {
        boolean bearing = stack.is(com.kragnoth.creativeengineering.registry.ModItems.BEAM_BEARING.get());
        boolean well = stack.is(com.kragnoth.creativeengineering.registry.ModItems.WELLHEAD.get());
        if (!bearing && !well) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        Direction f = state.getValue(HORIZONTAL_FACING);
        PumpjackRig rig = PumpjackRig.fromGearbox(level, pos, f);
        BlockPos at = null;
        String why;
        if (bearing) {
            int h = PumpjackRig.column(level, pos.relative(f));
            why = rig != null ? "creativeengineering.pumpjack.has_post" : h < PumpjackRig.MIN_H ? "creativeengineering.pumpjack.no_column" : null;
            if (why == null) {
                at = pos.relative(f).above(h);
            }
        } else {
            why = rig == null ? "creativeengineering.pumpjack.post_first" : rig.well() != null ? "creativeengineering.pumpjack.has_well" : null;
            if (why == null) {
                at = rig.base().relative(f);
            }
        }
        if (at != null && !level.getBlockState(at).canBeReplaced()) {
            at = null;
            why = "creativeengineering.pumpjack.blocked";
        }
        if (at == null) {
            if (level.isClientSide) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(why), true);
            }
            return net.minecraft.world.ItemInteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            level.setBlock(at, bearing ? com.kragnoth.creativeengineering.registry.ModBlocks.BEAM_BEARING.get().defaultBlockState().setValue(BeamBearingBlock.FACING, f)
                    : com.kragnoth.creativeengineering.registry.ModBlocks.WELLHEAD.get().defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, at, level.getBlockState(at).getSoundType().getPlaceSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1, 0.8F);
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
