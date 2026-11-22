package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wellhead (docs/petroleum-spec.md): a casing head on the ground that brings up what lies below (WellSources: crude
 * from an oil vein, water, lava in the Nether...) while a Pumpjack strokes it. On its own it does nothing, like a
 * steam engine without a boiler. Pipes take the fluid from any side; the wellhead pushes into them by itself.
 */
public class WellheadBlock extends Block implements IBE<WellheadBlockEntity>, IWrenchable {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 1, 14), Block.box(4, 1, 4, 12, 12, 12),
            Block.box(0, 5, 6, 16, 8, 10), Block.box(6, 5, 0, 10, 8, 16));

    public WellheadBlock(Properties p) {
        super(p);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public Class<WellheadBlockEntity> getBlockEntityClass() {
        return WellheadBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WellheadBlockEntity> getBlockEntityType() {
        return ModBlockEntities.WELLHEAD.get();
    }
}
