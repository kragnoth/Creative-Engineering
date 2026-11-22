package com.kragnoth.creativeengineering.content.storage;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Create's tank block entity under our type; exposes the handler Create keeps (Create registers it for its own type only). */
public class SteelTankBlockEntity extends FluidTankBlockEntity {
    public SteelTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IFluidHandler capability() {
        return fluidCapability;
    }
}
