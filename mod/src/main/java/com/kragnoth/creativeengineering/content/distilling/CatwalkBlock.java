package com.kragnoth.creativeengineering.content.distilling;

import com.kragnoth.creativeengineering.registry.ModBlocks;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Catwalk (andesite / brass / copper): what Create metal scaffolding becomes when it stands beside a formed Distilling
 * Tank (Kragnoth, 2026-11-22: "scaffolding could be part of the build process"), like the platforms on IP's tower.
 * A thin grate deck with railings on every open edge (not toward the column or other catwalk); climbable and
 * passable from below like scaffolding (the deck only holds you up from above, sneak to go down). Breaking it gives
 * the scaffolding back. Spreads along scaffolding only next to a column.
 */
public class CatwalkBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north"), EAST = BooleanProperty.create("east"),
            SOUTH = BooleanProperty.create("south"), WEST = BooleanProperty.create("west");
    private static final VoxelShape DECK = Block.box(0, 0, 0, 16, 1.5, 16);
    private static final Map<Direction, VoxelShape> RAIL = Map.of(
            Direction.NORTH, Block.box(0, 0, 0, 16, 16, 1.5), Direction.SOUTH, Block.box(0, 0, 14.5, 16, 16, 16),
            Direction.WEST, Block.box(0, 0, 0, 1.5, 16, 16), Direction.EAST, Block.box(14.5, 0, 0, 16, 16, 16));

    public final String metal;

    public CatwalkBlock(String metal, Properties p) {
        super(p);
        this.metal = metal;
        registerDefaultState(defaultBlockState().setValue(NORTH, true).setValue(EAST, true).setValue(SOUTH, true).setValue(WEST, true));
    }

    public static BooleanProperty rail(Direction d) {
        return switch (d) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            default -> WEST;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(NORTH, EAST, SOUTH, WEST);
    }

    /** A railing wherever the side is open: not the column, not more catwalk, not a solid block. */
    static boolean railTowards(LevelReader level, BlockPos pos, Direction d) {
        BlockPos n = pos.relative(d);
        BlockState s = level.getBlockState(n);
        return !(s.getBlock() instanceof CatwalkBlock) && !s.is(ModBlocks.DISTILLING_TANK.get()) && !s.isFaceSturdy(level, n, d.getOpposite());
    }

    public BlockState withRails(LevelReader level, BlockPos pos) {
        BlockState s = defaultBlockState();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            s = s.setValue(rail(d), railTowards(level, pos, d));
        }
        return s;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return withRails(ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction d, BlockState other, LevelAccessor level, BlockPos pos, BlockPos from) {
        if (d.getAxis().isHorizontal()) {
            state = state.setValue(rail(d), railTowards(level, pos, d));
        }
        return state;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        if (!level.isClientSide && from.getY() == pos.getY()) {
            convertIfScaffolding(level, from);
        }
    }

    // ---- shapes: railings always; the deck only for someone standing on it (scaffolding-style)

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape s = DECK;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (state.getValue(rail(d))) {
                s = Shapes.or(s, RAIL.get(d));
            }
        }
        return s;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape s = Shapes.empty();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (state.getValue(rail(d))) {
                s = Shapes.or(s, RAIL.get(d));
            }
        }
        boolean standing = ctx.isAbove(DECK, pos, true) && !ctx.isDescending();
        return standing || !(ctx instanceof EntityCollisionContext) ? Shapes.or(s, DECK) : s;
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;   // climb up through the decks like scaffolding
    }

    // ---- scaffolding -> catwalk

    /** Turns Create metal scaffolding at pos into the matching catwalk if it stands next to (or diagonal to) a column. */
    public static void convertIfScaffolding(Level level, BlockPos pos) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
        if (!id.getNamespace().equals("create") || !id.getPath().endsWith("_scaffolding")) {
            return;
        }
        CatwalkBlock to = switch (id.getPath()) {
            case "andesite_scaffolding" -> ModBlocks.ANDESITE_CATWALK.get();
            case "brass_scaffolding" -> ModBlocks.BRASS_CATWALK.get();
            case "copper_scaffolding" -> ModBlocks.COPPER_CATWALK.get();
            default -> null;
        };
        if (to == null || !besideColumn(level, pos)) {
            return;
        }
        level.setBlock(pos, to.withRails(level, pos), Block.UPDATE_ALL);
    }

    private static boolean besideColumn(Level level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if ((dx != 0 || dz != 0) && level.getBlockEntity(pos.offset(dx, 0, dz)) instanceof DistillingTankBlockEntity t && t.formed()) {
                    return true;
                }
            }
        }
        return false;
    }
}
