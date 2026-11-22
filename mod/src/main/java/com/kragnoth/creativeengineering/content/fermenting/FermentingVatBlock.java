package com.kragnoth.creativeengineering.content.fermenting;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
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
 * Fermenting Vat block. TOP / BOTTOM / VALVE come from the formed vat (lid on the top layer, feet on the bottom, one
 * pressure-release valve top-middle); WORKING while a batch ferments drives the valve's occasional hiss and puff.
 * Hand use: buckets fill the input / empty the product (FluidUtil, like a tank); ingredients go in; empty hand takes
 * the ingredients back.
 */
public class FermentingVatBlock extends Block implements IBE<FermentingVatBlockEntity>, IWrenchable {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty VALVE = BooleanProperty.create("valve");
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public FermentingVatBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(TOP, true).setValue(BOTTOM, true).setValue(VALVE, true).setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(TOP, BOTTOM, VALVE, WORKING);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (old.getBlock() != state.getBlock()) {
            withBlockEntityDo(level, pos, FermentingVatBlockEntity::requestRecheck);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        if (neighbor == this || level.getBlockState(from).is(this)) {
            withBlockEntityDo(level, pos, FermentingVatBlockEntity::requestRecheck);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof FermentingVatBlockEntity be) {
                be.contentsToDrop().forEach(s -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), s));
            }
            level.removeBlockEntity(pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        FermentingVatBlockEntity be = getBlockEntity(level, pos);
        if (be == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (FluidUtil.getFluidHandler(stack).isPresent()) {
            return FluidUtil.interactWithFluidHandler(player, hand, be.automationFluids())
                    ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stack.isEmpty()) {
            if (be.automationItems().insertItem(0, stack, true).getCount() == stack.getCount()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                ItemStack rest = be.automationItems().insertItem(0, stack.copy(), false);
                if (!player.isCreative()) {
                    player.setItemInHand(hand, rest);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.3F, 0.8F);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (!level.isClientSide) {
            be.takeAllItems().forEach(s -> player.getInventory().placeItemBackInInventory(s));
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(WORKING) || !state.getValue(VALVE) || random.nextInt(8) != 0) {
            return;
        }
        // pressure-release valve: a small hiss and a white puff now and then
        double x = pos.getX() + 0.5, y = pos.getY() + 1.15, z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, 0, 0.04, 0);
        if (random.nextInt(3) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.06F, 1.6F + random.nextFloat() * 0.3F, false);
        }
    }

    @Override
    public Class<FermentingVatBlockEntity> getBlockEntityClass() {
        return FermentingVatBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FermentingVatBlockEntity> getBlockEntityType() {
        return ModBlockEntities.FERMENTING_VAT.get();
    }
}
