package com.kragnoth.creativeengineering.content.cokeoven;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
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

    public CokeOvenBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
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
