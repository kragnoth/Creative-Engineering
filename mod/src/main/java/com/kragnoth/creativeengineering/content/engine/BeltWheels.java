package com.kragnoth.creativeengineering.content.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Shared block hooks for the belt wheels. */
final class BeltWheels {
    private BeltWheels() {}

    /** Breaking (or replacing) a belted wheel drops the belt and frees the other wheel. */
    static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof BeltWheelBlockEntity w && w.hasBelt()) {
            w.unlink(true, true);
        }
    }
}
