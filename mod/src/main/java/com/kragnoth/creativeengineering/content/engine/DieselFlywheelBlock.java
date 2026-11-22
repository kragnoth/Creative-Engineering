package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Cosmetic flywheel on a horizontal shaft. A plain Create kinetic block: spins with the shaft and passes it on. */
public class DieselFlywheelBlock extends HorizontalAxisKineticBlock implements IBE<FlywheelBlockEntity> {
    public DieselFlywheelBlock(Properties p) {
        super(p);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;   // drawn spinning by Create's KineticBlockEntityRenderer
    }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, BlockState newState, boolean moving) {
        BeltWheels.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public Class<FlywheelBlockEntity> getBlockEntityClass() {
        return FlywheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FlywheelBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DIESEL_FLYWHEEL.get();
    }
}
