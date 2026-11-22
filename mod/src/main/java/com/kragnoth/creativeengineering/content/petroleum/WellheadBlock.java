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
 * steam engine without a boiler. Its four outlets are Create pipe size (8 px, centred), so a pipe on any side meets one square; the wellhead pushes
 * into them by itself.
 */
public class WellheadBlock extends Block implements IBE<WellheadBlockEntity>, IWrenchable {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 1, 15, 1, 15), Block.box(3, 1, 3, 13, 15, 13),
            Block.box(0, 4, 4, 16, 12, 12), Block.box(4, 4, 0, 12, 12, 16), Block.box(6, 15, 6, 10, 17, 10));

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
