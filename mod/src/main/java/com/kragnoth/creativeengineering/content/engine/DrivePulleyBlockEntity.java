package com.kragnoth.creativeengineering.content.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Drive Pulley: the small end of a Drive Belt (4x the flywheel's speed). */
public class DrivePulleyBlockEntity extends BeltWheelBlockEntity {
    public DrivePulleyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isBig() {
        return false;
    }
}
