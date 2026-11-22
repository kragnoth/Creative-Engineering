package com.kragnoth.creativeengineering.gametest;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlock;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlockEntity;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.kragnoth.creativeengineering.registry.ModItems;
import net.minecraft.core.BlockPos;
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
}
