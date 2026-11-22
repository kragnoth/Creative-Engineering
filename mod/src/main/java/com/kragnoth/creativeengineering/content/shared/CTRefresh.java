package com.kragnoth.creativeengineering.content.shared;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connected textures are baked from each block's cached model data, and a block's look depends on its NEIGHBOURS'
 * multiblock membership too. So when a client block learns a new controller, refresh it AND its six neighbours and
 * mark them for re-render; otherwise the neighbours keep stale "not connected" data until some unrelated chunk
 * rebuild (the furnace seam delay Kragnoth saw, 2026-11-22).
 */
public final class CTRefresh {
    private CTRefresh() {}

    public static void onClientController(BlockEntity be, @Nullable BlockPos oldController, @Nullable BlockPos newController) {
        Level level = be.getLevel();
        if (level == null || !level.isClientSide || Objects.equals(oldController, newController)) {
            return;
        }
        refresh(level, be.getBlockPos());
        for (Direction d : Direction.values()) {
            refresh(level, be.getBlockPos().relative(d));
        }
    }

    private static void refresh(Level level, BlockPos pos) {
        BlockEntity n = level.getBlockEntity(pos);
        if (n != null) {
            n.requestModelDataUpdate();
        }
        BlockState s = level.getBlockState(pos);
        level.sendBlockUpdated(pos, s, s, Block.UPDATE_IMMEDIATE);
    }
}
