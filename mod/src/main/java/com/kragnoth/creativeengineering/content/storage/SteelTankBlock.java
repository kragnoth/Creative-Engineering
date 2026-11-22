package com.kragnoth.creativeengineering.content.storage;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Steel Storage Tank (Kragnoth, 2026-11-22: "work in the extra storage tanks as a new tank type"): Create's own fluid
 * tank (multiblock forming, windows, fluid rendering, wrench, boilers) under its own block entity type, re-skinned in
 * the Distilling Tank's steel by the load-time recolour. Pairs with the column's pump-free outlets.
 */
public class SteelTankBlock extends FluidTankBlock {
    public SteelTankBlock(Properties p) {
        super(p, false);
    }

    @Override
    public Class<FluidTankBlockEntity> getBlockEntityClass() {
        return FluidTankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
        return ModBlockEntities.STEEL_TANK.get();
    }
}
