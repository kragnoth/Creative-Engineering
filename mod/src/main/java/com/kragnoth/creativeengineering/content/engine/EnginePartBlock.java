package com.kragnoth.creativeengineering.content.engine;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import com.mojang.serialization.MapCodec;

/**
 * Diesel engine parts, first pass (docs/engines-spec.md): shape and orientation only, so the look can be judged in
 * game. Crankcase and flywheel lie along a horizontal axis; cylinder, exhaust and starter face a horizontal direction
 * (a cylinder leans toward its facing). Engine behaviour comes after the look is approved. Wrench rotates.
 */
public final class EnginePartBlock {
    private EnginePartBlock() {}

    /** Crankcase, flywheel: horizontal axis (X or Z), taken from the player's facing. */
    public static class Axial extends Block implements IWrenchable {
        public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

        public Axial(Properties p) {
            super(p);
            registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(AXIS);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            return defaultBlockState().setValue(AXIS, ctx.getHorizontalDirection().getAxis());
        }
    }

    /** Cylinder, exhaust, starter: horizontal facing (a cylinder leans toward its facing). */
    public static class Facing extends HorizontalDirectionalBlock implements IWrenchable {
        public static final MapCodec<Facing> CODEC = simpleCodec(Facing::new);

        public Facing(Properties p) {
            super(p);
            registerDefaultState(defaultBlockState().setValue(FACING, Direction.SOUTH));
        }

        @Override
        protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
            return CODEC;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
        }
    }
}
