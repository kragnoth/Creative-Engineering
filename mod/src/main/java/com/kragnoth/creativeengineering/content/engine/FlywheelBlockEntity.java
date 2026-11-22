package com.kragnoth.creativeengineering.content.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** The flywheel: the big end of a Drive Belt. */
public class FlywheelBlockEntity extends BeltWheelBlockEntity {
    public FlywheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isBig() {
        return true;
    }
}
