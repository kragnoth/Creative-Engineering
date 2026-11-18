package com.kragnoth.creativeengineering.content.cokeoven;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
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

/** Coke Oven Bricks: place them next to each other and they form one oven (see CokeOvenBlockEntity). */
public class CokeOvenBlock extends Block implements IBE<CokeOvenBlockEntity>, IWrenchable {
    /** True while the oven has heat. Drives the glow, and tells autocrafters not to add fuel (tarbound-compat T4). */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** True while something is actually coking (heated AND busy). Drives the smoke and crackle; no texture change. */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public CokeOvenBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, WORKING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() == state.getBlock() || isMoving) {
            return;
        }
        withBlockEntityDo(level, pos, CokeOvenBlockEntity::updateConnectivitySoon);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be) {
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

    /** Comparators read how full the oven's output is. */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity be)) {
            return 0;
        }
        IItemHandler view = be.automationItems();
        ItemStack out = view.getStackInSlot(1);
        if (out.isEmpty()) {
            return 0;
        }
        float fill = out.getCount() / (float) view.getSlotLimit(1);
        return Math.max(1, Math.round(fill * 15));
    }

    @Override
    public Class<CokeOvenBlockEntity> getBlockEntityClass() {
        return CokeOvenBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CokeOvenBlockEntity> getBlockEntityType() {
        return ModBlockEntities.COKE_OVEN.get();
    }
}
