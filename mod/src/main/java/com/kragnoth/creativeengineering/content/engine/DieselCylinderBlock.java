package com.kragnoth.creativeengineering.content.engine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

/**
 * A diesel cylinder: sits diagonally above a crankcase and leans toward its FACING (away from the crankshaft). The
 * engine sets RUNNING while it fires; that only drives client effects: occasional vanilla campfire smoke off the
 * head and a soft thump (no custom particles, no server cost).
 */
public class DieselCylinderBlock extends HorizontalDirectionalBlock implements IWrenchable {
    public static final MapCodec<DieselCylinderBlock> CODEC = simpleCodec(DieselCylinderBlock::new);
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    public DieselCylinderBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.SOUTH).setValue(RUNNING, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, RUNNING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(RUNNING)) {
            return;
        }
        // the head sits ~13.5 px out along the 45 degree axis: up and toward FACING from the block centre
        Direction f = state.getValue(FACING);
        double x = pos.getX() + 0.5 + f.getStepX() * 0.55, y = pos.getY() + 1.05, z = pos.getZ() + 0.5 + f.getStepZ() * 0.55;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, f.getStepX() * 0.005, 0.03, f.getStepZ() * 0.005);
        }
        if (random.nextInt(6) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.05F, 0.5F + random.nextFloat() * 0.2F, false);
        }
    }
}
