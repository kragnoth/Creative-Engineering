package com.kragnoth.creativeengineering.content.alloyfurnace;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.kragnoth.creativeengineering.content.shared.Port;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.items.IItemHandler;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Alloy Furnace Bricks: any solid box 2..4 per side forms one furnace (see AlloyFurnaceBlockEntity). */
public class AlloyFurnaceBlock extends Block implements IBE<AlloyFurnaceBlockEntity>, IWrenchable {
    /** True while the formed furnace has heat (tarbound-compat T4). */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** True while batches are running. Drives smoke, flame and sound; no texture change. */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public AlloyFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(WORKING, false)
                .setValue(Port.PROPERTY, Port.BRICKS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, WORKING, Port.PROPERTY);
        super.createBlockStateDefinition(builder);
    }

    /** Wrench: cycle the look of this brick (bricks, input hatch, output hatch). Sneak-wrench still picks it up. */
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            level.setBlock(context.getClickedPos(), state.setValue(Port.PROPERTY, state.getValue(Port.PROPERTY).next()), Block.UPDATE_ALL);
            IWrenchable.playRotateSound(level, context.getClickedPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            withBlockEntityDo(level, pos, AlloyFurnaceBlockEntity::requestRecheck);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        if (neighborBlock == this || level.getBlockState(neighborPos).is(this)) {
            withBlockEntityDo(level, pos, AlloyFurnaceBlockEntity::requestRecheck);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof AlloyFurnaceBlockEntity be) {
                be.contentsToDrop().forEach(s -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), s));
                level.removeBlockEntity(pos);
            }
        }
    }

    /**
     * Hand use like Create's Millstone: an ingredient in hand goes in; an empty hand takes the output, or
     * with no output the ingredients.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        AlloyFurnaceBlockEntity be = getBlockEntity(level, pos);
        if (be == null || !be.isFormed()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stack.isEmpty()) {
            if (ItemHandlerHelper.insertItem(be.automationItems(), stack.copy(), true).getCount() == stack.getCount()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                ItemStack rest = ItemHandlerHelper.insertItem(be.automationItems(), stack.copy(), false);
                if (!player.isCreative()) {
                    player.setItemInHand(hand, rest);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.3F, 0.8F);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (!level.isClientSide) {
            var taken = be.takeForHand();
            taken.forEach(s -> player.getInventory().placeItemBackInInventory(s));
            if (!taken.isEmpty()) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F + level.random.nextFloat());
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean top = !level.getBlockState(pos.above()).is(this);
        boolean bottom = !level.getBlockState(pos.below()).is(this);
        if (state.getValue(WORKING) && top) {
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.35 + random.nextDouble() * 0.3, pos.getY() + 1.02,
                        pos.getZ() + 0.35 + random.nextDouble() * 0.3, 0, 0.02, 0);
            }
            if (random.nextInt(3) == 0) {
                level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
                        pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                        0, 0.03 + random.nextDouble() * 0.02, 0);
            }
            if (random.nextInt(50) == 0) {
                for (int i = 0; i < 4; i++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
                            pos.getY() + 1.1, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6, 0, 0.06, 0);
                }
            }
            if (random.nextInt(80) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLASTFURNACE_FIRE_CRACKLE,
                        SoundSource.BLOCKS, 0.6F, 0.7F + random.nextFloat() * 0.2F, false);
            }
        }
        if (state.getValue(LIT) && bottom && random.nextInt(5) == 0) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            if (level.getBlockState(pos.relative(side)).is(this)) {
                return; // only on the oven's outside faces
            }
            double along = random.nextDouble();
            double x = pos.getX() + 0.5 + side.getStepX() * 0.52 + (side.getStepX() == 0 ? along - 0.5 : 0);
            double z = pos.getZ() + 0.5 + side.getStepZ() * 0.52 + (side.getStepZ() == 0 ? along - 0.5 : 0);
            level.addParticle(random.nextInt(4) == 0 ? ParticleTypes.LAVA : ParticleTypes.SMALL_FLAME,
                    x, pos.getY() + 0.05 + random.nextDouble() * 0.15, z, 0, 0.01, 0);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** Comparators read how full the output slots are. */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        AlloyFurnaceBlockEntity be = getBlockEntity(level, pos);
        if (be == null || !be.isFormed()) {
            return 0;
        }
        var view = be.automationItems();
        float fill = 0;
        for (int i = AlloyFurnaceBlockEntity.OUT_START; i < AlloyFurnaceBlockEntity.SLOTS; i++) {
            fill += view.getStackInSlot(i).getCount() / 64F;
        }
        fill /= AlloyFurnaceBlockEntity.SLOTS - AlloyFurnaceBlockEntity.OUT_START;
        return fill <= 0 ? 0 : 1 + Math.round(fill * 14);
    }

    @Override
    public Class<AlloyFurnaceBlockEntity> getBlockEntityClass() {
        return AlloyFurnaceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AlloyFurnaceBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ALLOY_FURNACE.get();
    }
}
