package com.kragnoth.creativeengineering.content.distilling;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

/**
 * Tap (Kragnoth, 2026-11-22): goes on the side of a Distilling Tank level (its back on the column, FACING = where the
 * pipe leaves). While OPEN and not powered it gravity-feeds that level's fluid down the attached pipe run into every
 * tank the pipe enters at the tap's height or lower: no pump (TapBlockEntity). Right-click opens / closes; redstone
 * closes. FLOWING drives the drips and the trickle.
 */
public class TapBlock extends HorizontalDirectionalBlock implements IBE<TapBlockEntity>, IWrenchable {
    public static final MapCodec<TapBlock> CODEC = simpleCodec(TapBlock::new);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN, POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty FLOWING = BooleanProperty.create("flowing");
    private static final VoxelShape[] SHAPES = new VoxelShape[4];

    static {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            VoxelShape body = Block.box(4, 4, 6, 12, 15, 16), spout = Block.box(5, 5, 0, 11, 11, 6);
            SHAPES[d.get2DDataValue()] = rotate(Shapes.or(body, spout), d);
        }
    }

    private static VoxelShape rotate(VoxelShape s, Direction to) {
        VoxelShape out = Shapes.empty();
        for (var box : s.toAabbs()) {
            double x0 = box.minX, z0 = box.minZ, x1 = box.maxX, z1 = box.maxZ;
            double[] r = switch (to) {   // built facing north
                case SOUTH -> new double[]{1 - x1, 1 - z1, 1 - x0, 1 - z0};
                case EAST -> new double[]{1 - z1, x0, 1 - z0, x1};
                case WEST -> new double[]{z0, 1 - x1, z1, 1 - x0};
                default -> new double[]{x0, z0, x1, z1};
            };
            out = Shapes.or(out, Shapes.box(r[0], box.minY, r[1], r[2], box.maxY, r[3]));
        }
        return out;
    }

    public TapBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(OPEN, true).setValue(POWERED, false).setValue(FLOWING, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, OPEN, POWERED, FLOWING);
    }

    /** Placed against the column: the pipe leaves away from the face clicked. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction d = ctx.getClickedFace().getAxis().isHorizontal() ? ctx.getClickedFace() : ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, d).setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean open = !state.getValue(OPEN);
            level.setBlock(pos, state.setValue(OPEN, open).setValue(FLOWING, false), Block.UPDATE_ALL);
            level.playSound(null, pos, open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 1.3F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FLOWING)) {
            return;
        }
        Direction f = state.getValue(FACING);
        if (random.nextInt(4) == 0) {   // a drip from the spout's lip
            level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + 0.5 + f.getStepX() * 0.3, pos.getY() + 0.28,
                    pos.getZ() + 0.5 + f.getStepZ() * 0.3, 0, 0, 0);
        }
        if (random.nextInt(30) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS,
                    0.08F, 1.4F + random.nextFloat() * 0.3F, false);
        }
    }

    @Override
    public Class<TapBlockEntity> getBlockEntityClass() {
        return TapBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TapBlockEntity> getBlockEntityType() {
        return ModBlockEntities.TAP.get();
    }
}
