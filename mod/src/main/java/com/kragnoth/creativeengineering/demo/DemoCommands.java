package com.kragnoth.creativeengineering.demo;

import com.kragnoth.creativeengineering.content.heat.CampfireStandBlock;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.kragnoth.creativeengineering.registry.ModFluids;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Live demonstrations (Kragnoth, 2026-11-22): {@code /test rundistill} builds a complete, running Distilling Tank in
 * front of you, added under vanilla's {@code /test} (Brigadier merges the literal), op level 2.
 * <ul>
 *   <li>Input: a Create creative fluid tank of hooch -> pipe -> mechanical pump (powered by a cogwheel on a creative
 *       motor) -> the boiler.</li>
 *   <li>Column: 2x2x4 Distilling Tank on four lit copper Campfire Stands (heat x1).</li>
 *   <li>Outputs, no pumps: a Tap on level 1 feeds water along Steel Pipe into a Steel Storage Tank at its height; a Tap
 *       on level 2 drops vodka down Steel Pipe into a 2-high tank on the floor.</li>
 * </ul>
 */
public final class DemoCommands {
    private DemoCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("test").then(Commands.literal("rundistill")
                .requires(s -> s.hasPermission(2)).executes(DemoCommands::distiller)));
        event.getDispatcher().register(Commands.literal("test").then(Commands.literal("runrefinery")
                .requires(s -> s.hasPermission(2)).executes(DemoCommands::refinery)));
    }

    private static BlockState create(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", id)).defaultBlockState();
    }

    private static int distiller(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        Vec3 p = src.getPosition();
        Direction facing = src.getEntity() != null ? src.getEntity().getDirection() : Direction.NORTH;
        // origin: the build's (-2, 0, -1) corner, a few blocks in front of you
        BlockPos front = BlockPos.containing(p).relative(facing, 6);
        buildDistiller(level, front.offset(-2, 0, -2));
        src.sendSuccess(() -> Component.literal("Distilling Tank demo built: hooch is pumped into the boiler; taps on levels 1 and 2 "
                + "gravity-feed water and vodka through Steel Pipe into Steel Storage Tanks, no pumps. One batch every 10 s on campfire heat.")
                .withStyle(ChatFormatting.GOLD), true);
        return 1;
    }

    private static int refinery(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        Direction facing = src.getEntity() != null ? src.getEntity().getDirection() : Direction.NORTH;
        BlockPos front = BlockPos.containing(src.getPosition()).relative(facing, 6);
        buildRefinery(level, front.offset(-2, 0, -2));
        src.sendSuccess(() -> Component.literal("Refinery demo built: a Pumpjack on a creative motor draws crude (vein forced to 60 mB/s "
                + "under its well until restart) and pushes it up Steel Pipe into a Distilling Tank on a creative Blaze Burner; taps "
                + "drain diesel, kerosene and gasoline into Steel Storage Tanks. Goggles on the gearbox show the vein.")
                .withStyle(ChatFormatting.GOLD), true);
        return 1;
    }

    /** Storage tanks (tops, from the origin) for the refinery demo's diesel, kerosene and gasoline levels. */
    public static final BlockPos DIESEL_TANK = new BlockPos(5, 2, 2), KEROSENE_TANK = new BlockPos(3, 3, 0), GASOLINE_TANK = new BlockPos(3, 4, 4);

    /**
     * Pumpjack -> Distilling Tank -> three storage tanks, origin o. Crank Gearbox (0,0,0) facing south with a creative
     * motor east of it, Samson Post (0,0..1,1), its horsehead over the Wellhead (0,0,2); Steel Pipe from the wellhead's
     * east side up into the boiler of a 1x1x4 column at (3,1..4,2) on a creative Blaze Burner. Taps on the three levels
     * (east, north, south) feed tall Steel Storage Tanks standing on the floor.
     */
    public static void buildRefinery(ServerLevel level, BlockPos o) {
        for (int x = -2; x <= 7; x++) {
            for (int z = -2; z <= 6; z++) {
                level.setBlock(o.offset(x, -1, z), Blocks.SMOOTH_SANDSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                for (int y = 0; y <= 6; y++) {
                    level.setBlock(o.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        set(level, o, ModBlocks.PUMPJACK.get().defaultBlockState().setValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING, Direction.SOUTH));
        BlockState post = ModBlocks.SAMSON_POST.get().defaultBlockState().setValue(com.kragnoth.creativeengineering.content.petroleum.SamsonPostBlock.FACING, Direction.SOUTH);
        set(level, o.offset(0, 0, 1), post);
        set(level, o.offset(0, 1, 1), post.setValue(com.kragnoth.creativeengineering.content.petroleum.SamsonPostBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        set(level, o.offset(0, 0, 2), ModBlocks.WELLHEAD.get().defaultBlockState());
        set(level, o.offset(1, 0, 0), create("creative_motor").setValue(BlockStateProperties.FACING, Direction.WEST));
        com.kragnoth.creativeengineering.content.petroleum.OilVeins.force(new net.minecraft.world.level.ChunkPos(o.offset(0, 0, 2)), 60);
        // crude line: wellhead east -> up one -> boiler's west face
        set(level, o.offset(1, 0, 2), steelPipe(Direction.WEST, Direction.EAST));
        set(level, o.offset(2, 0, 2), steelPipe(Direction.WEST, Direction.UP));
        set(level, o.offset(2, 1, 2), steelPipe(Direction.DOWN, Direction.EAST));
        // column on a creative (always lit) Blaze Burner: crude needs heat 2
        set(level, o.offset(3, 0, 2), create("blaze_burner").setValue(com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL,
                com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED));
        if (level.getBlockEntity(o.offset(3, 0, 2)) instanceof com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity b) {
            b.isCreative = true;
            b.setChanged();
        }
        for (int y = 1; y <= 4; y++) {
            set(level, o.offset(3, y, 2), ModBlocks.DISTILLING_TANK.get().defaultBlockState());
        }
        // taps: diesel (level 1) east, kerosene (2) north, gasoline (3) south, each straight into a floor-standing tank
        set(level, o.offset(4, 2, 2), tap(Direction.EAST));
        set(level, o.offset(3, 3, 1), tap(Direction.NORTH));
        set(level, o.offset(3, 4, 3), tap(Direction.SOUTH));
        for (BlockPos top : new BlockPos[] {DIESEL_TANK, KEROSENE_TANK, GASOLINE_TANK}) {
            for (int y = 0; y <= top.getY(); y++) {
                set(level, o.offset(top.getX(), y, top.getZ()), ModBlocks.STEEL_TANK.get().defaultBlockState());
            }
        }
        sign(level, o.offset(-1, 0, 2), Direction.WEST, "Wellhead", "crude out");
        sign(level, o.offset(-1, 0, 0), Direction.WEST, "Pumpjack", "crank gearbox");
        sign(level, o.offset(-1, 0, 1), Direction.WEST, "Samson Post", "walking beam");
        sign(level, o.offset(6, 0, 2), Direction.EAST, "Diesel", "level 1");
        sign(level, o.offset(3, 0, -1), Direction.NORTH, "Kerosene", "level 2");
        sign(level, o.offset(3, 0, 5), Direction.SOUTH, "Gasoline", "level 3");
    }

    /** Positions (from the origin) of the storage tanks the demo's levels 1 and 2 fill. */
    public static final BlockPos WATER_TANK = new BlockPos(6, 2, 2), VODKA_TANK = new BlockPos(2, 2, 6);

    /** Builds the whole demo with o as its origin; also used by the game test that keeps it honest. */
    public static void buildDistiller(ServerLevel level, BlockPos o) {

        // clear a 9 x 8 x 8 space on a smooth stone floor
        for (int x = -3; x <= 8; x++) {
            for (int z = -2; z <= 8; z++) {
                level.setBlock(o.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                for (int y = 0; y <= 7; y++) {
                    level.setBlock(o.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        // heat: four lit copper Campfire Stands
        for (int x = 2; x <= 3; x++) {
            for (int z = 2; z <= 3; z++) {
                set(level, o.offset(x, 0, z), ModBlocks.COPPER_CAMPFIRE_STAND.get().defaultBlockState()
                        .setValue(CampfireStandBlock.FIRE, CampfireStandBlock.Fire.CAMPFIRE).setValue(CampfireStandBlock.LIT, true));
            }
        }
        // the column: 2 x 2, 4 tall (boiler, water level, vodka level, spare top level)
        for (int y = 1; y <= 4; y++) {
            for (int x = 2; x <= 3; x++) {
                for (int z = 2; z <= 3; z++) {
                    set(level, o.offset(x, y, z), ModBlocks.DISTILLING_TANK.get().defaultBlockState());
                }
            }
        }
        // input: creative hooch tank -> pipe -> pump (east) -> boiler; cogwheel above the pump on a creative motor
        set(level, o.offset(-1, 1, 2), create("creative_fluid_tank"));
        if (level.getBlockEntity(o.offset(-1, 1, 2)) instanceof CreativeFluidTankBlockEntity t
                && t.getTankInventory() instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank c) {
            c.setContainedFluid(new FluidStack(ModFluids.HOOCH.source.get(), 1000));
        }
        BlockState pipe = create("fluid_pipe");
        for (var prop : new net.minecraft.world.level.block.state.properties.BooleanProperty[]{BlockStateProperties.NORTH,
                BlockStateProperties.SOUTH, BlockStateProperties.UP, BlockStateProperties.DOWN}) {
            pipe = pipe.setValue(prop, false);
        }
        set(level, o.offset(0, 1, 2), pipe.setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.WEST, true));
        set(level, o.offset(1, 1, 2), create("mechanical_pump").setValue(BlockStateProperties.FACING, Direction.EAST));
        set(level, o.offset(1, 2, 2), create("cogwheel").setValue(BlockStateProperties.AXIS, Direction.Axis.X));
        set(level, o.offset(0, 2, 2), create("creative_motor").setValue(BlockStateProperties.FACING, Direction.EAST));
        // outputs, no pumps: a Tap on each level gravity-feeds a Steel Storage Tank through Steel Pipe
        //   level 1 (water): tap east, a straight run to a tank at the same height
        set(level, o.offset(4, 2, 2), tap(Direction.EAST));
        set(level, o.offset(5, 2, 2), steelPipe(Direction.WEST, Direction.EAST));
        set(level, o.offset(WATER_TANK), ModBlocks.STEEL_TANK.get().defaultBlockState());
        //   level 2 (vodka): tap south, the pipe drops two blocks to a 2-high tank on the floor
        set(level, o.offset(2, 3, 4), tap(Direction.SOUTH));
        set(level, o.offset(2, 3, 5), steelPipe(Direction.NORTH, Direction.DOWN));
        set(level, o.offset(2, 2, 5), steelPipe(Direction.UP, Direction.SOUTH));
        set(level, o.offset(VODKA_TANK), ModBlocks.STEEL_TANK.get().defaultBlockState());
        set(level, o.offset(VODKA_TANK).below(), ModBlocks.STEEL_TANK.get().defaultBlockState());   // 2 high: they merge like Create's
        // labels
        sign(level, o.offset(-2, 1, 2), Direction.WEST, "Hooch in", "(creative tank)");
        sign(level, o.offset(1, 1, 1), Direction.NORTH, "Pump", "into the boiler");
        sign(level, o.offset(7, 2, 2), Direction.EAST, "Level 1 tap", "water");
        sign(level, o.offset(2, 2, 7), Direction.SOUTH, "Level 2 tap", "vodka");
    }

    private static BlockState tap(Direction facing) {
        return ModBlocks.TAP.get().defaultBlockState().setValue(com.kragnoth.creativeengineering.content.distilling.TapBlock.FACING, facing);
    }

    private static BlockState steelPipe(Direction... open) {
        BlockState s = ModBlocks.STEEL_PIPE.get().defaultBlockState();
        for (Direction d : Direction.values()) {
            s = s.setValue(net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(d), java.util.List.of(open).contains(d));
        }
        return s;
    }

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private static void sign(ServerLevel level, BlockPos pos, Direction facing, String l1, String l2) {
        set(level, pos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing));
        if (level.getBlockEntity(pos) instanceof SignBlockEntity s) {
            SignText t = new SignText().setMessage(1, Component.literal(l1).withStyle(ChatFormatting.BOLD)).setMessage(2, Component.literal(l2));
            s.setText(t, true);
            s.setChanged();
            level.sendBlockUpdated(pos, s.getBlockState(), s.getBlockState(), Block.UPDATE_ALL);
        }
    }
}
