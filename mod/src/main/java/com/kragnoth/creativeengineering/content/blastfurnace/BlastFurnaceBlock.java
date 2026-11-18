package com.kragnoth.creativeengineering.content.blastfurnace;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
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

/** Blast Furnace Bricks: stack them and they form one furnace (see BlastFurnaceBlockEntity). Same hand use as the coke oven. */
public class BlastFurnaceBlock extends Block implements IBE<BlastFurnaceBlockEntity>, IWrenchable {
    /** True while the oven has heat. Drives the glow, and tells autocrafters not to add fuel (tarbound-compat T4). */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** True while something is actually coking (heated AND busy). Drives the smoke and crackle; no texture change. */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

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

    public BlastFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(WORKING, false)
                .setValue(Port.PROPERTY, Port.BRICKS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, WORKING, Port.PROPERTY);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() == state.getBlock() || isMoving) {
            return;
        }
        withBlockEntityDo(level, pos, BlastFurnaceBlockEntity::updateConnectivitySoon);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof BlastFurnaceBlockEntity be) {
                drop(level, pos, be.ownItems());
                drop(level, pos, be.workItems());
                level.removeBlockEntity(pos);
                ConnectivityHandler.splitMulti(be);
            }
        }
    }

    private static void drop(Level level, BlockPos pos, IItemHandler items) {
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items.getStackInSlot(i).copy());
        }
    }

    /**
     * Hand use, like Create's Millstone and Basin (Kragnoth, 0.2.3):
     * <ul>
     *   <li>iron or fuel in hand: puts the held stack in (iron first, else fuel);</li>
     *   <li>empty hand: takes all finished output; if there is none, the unstarted input; if none, what's mid-coking.</li>
     * </ul>
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlastFurnaceBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stack.isEmpty()) {
            IItemHandler view = be.automationItems();
            int slot = view.isItemValid(BlastFurnaceBlockEntity.IN, stack) ? BlastFurnaceBlockEntity.IN
                    : view.isItemValid(BlastFurnaceBlockEntity.FUEL, stack) ? BlastFurnaceBlockEntity.FUEL : -1;
            if (slot < 0) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                ItemStack rest = view.insertItem(slot, stack.copy(), false);
                if (!player.isCreative()) {
                    player.setItemInHand(hand, rest);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.3F, 0.8F);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        boolean took = false;
        for (int slot : new int[]{BlastFurnaceBlockEntity.METAL, BlastFurnaceBlockEntity.SLAG, BlastFurnaceBlockEntity.IN,
                BlastFurnaceBlockEntity.FUEL}) {
            took = takeAll(be, slot, player);
            if (took) {
                break;
            }
        }
        if (!took) {
            for (BlastFurnaceBlockEntity p : be.parts()) {
                took |= give(p.workItems(), player);
                p.resetProgress();
            }
        }
        if (took) {
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F + level.random.nextFloat());
        }
        return ItemInteractionResult.SUCCESS;
    }

    private static boolean takeAll(BlastFurnaceBlockEntity be, int slot, Player player) {
        boolean took = false;
        for (BlastFurnaceBlockEntity p : be.parts()) {
            ItemStack s = p.ownItems().getStackInSlot(slot);
            if (!s.isEmpty()) {
                player.getInventory().placeItemBackInInventory(s.copy());
                p.ownItems().setStackInSlot(slot, ItemStack.EMPTY);
                took = true;
            }
        }
        return took;
    }

    private static boolean give(net.neoforged.neoforge.items.ItemStackHandler items, Player player) {
        boolean took = false;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack s = items.getStackInSlot(i);
            if (!s.isEmpty()) {
                player.getInventory().placeItemBackInInventory(s.copy());
                items.setStackInSlot(i, ItemStack.EMPTY);
                took = true;
            }
        }
        return took;
    }

    /**
     * Client-only effects (no texture swap, the bricks keep their shape):
     * <ul>
     *   <li>working, top layer: campfire-style smoke wisps, a heavier puff every few seconds, a low crackle;</li>
     *   <li>heated, bottom layer: embers and the odd spark along the bottom edge (heat coming up from below).</li>
     * </ul>
     */
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

    /** Comparators read how full the metal output is. */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof BlastFurnaceBlockEntity be)) {
            return 0;
        }
        IItemHandler view = be.automationItems();
        ItemStack out = view.getStackInSlot(BlastFurnaceBlockEntity.METAL);
        if (out.isEmpty()) {
            return 0;
        }
        float fill = out.getCount() / (float) view.getSlotLimit(BlastFurnaceBlockEntity.METAL);
        return Math.max(1, Math.round(fill * 15));
    }

    @Override
    public Class<BlastFurnaceBlockEntity> getBlockEntityClass() {
        return BlastFurnaceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlastFurnaceBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BLAST_FURNACE.get();
    }
}
