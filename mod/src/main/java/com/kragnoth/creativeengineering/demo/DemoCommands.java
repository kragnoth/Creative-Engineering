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
 *   <li>Outputs, no pumps: a Steel Storage Tank against level 1 fills with water, a 2-high one against level 2 with vodka;
 *       each join gets the column's bolted coupling.</li>
 * </ul>
 */
public final class DemoCommands {
    private DemoCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("test").then(Commands.literal("rundistill")
                .requires(s -> s.hasPermission(2)).executes(DemoCommands::distiller)));
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
        src.sendSuccess(() -> Component.literal("Distilling Tank demo built: hooch is pumped into the boiler; water fills the tank on "
                + "level 1 and vodka the tank on level 2, no output pumps. One batch every 10 s on campfire heat.").withStyle(ChatFormatting.GOLD), true);
        return 1;
    }

    /** Positions (from the origin) of the storage tanks the demo's levels 1 and 2 fill. */
    public static final BlockPos WATER_TANK = new BlockPos(4, 2, 2), VODKA_TANK = new BlockPos(2, 3, 4);

    /** Builds the whole demo with o as its origin; also used by the game test that keeps it honest. */
    public static void buildDistiller(ServerLevel level, BlockPos o) {

        // clear a 9 x 8 x 8 space on a smooth stone floor
        for (int x = -3; x <= 6; x++) {
            for (int z = -2; z <= 6; z++) {
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
        // outputs, no pumps: storage tanks touching level 1 (east side) and level 2 (south side)
        set(level, o.offset(WATER_TANK), ModBlocks.STEEL_TANK.get().defaultBlockState());
        set(level, o.offset(VODKA_TANK), ModBlocks.STEEL_TANK.get().defaultBlockState());
        set(level, o.offset(VODKA_TANK).above(), ModBlocks.STEEL_TANK.get().defaultBlockState());   // a 2-high tank: they merge like Create's
        // labels
        sign(level, o.offset(-2, 1, 2), Direction.WEST, "Hooch in", "(creative tank)");
        sign(level, o.offset(1, 1, 1), Direction.NORTH, "Pump", "into the boiler");
        sign(level, o.offset(5, 2, 2), Direction.EAST, "Level 1", "water");
        sign(level, o.offset(2, 3, 5), Direction.SOUTH, "Level 2", "vodka");
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
