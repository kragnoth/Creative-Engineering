package com.kragnoth.creativeengineering.content.distilling;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * Distilling Tank block: the vat's steel Create tank, connected like one. Its dressing (pipe runs, collars, gauge,
 * relief valve with gooseneck) is drawn by the client from the formed column (Plumbing), so the block keeps just
 * VALVE (vapour puffs, top-middle) and WORKING. Buckets
 * fill the boiler / take a fraction from the level clicked.
 */
public class DistillingTankBlock extends Block implements IBE<DistillingTankBlockEntity>, IWrenchable {
    public static final BooleanProperty VALVE = BooleanProperty.create("valve");
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public DistillingTankBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(VALVE, false).setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(VALVE, WORKING);
    }

    /** Walls between column blocks never draw. */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction dir) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, dir);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (old.getBlock() != state.getBlock()) {
            withBlockEntityDo(level, pos, DistillingTankBlockEntity::requestRecheck);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        if (neighbor == this || level.getBlockState(from).is(this)) {
            withBlockEntityDo(level, pos, DistillingTankBlockEntity::requestRecheck);
        }
        if (!level.isClientSide) {
            withBlockEntityDo(level, pos, DistillingTankBlockEntity::refreshOutlets);   // a tank put against a level: fit a coupling
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof DistillingTankBlockEntity be) {
                be.contentsToDrop().forEach(s -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), s));
            }
            level.removeBlockEntity(pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        DistillingTankBlockEntity be = getBlockEntity(level, pos);
        if (be == null || FluidUtil.getFluidHandler(stack).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, be.automationFluids())
                ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(WORKING) || !state.getValue(VALVE) || random.nextInt(7) != 0) {
            return;
        }
        // vapour from the gooseneck's mouth now and then
        double x = pos.getX() + 0.5 + 0.34, y = pos.getY() + 1.28, z = pos.getZ() + 0.5;   // gooseneck mouth
        level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, 0.01, 0.03, 0);
        if (random.nextInt(3) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS,
                    0.08F, 0.7F + random.nextFloat() * 0.2F, false);
        }
    }

    @Override
    public Class<DistillingTankBlockEntity> getBlockEntityClass() {
        return DistillingTankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DistillingTankBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DISTILLING_TANK.get();
    }
}
