package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Drive Sprocket (id drive_pulley): Create's small cogwheel on a horizontal shaft; chain it to a flywheel for 4x speed. */
public class DrivePulleyBlock extends HorizontalAxisKineticBlock implements IBE<DrivePulleyBlockEntity> {
    private static final VoxelShape X = Block.box(5, 0, 0, 11, 16, 16), Z = Block.box(0, 0, 5, 16, 16, 11);   // the cog's disc

    public DrivePulleyBlock(Properties p) {
        super(p);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return BeltWheelBlockEntity.axis(state) == net.minecraft.core.Direction.Axis.X ? X : Z;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        BeltWheels.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public Class<DrivePulleyBlockEntity> getBlockEntityClass() {
        return DrivePulleyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DrivePulleyBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DRIVE_PULLEY.get();
    }
}
