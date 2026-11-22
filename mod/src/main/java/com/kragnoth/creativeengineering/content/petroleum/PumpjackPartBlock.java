package com.kragnoth.creativeengineering.content.petroleum;

import javax.annotation.Nullable;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * The Pumpjack's Frame (offset 1) and Wellhead (offset 2), placed by the gearbox along its facing. Breaking either
 * breaks the whole machine (dropping one Pumpjack). The Wellhead is where the crude comes out: its fluid capability
 * is the gearbox's drain-only tank, so Create pipes and pumps connect to it.
 */
public class PumpjackPartBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public final int offset;
    private final VoxelShaper shape;

    public PumpjackPartBlock(Properties p, int offset, VoxelShape southShape) {
        super(p);
        this.offset = offset;
        this.shape = VoxelShaper.forHorizontal(southShape, Direction.SOUTH);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    public static BlockPos controllerPos(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING).getOpposite(), ((PumpjackPartBlock) state.getBlock()).offset);
    }

    @Nullable
    public static PumpjackBlockEntity controller(Level level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(controllerPos(pos, state)) instanceof PumpjackBlockEntity be ? be : null;
    }

    /** Capability provider for the Wellhead (registered without a block entity). */
    @Nullable
    public static IFluidHandler wellheadFluids(Level level, BlockPos pos, BlockState state) {
        PumpjackBlockEntity be = controller(level, pos, state);
        return be == null ? null : be.output();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockPos c = controllerPos(pos, state);
            BlockState cs = level.getBlockState(c);
            if (cs.getBlock() instanceof PumpjackBlock && cs.getValue(PumpjackBlock.HORIZONTAL_FACING) == state.getValue(FACING)) {
                level.removeBlock(c, false);   // takes the other part with it
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shape.get(state.getValue(FACING));
    }
}
