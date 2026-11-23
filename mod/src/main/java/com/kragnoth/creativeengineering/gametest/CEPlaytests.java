package com.kragnoth.creativeengineering.gametest;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlock;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlockEntity;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.kragnoth.creativeengineering.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Headless playtests (dev only; excluded from the jar). Kragnoth runs them: ./gradlew runGameTestServer
 * They check the automation contract (docs/steelworks-spec.md section 0) through the same capability hoppers and
 * Create's funnels use. Template: data/creativeengineering/structure/gametest/playground.nbt (dev/make_gametest_template.py).
 */
@GameTestHolder(CreativeEngineering.MODID)
@PrefixGameTestTemplate(false)
public final class CEPlaytests {
    private static final String PLAYGROUND = "gametest/playground";

    private CEPlaytests() {}

    /** Builds a size x size x size oven with its bottom layer at y=3 and fire under every column. */
    private static void oven(GameTestHelper h, int size, boolean heat) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (heat) {
                    h.setBlock(new BlockPos(4 + x, 2, 4 + z), Blocks.MAGMA_BLOCK);
                }
                for (int y = 0; y < size; y++) {
                    h.setBlock(new BlockPos(4 + x, 3 + y, 4 + z), ModBlocks.COKE_OVEN_BRICKS.get());
                }
            }
        }
    }

    private static IItemHandler items(GameTestHelper h, BlockPos rel, Direction side) {
        return h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(rel), side);
    }

    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void cokeOvenForms2x2x2(GameTestHelper h) {
        open(h);
        oven(h, 2, false);
        h.succeedWhen(() -> {
            CokeOvenBlockEntity be = (CokeOvenBlockEntity) h.getBlockEntity(new BlockPos(5, 4, 5));
            h.assertTrue(be.getControllerBE() != null && be.getControllerBE().getWidth() == 2
                    && be.getControllerBE().getHeight() == 2, "2x2x2 oven did not form");
        });
    }

    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void cokeOvenInputIsInsertOnly(GameTestHelper h) {
        open(h);
        oven(h, 1, false);
        h.runAfterDelay(5, () -> {
            IItemHandler view = items(h, new BlockPos(4, 3, 4), Direction.NORTH);
            h.assertTrue(view != null && view.getSlots() == 2, "compact two-slot view missing");
            h.assertTrue(view.insertItem(0, new ItemStack(Items.COBBLESTONE), false).getCount() == 1, "took a non-recipe item");
            h.assertTrue(view.insertItem(0, new ItemStack(Items.COAL, 8), false).isEmpty(), "refused coal");
            h.assertTrue(view.extractItem(0, 64, false).isEmpty(), "input could be extracted");
            h.assertTrue(items(h, new BlockPos(4, 3, 4), null) != null, "no side-less view");
            h.succeed();
        });
    }

    @GameTest(template = PLAYGROUND, timeoutTicks = 1200)
    public static void cokeOvenMakesCokeAndLights(GameTestHelper h) {
        open(h);
        oven(h, 1, true);
        h.runAfterDelay(5, () -> items(h, new BlockPos(4, 3, 4), Direction.UP).insertItem(0, new ItemStack(Items.COAL, 4), false));
        h.succeedWhen(() -> {
            h.assertBlockProperty(new BlockPos(4, 3, 4), CokeOvenBlock.LIT, true);
            ItemStack out = items(h, new BlockPos(4, 3, 4), Direction.DOWN).getStackInSlot(1);
            h.assertTrue(out.is(ModItems.COAL_COKE.get()) && out.getCount() == 4, "expected 4 coke, got " + out);
        });
    }

    /** A 1x1x1 oven holds 1000 mB; 8 coal make 2000 mB. It must keep coking (creosote vents, never stalls). */
    @GameTest(template = PLAYGROUND, timeoutTicks = 2400)
    public static void cokeOvenVentsInsteadOfStalling(GameTestHelper h) {
        open(h);
        oven(h, 1, true);
        h.runAfterDelay(5, () -> items(h, new BlockPos(4, 3, 4), Direction.UP).insertItem(0, new ItemStack(Items.COAL, 8), false));
        h.succeedWhen(() -> {
            ItemStack out = items(h, new BlockPos(4, 3, 4), Direction.DOWN).getStackInSlot(1);
            h.assertTrue(out.getCount() == 8, "oven stalled at " + out.getCount() + " coke");
        });
    }

    /**
     * Vanilla walls every test structure in barrier blocks before the test runs; they get in the way when the tests
     * are run in runClient as live demonstrations (Kragnoth, 2026-11-22), and nothing here needs containing. Every
     * test strips them first.
     */
    private static void open(GameTestHelper h) {
        net.minecraft.gametest.framework.StructureUtils.removeBarriers(h.getBounds(), h.getLevel());
    }

    private static net.minecraft.world.level.block.Block block(String id) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse(id));
    }

    /**
     * Diesel engine fires up: one crankcase (output east) with one cylinder, a Create Fluid Tank of creosote against
     * its north side, a Create Creative Motor at the front as the starter (read-only: not connected), a Create shaft
     * on the back. Expect: running, output shaft at 64 RPM, stress capacity, cylinder RUNNING, creosote burning.
     */
    @GameTest(template = PLAYGROUND, timeoutTicks = 200)
    public static void dieselEngineStartsAndDrivesShaft(GameTestHelper h) {
        open(h);
        BlockPos cc = new BlockPos(5, 3, 5);
        h.setBlock(cc, ModBlocks.DIESEL_CRANKCASE.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.engine.DieselCrankcaseBlock.HORIZONTAL_FACING, Direction.EAST));
        h.setBlock(cc.above().south(), ModBlocks.DIESEL_CYLINDER.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.FACING, Direction.SOUTH));
        h.setBlock(cc.north(), block("create:fluid_tank"));
        h.setBlock(cc.east(), block("create:shaft").defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, Direction.Axis.X));
        h.runAfterDelay(2, () -> {
            var tank = h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(cc.north()), Direction.SOUTH);
            h.assertTrue(tank != null, "no tank capability");
            tank.fill(new net.neoforged.neoforge.fluids.FluidStack(com.kragnoth.creativeengineering.registry.ModFluids.CREOSOTE.get(), 4000),
                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            h.setBlock(cc.west(), block("create:creative_motor").defaultBlockState()
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.EAST));
        });
        h.succeedWhen(() -> {
            var shaft = (com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(cc.east());
            h.assertTrue(Math.abs(shaft.getSpeed()) == 64, "output shaft turning at " + shaft.getSpeed());
            var engine = (com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity) h.getBlockEntity(cc);
            h.assertTrue(engine.calculateAddedStressCapacity() > 0, "engine gives no stress capacity");
            h.assertBlockProperty(cc.above().south(), com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.RUNNING, true);
            var tank = h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(cc.north()), Direction.SOUTH);
            h.assertTrue(tank.getFluidInTank(0).getAmount() < 4000, "no creosote burnt");
        });
    }

    /** A cylinder added while the engine runs joins in (RUNNING set, capacity up) within the 1 s rescan. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 300)
    public static void dieselCylinderAddedWhileRunningJoins(GameTestHelper h) {
        open(h);
        BlockPos cc = new BlockPos(5, 3, 5);
        h.setBlock(cc, ModBlocks.DIESEL_CRANKCASE.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.engine.DieselCrankcaseBlock.HORIZONTAL_FACING, Direction.EAST));
        h.setBlock(cc.above().south(), ModBlocks.DIESEL_CYLINDER.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.FACING, Direction.SOUTH));
        h.setBlock(cc.north(), block("create:fluid_tank"));
        h.runAfterDelay(2, () -> {
            h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(cc.north()), Direction.SOUTH)
                    .fill(new net.neoforged.neoforge.fluids.FluidStack(com.kragnoth.creativeengineering.registry.ModFluids.CREOSOTE.get(), 4000),
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            h.setBlock(cc.west(), block("create:creative_motor").defaultBlockState()
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.EAST));
        });
        h.runAfterDelay(80, () -> h.setBlock(cc.above().north(), ModBlocks.DIESEL_CYLINDER.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.FACING, Direction.NORTH)));
        h.runAfterDelay(130, () -> h.succeedWhen(() -> {
            h.assertBlockProperty(cc.above().north(), com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.RUNNING, true);
            var engine = (com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity) h.getBlockEntity(cc);
            h.assertTrue(engine.cylinderCount() == 2, "engine counts " + engine.cylinderCount() + " cylinders");
        }));
    }

    /** 2x2x2 vat over campfires forms one vat; water + sugar cane ferments to ethanol, all four batches at once. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 1200)
    public static void fermentingVatFormsAndFerments(GameTestHelper h) {
        open(h);
        BlockPos base = new BlockPos(4, 2, 4);
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                h.setBlock(base.offset(x, 0, z), net.minecraft.world.level.block.Blocks.CAMPFIRE);
                for (int y = 1; y <= 2; y++) {
                    h.setBlock(base.offset(x, y, z), ModBlocks.FERMENTING_VAT.get());
                }
            }
        }
        BlockPos vat = base.above();
        h.runAfterDelay(5, () -> {
            var level = h.getLevel();
            var fl = level.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(vat.above().east()), Direction.UP);
            h.assertTrue(fl != null && fl.getTankCapacity(0) == 8 * 2000, "vat did not form as 2x2x2");
            fl.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000),
                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            var it = level.getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(vat.south()), Direction.NORTH);
            var rest = it.insertItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SUGAR_CANE, 16), false);
            h.assertTrue(rest.isEmpty(), "vat refused sugar cane");
            h.assertTrue(it.insertItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE), true).getCount() == 1,
                    "vat took a non-ingredient");
        });
        h.succeedWhen(() -> {
            var fl = h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(vat), Direction.UP);
            var out = fl.getFluidInTank(1);
            h.assertTrue(out.getFluid() == com.kragnoth.creativeengineering.registry.ModFluids.ETHANOL.source.get() && out.getAmount() == 1000,
                    "product " + out.getAmount() + " " + out.getFluid());
            h.assertTrue(fl.getFluidInTank(0).isEmpty(), "water left over");
        });
    }

    /** A lit Campfire Stand under a vat is passive heat (x1); doused, the vat has none. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 200)
    public static void campfireStandHeatsVat(GameTestHelper h) {
        open(h);
        BlockPos stand = new BlockPos(4, 2, 4);
        h.setBlock(stand, ModBlocks.COPPER_CAMPFIRE_STAND.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.FIRE, com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.Fire.CAMPFIRE)
                .setValue(com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.LIT, true));
        h.setBlock(stand.above(), ModBlocks.FERMENTING_VAT.get());
        h.runAfterDelay(40, () -> {
            var vat = (com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity) h.getBlockEntity(stand.above());
            h.assertTrue(vat.speed() == 1F, "lit stand gave heat " + vat.speed());
            h.setBlock(stand, h.getBlockState(stand).setValue(com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.LIT, false));
        });
        h.runAfterDelay(80, () -> h.succeedWhen(() -> {
            var vat = (com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity) h.getBlockEntity(stand.above());
            h.assertTrue(vat.speed() == 0F, "doused stand still gives heat " + vat.speed());
        }));
    }

    /** Stands are fan catalysts: air passes through; lit campfire = smoking, lit soul = haunting, unlit / empty = neither. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 40)
    public static void campfireStandIsFanCatalyst(GameTestHelper h) {
        open(h);
        var smoke = com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes.SMOKING;
        var haunt = com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes.HAUNTING;
        BlockPos p = new BlockPos(4, 2, 4);
        var base = ModBlocks.BRASS_CAMPFIRE_STAND.get().defaultBlockState();
        var FIRE = com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.FIRE;
        var LIT = com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.LIT;
        h.assertTrue(com.simibubi.create.AllTags.AllBlockTags.FAN_TRANSPARENT.matches(base), "stand blocks fans");
        Object[][] cases = {
                {com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.Fire.CAMPFIRE, true, true, false},
                {com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.Fire.SOUL, true, false, true},
                {com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.Fire.CAMPFIRE, false, false, false},
                {com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.Fire.NONE, false, false, false}};
        for (Object[] c : cases) {
            h.setBlock(p, base.setValue(FIRE, (com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.Fire) c[0]).setValue(LIT, (Boolean) c[1]));
            BlockPos abs = h.absolutePos(p);
            h.assertTrue(smoke.isValidAt(h.getLevel(), abs) == (Boolean) c[2], "smoking wrong for " + c[0] + " lit=" + c[1]);
            h.assertTrue(haunt.isValidAt(h.getLevel(), abs) == (Boolean) c[3], "haunting wrong for " + c[0] + " lit=" + c[1]);
        }
        h.succeed();
    }

    /** No pump: a crankcase pulls fuel out of a tank through a 3-pipe run into its reservoir, and the engine starts. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 300)
    public static void crankcasePullsFuelThroughPipes(GameTestHelper h) {
        open(h);
        BlockPos cc = new BlockPos(5, 3, 6);
        h.setBlock(cc, ModBlocks.DIESEL_CRANKCASE.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.engine.DieselCrankcaseBlock.HORIZONTAL_FACING, Direction.EAST));
        h.setBlock(cc.above().south(), ModBlocks.DIESEL_CYLINDER.get().defaultBlockState()
                .setValue(com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.FACING, Direction.SOUTH));
        var pipe = block("create:fluid_pipe").defaultBlockState();
        for (var prop : new net.minecraft.world.level.block.state.properties.BooleanProperty[]{
                net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST, net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST,
                net.minecraft.world.level.block.state.properties.BlockStateProperties.UP, net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN}) {
            pipe = pipe.setValue(prop, false);
        }
        pipe = pipe.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH, true)
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH, true);
        for (int z = 1; z <= 3; z++) {
            h.setBlock(cc.north(z), pipe);
        }
        BlockPos tank = cc.north(4);
        h.setBlock(tank, block("create:fluid_tank"));
        h.runAfterDelay(2, () -> {
            h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(tank), Direction.SOUTH)
                    .fill(new net.neoforged.neoforge.fluids.FluidStack(com.kragnoth.creativeengineering.registry.ModFluids.CREOSOTE.get(), 4000),
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            h.setBlock(cc.west(), block("create:creative_motor").defaultBlockState()
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.EAST));
        });
        h.runAfterDelay(60, () -> h.succeedWhen(() -> {
            var engine = (com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity) h.getBlockEntity(cc);
            h.assertTrue(engine.reservoirFluid().getAmount() > 0, "reservoir empty: nothing pulled through the pipes");
            h.assertBlockProperty(cc.above().south(), com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.RUNNING, true);
        }));
    }

    private static BlockPos beltRig(GameTestHelper h, BlockPos fly, BlockPos pulley, BlockPos motorAt, Direction motorFacing) {
        var ax = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS;
        h.setBlock(fly, ModBlocks.DIESEL_FLYWHEEL.get().defaultBlockState().setValue(ax, Direction.Axis.X));
        h.setBlock(pulley, ModBlocks.DRIVE_PULLEY.get().defaultBlockState().setValue(ax, Direction.Axis.X));
        h.setBlock(motorAt, block("create:creative_motor").defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, motorFacing));
        return motorAt;
    }

    /** Drive Belt: motor on the flywheel's shaft; the belted pulley 4 blocks up turns at 4x. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void driveBeltQuadruplesSpeed(GameTestHelper h) {
        open(h);
        BlockPos fly = new BlockPos(5, 2, 5), pulley = fly.above(4);
        beltRig(h, fly, pulley, fly.west(), Direction.EAST);
        h.runAfterDelay(2, () -> h.assertTrue(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.link(
                h.getLevel(), h.absolutePos(fly), h.absolutePos(pulley)), "belt refused"));
        h.runAfterDelay(10, () -> h.succeedWhen(() -> {
            float in = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(fly)).getSpeed();
            float out = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(pulley)).getSpeed();
            h.assertTrue(in != 0 && out == in * 4, "flywheel " + in + " rpm, pulley " + out + " rpm");
        }));
    }

    /** Driving the pulley turns the flywheel at a quarter of its speed. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void driveBeltReverseQuarters(GameTestHelper h) {
        open(h);
        BlockPos fly = new BlockPos(5, 2, 5), pulley = fly.above(3);
        beltRig(h, fly, pulley, pulley.east(), Direction.WEST);
        h.runAfterDelay(2, () -> com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.link(
                h.getLevel(), h.absolutePos(pulley), h.absolutePos(fly)));
        h.runAfterDelay(10, () -> h.succeedWhen(() -> {
            float in = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(pulley)).getSpeed();
            float out = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(fly)).getSpeed();
            h.assertTrue(in != 0 && out * 4 == in, "pulley " + in + " rpm, flywheel " + out + " rpm");
        }));
    }

    /** Breaking the pulley drops the belt, frees the flywheel, and the pulley side stops. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void driveBeltDropsWhenWheelBroken(GameTestHelper h) {
        open(h);
        BlockPos fly = new BlockPos(5, 2, 5), pulley = fly.above(2);
        beltRig(h, fly, pulley, fly.west(), Direction.EAST);
        h.runAfterDelay(2, () -> h.assertTrue(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.link(
                h.getLevel(), h.absolutePos(fly), h.absolutePos(pulley)), "chain refused at a 2 block gap"));
        h.runAfterDelay(10, () -> h.destroyBlock(pulley));
        h.runAfterDelay(14, () -> h.succeedWhen(() -> {
            h.assertItemEntityPresent(com.kragnoth.creativeengineering.registry.ModItems.DRIVE_BELT.get(), pulley, 5);   // it falls
            var w = (com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity) h.getBlockEntity(fly);
            h.assertFalse(w.hasBelt(), "flywheel still thinks it has a belt");
            h.assertTrue(w.getSpeed() != 0, "flywheel lost its motor drive");
        }));
    }

    /** Chains go anywhere in the wheels' plane: flywheel to a sprocket 3 blocks to the side at the same height = 4x. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void driveChainSideways(GameTestHelper h) {
        open(h);
        BlockPos fly = new BlockPos(5, 3, 3), sprocket = fly.south(3);
        beltRig(h, fly, sprocket, fly.west(), Direction.EAST);
        h.runAfterDelay(2, () -> h.assertTrue(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.link(
                h.getLevel(), h.absolutePos(fly), h.absolutePos(sprocket)), "sideways chain refused"));
        h.runAfterDelay(10, () -> h.succeedWhen(() -> {
            float in = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(fly)).getSpeed();
            float out = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(sprocket)).getSpeed();
            h.assertTrue(in != 0 && out == in * 4, "flywheel " + in + " rpm, sprocket " + out + " rpm");
        }));
    }

    /** Sprocket to sprocket on a diagonal is 1:1; a sprocket out of the plane is refused. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void driveChainSprocketToSprocket(GameTestHelper h) {
        open(h);
        var ax = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS;
        BlockPos a = new BlockPos(5, 2, 2), b = a.offset(0, 2, 3), off = a.offset(1, 2, 3);
        for (BlockPos p : new BlockPos[]{a, b, off}) {
            h.setBlock(p, ModBlocks.DRIVE_PULLEY.get().defaultBlockState().setValue(ax, Direction.Axis.X));
        }
        h.setBlock(a.west(), block("create:creative_motor").defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.EAST));
        h.runAfterDelay(2, () -> {
            h.assertTrue(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.linkProblem(
                    h.getLevel(), h.absolutePos(a), h.absolutePos(off)) != null, "out-of-plane chain accepted");
            h.assertTrue(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.link(
                    h.getLevel(), h.absolutePos(a), h.absolutePos(b)), "diagonal sprocket chain refused");
        });
        h.runAfterDelay(10, () -> h.succeedWhen(() -> {
            float in = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(a)).getSpeed();
            float out = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(b)).getSpeed();
            h.assertTrue(in != 0 && out == in, "sprocket " + in + " rpm, other sprocket " + out + " rpm");
        }));
    }

    /** A sprocket relays: flywheel -> sprocket A (4x) -> sprocket B (1:1); a third chain on A is refused. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void driveChainRelay(GameTestHelper h) {
        open(h);
        var ax = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS;
        BlockPos fly = new BlockPos(5, 2, 2), a = fly.south(3), b = a.offset(0, 3, 2), c = a.offset(0, 0, 3);
        beltRig(h, fly, a, fly.west(), Direction.EAST);
        for (BlockPos p : new BlockPos[]{b, c}) {
            h.setBlock(p, ModBlocks.DRIVE_PULLEY.get().defaultBlockState().setValue(ax, Direction.Axis.X));
        }
        h.runAfterDelay(2, () -> {
            var L = h.getLevel();
            h.assertTrue(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.link(L, h.absolutePos(fly), h.absolutePos(a)), "first chain refused");
            h.assertTrue(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.link(L, h.absolutePos(a), h.absolutePos(b)), "relay chain refused");
            h.assertTrue("taken".equals(com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.linkProblem(
                    L, h.absolutePos(a), h.absolutePos(c))), "third chain on one wheel accepted");
        });
        h.runAfterDelay(10, () -> h.succeedWhen(() -> {
            float in = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(fly)).getSpeed();
            float out = ((com.simibubi.create.content.kinetics.base.KineticBlockEntity) h.getBlockEntity(b)).getSpeed();
            h.assertTrue(in != 0 && out == in * 4, "flywheel " + in + " rpm, relayed sprocket " + out + " rpm");
        }));
    }

    /** A 1x1x3 Distilling Tank on a campfire splits hooch (water on level 1, vodka on level 2, pushed into a Steel Storage Tank beside it, which gets a coupling). */
    @GameTest(template = PLAYGROUND, timeoutTicks = 600)
    public static void distillingTankSplitsHooch(GameTestHelper h) {
        open(h);
        BlockPos base = new BlockPos(5, 2, 5);
        h.setBlock(base, net.minecraft.world.level.block.Blocks.CAMPFIRE);
        for (int y = 1; y <= 3; y++) {
            h.setBlock(base.above(y), ModBlocks.DISTILLING_TANK.get());
        }
        BlockPos boiler = base.above(), lvl1 = base.above(2), lvl2 = base.above(3);
        var hooch = new net.neoforged.neoforge.fluids.FluidStack(com.kragnoth.creativeengineering.registry.ModFluids.HOOCH.source.get(), 1000);
        h.runAfterDelay(5, () -> {
            var L = h.getLevel();
            var in = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(boiler), Direction.NORTH);
            h.assertTrue(in.fill(hooch, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE) == 1000, "boiler refused hooch");
            var up = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(lvl2), Direction.NORTH);
            h.assertTrue(up.fill(hooch, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE) == 0, "a fraction level took input");
            h.assertBlockProperty(lvl2, com.kragnoth.creativeengineering.content.distilling.DistillingTankBlock.VALVE, true);
            // pump-free outlet: a Create fluid tank touching level 2 fills with the vodka
            h.setBlock(lvl2.east(), ModBlocks.STEEL_TANK.get());
        });
        h.runAfterDelay(30, () -> h.succeedWhen(() -> {
            var L = h.getLevel();
            var w = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(lvl1), Direction.NORTH).getFluidInTank(0);
            var v = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(lvl2), Direction.NORTH).getFluidInTank(0);
            h.assertTrue(w.getFluid() == net.minecraft.world.level.material.Fluids.WATER && w.getAmount() >= 150, "level 1: " + w.getAmount() + " " + w.getFluid());
            var out = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(lvl2.east()), Direction.WEST).getFluidInTank(0);
            var col = (com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity) h.getBlockEntity(lvl2);
            h.assertTrue(col.greebles().stream().anyMatch(p -> p.model().equals("distilling_tank_outlet")), "no coupling drawn where the tank touches level 2");
            h.assertTrue(out.getFluid() == com.kragnoth.creativeengineering.registry.ModFluids.VODKA.source.get() && out.getAmount() >= 100,
                    "storage tank beside level 2: " + out.getAmount() + " " + out.getFluid());
        }));
    }

    /** Like Create tanks: placing one vat on top of a 2x2 vat places the whole 2x2 layer (4 items used). */
    @GameTest(template = PLAYGROUND, timeoutTicks = 60)
    public static void vatPlacesWholeLayer(GameTestHelper h) {
        open(h);
        BlockPos base = new BlockPos(4, 2, 4);
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                h.setBlock(base.offset(x, 0, z), ModBlocks.FERMENTING_VAT.get());
            }
        }
        h.runAfterDelay(5, () -> {
            var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            var stack = new net.minecraft.world.item.ItemStack(com.kragnoth.creativeengineering.registry.ModItems.FERMENTING_VAT.get(), 8);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
            BlockPos on = h.absolutePos(base.offset(1, 0, 1));
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(on).add(0, 0.5, 0), Direction.UP, on, false);
            stack.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    h.assertBlockPresent(ModBlocks.FERMENTING_VAT.get(), base.offset(x, 1, z));
                }
            }
            h.assertTrue(player.getMainHandItem().getCount() == 4, "used " + (8 - player.getMainHandItem().getCount()) + " vats, expected 4");
            h.succeed();
        });
    }

    /** The /test rundistill demo really runs: pumped hooch distils into the water and vodka storage tanks. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 900)
    public static void distillDemoRuns(GameTestHelper h) {
        open(h);
        BlockPos o = new BlockPos(4, 2, 4);
        com.kragnoth.creativeengineering.demo.DemoCommands.buildDistiller(h.getLevel(), h.absolutePos(o));
        h.runAfterDelay(20, () -> h.succeedWhen(() -> {
            var L = h.getLevel();
            var w = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(o.offset(com.kragnoth.creativeengineering.demo.DemoCommands.WATER_TANK)), Direction.UP).getFluidInTank(0);
            var v = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(o.offset(com.kragnoth.creativeengineering.demo.DemoCommands.VODKA_TANK)), Direction.UP).getFluidInTank(0);
            var src = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(o.offset(-1, 1, 2)), Direction.EAST);
            var pump = h.getBlockEntity(o.offset(1, 1, 2));
            var boiler = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(o.offset(2, 1, 2)), Direction.WEST);
            String diag = " | creative tank " + (src == null ? "none" : src.getFluidInTank(0).getAmount() + " " + src.getFluidInTank(0).getFluid())
                    + " | pump " + (pump instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity k ? k.getSpeed() + " rpm" : String.valueOf(pump))
                    + " | boiler " + (boiler == null ? "none" : boiler.getFluidInTank(0).getAmount() + " " + boiler.getFluidInTank(0).getFluid());
            if (h.getBlockEntity(o.offset(2, 1, 2)) instanceof com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity t) {
                var b = t.blocker();
                diag += " | size " + java.util.Arrays.toString(t.size()) + " heat " + t.speed() + " progress " + t.progressFraction()
                        + " blocker " + (b == null ? "none" : b.getString()) + " | L1 " + t.layerFluid(1).getAmount() + " " + t.layerFluid(1).getFluid()
                        + " L2 " + t.layerFluid(2).getAmount() + " " + t.layerFluid(2).getFluid();
                var wcap = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(o.offset(4, 2, 2)), Direction.WEST);
                diag += " | water tank block " + L.getBlockState(h.absolutePos(o.offset(4, 2, 2))) + " cap " + (wcap == null ? "null" : wcap.getTankCapacity(0))
                        + " | L1 east neighbour of (3,2,2): " + L.getBlockState(h.absolutePos(o.offset(3, 2, 2))).getBlock();
            }
            h.assertTrue(w.getFluid() == net.minecraft.world.level.material.Fluids.WATER && w.getAmount() >= 150, "water tank: " + w.getAmount() + " " + w.getFluid() + diag);
            h.assertTrue(v.getFluid() == com.kragnoth.creativeengineering.registry.ModFluids.VODKA.source.get() && v.getAmount() >= 100,
                    "vodka tank: " + v.getAmount() + " " + v.getFluid());
        }));
    }

    private static BlockState steelPipe(Direction... open) {
        BlockState s = ModBlocks.STEEL_PIPE.get().defaultBlockState();
        for (Direction d : Direction.values()) {
            s = s.setValue(net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(d), java.util.List.of(open).contains(d));
        }
        return s;
    }

    /** A Tap on level 2 gravity-feeds through Steel Pipe: the tank at its height fills, the one above it doesn't. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 600)
    public static void tapGravityFeeds(GameTestHelper h) {
        open(h);
        BlockPos base = new BlockPos(3, 2, 5);
        h.setBlock(base, net.minecraft.world.level.block.Blocks.CAMPFIRE);
        for (int y = 1; y <= 3; y++) {
            h.setBlock(base.above(y), ModBlocks.DISTILLING_TANK.get());
        }
        BlockPos lvl2 = base.above(3), tap = lvl2.east(), junction = tap.east(), low = junction.east(), riser = junction.above(), high = riser.above();
        h.setBlock(tap, ModBlocks.TAP.get().defaultBlockState().setValue(com.kragnoth.creativeengineering.content.distilling.TapBlock.FACING, Direction.EAST));
        h.setBlock(low, ModBlocks.STEEL_TANK.get());
        h.setBlock(high, ModBlocks.STEEL_TANK.get());
        h.setBlock(junction, steelPipe(Direction.WEST, Direction.EAST, Direction.UP));
        h.setBlock(riser, steelPipe(Direction.DOWN, Direction.UP));
        h.runAfterDelay(5, () -> h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(base.above()), Direction.NORTH)
                .fill(new net.neoforged.neoforge.fluids.FluidStack(com.kragnoth.creativeengineering.registry.ModFluids.HOOCH.source.get(), 1000),
                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE));
        h.runAfterDelay(30, () -> h.succeedWhen(() -> {
            var L = h.getLevel();
            var lo = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(low), Direction.WEST).getFluidInTank(0);
            var hi = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(high), Direction.DOWN).getFluidInTank(0);
            h.assertTrue(lo.getFluid() == com.kragnoth.creativeengineering.registry.ModFluids.VODKA.source.get() && lo.getAmount() >= 100,
                    "tank at the tap's height: " + lo.getAmount() + " " + lo.getFluid());
            h.assertTrue(hi.isEmpty(), "tank above the tap got " + hi.getAmount() + " " + hi.getFluid());
        }));
    }

    // ------------------------------------------------------------------------------------------------ petroleum
    private static final String PJ = "com.kragnoth.creativeengineering.content.petroleum";

    private static net.minecraft.world.level.block.state.BlockState create(String id) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create", id)).defaultBlockState();
    }

    /**
     * Crank Gearbox at g facing south with a creative motor on its east side, a column of industrial iron `height`
     * high `a` blocks on with a Walking Beam Bearing on top; returns the column's base.
     */
    private static BlockPos pumpjack(GameTestHelper h, BlockPos g, int a, int height) {
        h.setBlock(g, ModBlocks.PUMPJACK.get().defaultBlockState().setValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING, Direction.SOUTH));
        h.setBlock(g.east(), create("creative_motor").setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.WEST));
        BlockPos base = g.south(a);
        for (int y = 0; y < height; y++) {
            h.setBlock(base.above(y), create("industrial_iron_block"));
        }
        h.setBlock(base.above(height), ModBlocks.BEAM_BEARING.get().defaultBlockState().setValue(com.kragnoth.creativeengineering.content.petroleum.BeamBearingBlock.FACING, Direction.SOUTH));
        return base;
    }

    /** The smallest rig (column 2, rear arm 1, reach 1): returns where its Wellhead goes. */
    private static BlockPos pumpjack(GameTestHelper h, BlockPos g) {
        return pumpjack(h, g, 1, 2).south();
    }

    private static com.kragnoth.creativeengineering.content.petroleum.WellheadBlockEntity wellhead(GameTestHelper h, BlockPos rel) {
        return (com.kragnoth.creativeengineering.content.petroleum.WellheadBlockEntity) h.getBlockEntity(rel);
    }

    @GameTest(template = PLAYGROUND, timeoutTicks = 200)
    public static void pumpjackDrawsCrudeIntoTank(GameTestHelper h) {
        open(h);
        BlockPos well = pumpjack(h, new BlockPos(4, 2, 3));
        h.setBlock(well, ModBlocks.WELLHEAD.get());
        net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(h.absolutePos(well));
        com.kragnoth.creativeengineering.content.petroleum.OilVeins.force(chunk, 60);
        h.setBlock(well.east(), ModBlocks.STEEL_TANK.get());   // straight off the wellhead's side
        h.runAfterDelay(160, () -> h.succeedWhen(() -> {
            var t = h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(well.east()), Direction.WEST).getFluidInTank(0);
            h.assertTrue(t.getFluid() == com.kragnoth.creativeengineering.registry.ModFluids.CRUDE_OIL.source.get() && t.getAmount() >= 10,
                    "tank by the wellhead: " + t.getAmount() + " " + t.getFluid());
            com.kragnoth.creativeengineering.content.petroleum.OilVeins.unforce(chunk);
        }));
    }

    /** No vein: the overworld well source is water (data/creativeengineering/well_sources/water.json). */
    @GameTest(template = PLAYGROUND, timeoutTicks = 200)
    public static void pumpjackWithoutVeinDrawsWater(GameTestHelper h) {
        open(h);
        BlockPos well = pumpjack(h, new BlockPos(4, 2, 3));
        h.setBlock(well, ModBlocks.WELLHEAD.get());
        net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(h.absolutePos(well));
        com.kragnoth.creativeengineering.content.petroleum.OilVeins.force(chunk, 0);
        h.runAfterDelay(120, () -> h.succeedWhen(() -> {
            var w = wellhead(h, well);
            h.assertTrue(w.tank().getFluid().getFluid() == net.minecraft.world.level.material.Fluids.WATER && w.tank().getFluidAmount() > 0,
                    "dry-of-oil well gave " + w.tank().getFluidAmount() + " " + w.tank().getFluid().getFluid());
            com.kragnoth.creativeengineering.content.petroleum.OilVeins.unforce(chunk);
        }));
    }

    /** A Wellhead with no Pumpjack, and a Pumpjack with nothing in front, both do nothing (no magic). */
    @GameTest(template = PLAYGROUND, timeoutTicks = 200)
    public static void wellheadNeedsAPumpjack(GameTestHelper h) {
        open(h);
        BlockPos lone = new BlockPos(9, 2, 9);
        h.setBlock(lone, ModBlocks.WELLHEAD.get());
        BlockPos g = new BlockPos(4, 2, 3);
        pumpjack(h, g);
        h.runAfterDelay(100, () -> h.succeedWhen(() -> {
            h.assertTrue(wellhead(h, lone).tank().isEmpty(), "lone wellhead pumped " + wellhead(h, lone).tank().getFluidAmount());
            var pj = (com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity) h.getBlockEntity(g);
            h.assertTrue(pj.getSpeed() != 0 && pj.rig() != null && pj.rig().well() == null, "pumpjack should turn with nothing to stroke");
            h.assertTrue(pj.calculateStressApplied() == com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity.IDLE_SU_PER_RPM, "idle stress");
        }));
    }

    /** Reach 2 past the post doubles the stroke: twice the draw and twice the stress of the short rig. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 200)
    public static void longReachDoublesStroke(GameTestHelper h) {
        open(h);
        BlockPos g = new BlockPos(4, 2, 2);
        BlockPos well = pumpjack(h, g, 1, 2).south(2);
        h.setBlock(well, ModBlocks.WELLHEAD.get());
        net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(h.absolutePos(well));
        com.kragnoth.creativeengineering.content.petroleum.OilVeins.force(chunk, 40);
        h.runAfterDelay(60, () -> h.succeedWhen(() -> {
            var pj = (com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity) h.getBlockEntity(g);
            var w = wellhead(h, well);
            h.assertTrue(pj.rig() != null && pj.rig().b() == 2 && pj.rig().stroke() == 2F, "rig " + pj.rig());
            float expected = 40 * pj.power() * 2;
            h.assertTrue(Math.abs(w.pumpRate() - expected) < 0.01F, "draw " + w.pumpRate() + " expected " + expected);
            h.assertTrue(pj.calculateStressApplied() == 2 * com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity.SU_PER_RPM, "stress " + pj.calculateStressApplied());
            com.kragnoth.creativeengineering.content.petroleum.OilVeins.unforce(chunk);
        }));
    }

    /** Reach 6 on a 2-high column would swing the horsehead into the well: it refuses to run until the column is 5 high. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 200)
    public static void longReachNeedsATallColumn(GameTestHelper h) {
        open(h);
        BlockPos g = new BlockPos(2, 1, 1);
        BlockPos base = pumpjack(h, g, 1, 2);
        BlockPos well = base.south(6);
        h.setBlock(well, ModBlocks.WELLHEAD.get());
        h.runAfterDelay(40, () -> {
            var pj = (com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity) h.getBlockEntity(g);
            h.assertTrue(pj.rig() != null && pj.rig().b() == 6 && pj.rig().tooShort(), "2-high column with reach 6 should be too short: " + pj.rig());
            h.assertTrue(wellhead(h, well).pumpRate() == 0, "too-short rig pumped " + wellhead(h, well).pumpRate());
            for (int y = 2; y < 5; y++) {   // raise the column: move the bearing up
                h.setBlock(base.above(y), create("industrial_iron_block"));
            }
            h.setBlock(base.above(5), ModBlocks.BEAM_BEARING.get().defaultBlockState().setValue(com.kragnoth.creativeengineering.content.petroleum.BeamBearingBlock.FACING, Direction.SOUTH));
            h.runAfterDelay(40, () -> h.succeedWhen(() -> {
                var r = pj.rig();
                h.assertTrue(r != null && r.h() == 5 && !r.tooShort() && r.stroke() == 6F, "5-high column rig " + r);
                h.assertTrue(wellhead(h, well).pumpRate() > 0, "tall rig should pump");
            }));
        });
    }

    @GameTest(template = PLAYGROUND, timeoutTicks = 20)
    public static void seismicProbeFindsVein(GameTestHelper h) {
        open(h);
        BlockPos here = h.absolutePos(new BlockPos(4, 1, 4));
        net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(here);
        com.kragnoth.creativeengineering.content.petroleum.OilVeins.force(chunk, 42);
        var msg = com.kragnoth.creativeengineering.content.petroleum.SeismicProbeItem.report(h.getLevel(), here);
        com.kragnoth.creativeengineering.content.petroleum.OilVeins.unforce(chunk);
        h.assertTrue(msg.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc
                && tc.getKey().equals("creativeengineering.probe.here") && String.valueOf(tc.getArgs()[0]).equals("42"), "probe said " + msg.getString());
        h.succeed();
    }

    @GameTest(template = PLAYGROUND, timeoutTicks = 20)
    public static void crudeDistilsIntoThreeFuels(GameTestHelper h) {
        open(h);
        var r = h.getLevel().getRecipeManager().byKey(CreativeEngineering.id("distilling/crude_oil")).orElseThrow().value();
        var d = (com.kragnoth.creativeengineering.content.distilling.DistillingRecipe) r;
        h.assertTrue(d.fractions().size() == 4 && d.minHeight() == 5 && d.heat() == 2, "crude recipe shape");
        h.assertTrue(d.fractions().get(0).getFluid() == com.kragnoth.creativeengineering.registry.ModFluids.HEAVY_OIL.source.get()
                && d.fractions().get(3).getFluid() == com.kragnoth.creativeengineering.registry.ModFluids.GASOLINE.source.get(), "fraction order");
        h.succeed();
    }

    /** The /test runrefinery demo really runs: pumped crude distils into the diesel, kerosene and gasoline tanks. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 1200)
    public static void refineryDemoRuns(GameTestHelper h) {
        open(h);
        BlockPos o = new BlockPos(3, 1, 3);
        com.kragnoth.creativeengineering.demo.DemoCommands.buildRefinery(h.getLevel(), h.absolutePos(o));
        h.runAfterDelay(20, () -> h.succeedWhen(() -> {
            var L = h.getLevel();
            String diag = "";
            if (h.getBlockEntity(o.offset(3, 1, 2)) instanceof com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity t) {
                var b = t.blocker();
                diag = " | heat " + t.speed() + " blocker " + (b == null ? "none" : b.getString()) + " boiler " + t.layerFluid(0).getAmount()
                        + " " + t.layerFluid(0).getFluid();
            }
            if (h.getBlockEntity(o) instanceof com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity pj) {
                diag += " | pumpjack " + pj.getSpeed() + " rpm";
            }
            if (h.getBlockEntity(o.offset(0, 0, 2)) instanceof com.kragnoth.creativeengineering.content.petroleum.WellheadBlockEntity w) {
                diag += " | wellhead " + w.yieldRate() + " mB/s, tank " + w.tank().getFluidAmount();
            }
            for (var e : java.util.List.of(
                    java.util.Map.entry(com.kragnoth.creativeengineering.demo.DemoCommands.HEAVY_TANK, com.kragnoth.creativeengineering.registry.ModFluids.HEAVY_OIL),
                    java.util.Map.entry(com.kragnoth.creativeengineering.demo.DemoCommands.DIESEL_TANK, com.kragnoth.creativeengineering.registry.ModFluids.DIESEL),
                    java.util.Map.entry(com.kragnoth.creativeengineering.demo.DemoCommands.KEROSENE_TANK, com.kragnoth.creativeengineering.registry.ModFluids.KEROSENE),
                    java.util.Map.entry(com.kragnoth.creativeengineering.demo.DemoCommands.GASOLINE_TANK, com.kragnoth.creativeengineering.registry.ModFluids.GASOLINE))) {
                var f = L.getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(o.offset(e.getKey())), Direction.UP).getFluidInTank(0);
                h.assertTrue(f.getFluid() == e.getValue().source.get() && f.getAmount() >= 20, e.getValue().name + " tank: " + f.getAmount() + " " + f.getFluid() + diag);
            }
            com.kragnoth.creativeengineering.content.petroleum.OilVeins.unforce(new net.minecraft.world.level.ChunkPos(h.absolutePos(o.offset(0, 0, 2))));
        }));
    }

    /** Kragnoth: past 16 RPM the crude stopped climbing. The same demo with the motor turned up to 128 RPM must still run. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 1200)
    public static void refineryRunsFast(GameTestHelper h) {
        open(h);
        BlockPos o = new BlockPos(3, 1, 3);
        com.kragnoth.creativeengineering.demo.DemoCommands.buildRefinery(h.getLevel(), h.absolutePos(o));
        h.runAfterDelay(5, () -> {
            var m = (com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) h.getBlockEntity(o.offset(1, 0, 0));
            m.generatedSpeed.setValue(128);
        });
        h.runAfterDelay(40, () -> h.succeedWhen(() -> {
            var pj = (com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity) h.getBlockEntity(o);
            var w = (com.kragnoth.creativeengineering.content.petroleum.WellheadBlockEntity) h.getBlockEntity(o.offset(0, 0, 2));
            String diag = " | pumpjack " + pj.getSpeed() + " rpm overstressed " + pj.isOverStressed() + " | wellhead tank " + w.tank().getFluidAmount();
            if (h.getBlockEntity(o.offset(3, 1, 2)) instanceof com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity t) {
                var b = t.blocker();
                diag += " | boiler " + t.layerFluid(0).getAmount() + " blocker " + (b == null ? "none" : b.getString())
                        + " L1 " + t.layerFluid(1).getAmount() + " L2 " + t.layerFluid(2).getAmount() + " L3 " + t.layerFluid(3).getAmount();
            }
            var f = h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, h.absolutePos(o.offset(com.kragnoth.creativeengineering.demo.DemoCommands.GASOLINE_TANK)), Direction.UP).getFluidInTank(0);
            h.assertTrue(f.getAmount() >= 300, "gasoline tank at 128 RPM: " + f.getAmount() + diag);
            com.kragnoth.creativeengineering.content.petroleum.OilVeins.unforce(new net.minecraft.world.level.ChunkPos(h.absolutePos(o.offset(0, 0, 2))));
        }));
    }

    /** Every drink bottle drinks like a potion: its buff plus nausea, and the glass bottle back. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 20)
    public static void drinkBottlesApplyEffects(GameTestHelper h) {
        open(h);
        for (var e : ModItems.DRINKS.entrySet()) {
            var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            ItemStack bottle = new ItemStack(e.getValue().get());
            ItemStack left = bottle.finishUsingItem(h.getLevel(), player);
            h.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION), e.getKey() + ": no nausea");
            h.assertTrue(player.getActiveEffects().size() >= 2, e.getKey() + ": no buff, effects " + player.getActiveEffects());
            boolean bottleBack = left.is(Items.GLASS_BOTTLE) || player.getInventory().contains(new ItemStack(Items.GLASS_BOTTLE));
            h.assertTrue(bottleBack, e.getKey() + ": no glass bottle back, got " + left);
        }
        var rm = h.getLevel().getRecipeManager();
        h.assertTrue(rm.byKey(CreativeEngineering.id("filling/vodka_bottle")).isPresent() && rm.byKey(CreativeEngineering.id("emptying/wine_bottle")).isPresent(),
                "spout / drain recipes");
        h.succeed();
    }

    /** An Oil Can dose on a Create machine cuts its share of the network's stress by a quarter, and asphalt is quick. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void oilCanLubricatesMachines(GameTestHelper h) {
        open(h);
        BlockPos g = new BlockPos(4, 2, 3);
        pumpjack(h, g, 1, 2);   // a turning gearbox with nothing to stroke: 2 SU/RPM
        h.runAfterDelay(20, () -> {
            var pj = (com.kragnoth.creativeengineering.content.petroleum.PumpjackBlockEntity) h.getBlockEntity(g);
            float before = pj.getOrCreateNetwork().calculateStress();
            var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            ItemStack can = com.kragnoth.creativeengineering.content.petroleum.OilCanItem.full();
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, can);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(g)), Direction.UP, h.absolutePos(g), false);
            var result = can.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
            float after = pj.getOrCreateNetwork().calculateStress();
            h.assertTrue(result.consumesAction(), "oil can use: " + result);
            h.assertTrue(com.kragnoth.creativeengineering.content.petroleum.Lubrication.isLubricated(pj), "not lubricated");
            h.assertTrue(before > 0 && Math.abs(after - before * 0.75F) < 0.01F, "stress " + before + " -> " + after);
            h.assertTrue(com.kragnoth.creativeengineering.content.petroleum.OilCanItem.amount(player.getMainHandItem()) == 950, "can should hold 950 mB");
            h.assertTrue(ModBlocks.ASPHALT.get().getSpeedFactor() > 1, "asphalt speeds you up");
            h.succeed();
        });
    }

    /** 0.21.0 polish: slag crushes into gravel, asphalt takes slag, and the distilling kit needs no Blaze Burner or brass. */
    @GameTest(template = PLAYGROUND, timeoutTicks = 20)
    public static void polishedRecipesLoad(GameTestHelper h) {
        open(h);
        var rm = h.getLevel().getRecipeManager();
        h.assertTrue(rm.byKey(CreativeEngineering.id("crushing/slag")).isPresent(), "slag crushing recipe");
        h.assertTrue(new ItemStack(ModItems.SLAG.get()).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                CreativeEngineering.id("asphalt_aggregate"))), "slag is asphalt aggregate");
        for (String id : new String[] {"crafting/distilling_tank", "crafting/tap"}) {
            var r = rm.byKey(CreativeEngineering.id(id)).orElseThrow().value();
            for (var ing : r.getIngredients()) {
                for (ItemStack st : ing.getItems()) {
                    h.assertTrue(!st.is(Items.AIR) && !net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(st.getItem()).getPath().contains("blaze_burner")
                            && !net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(st.getItem()).getPath().contains("brass"), id + " still needs " + st);
                }
            }
        }
        h.succeed();
    }
}
