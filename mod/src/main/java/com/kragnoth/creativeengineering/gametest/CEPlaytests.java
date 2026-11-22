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
        oven(h, 2, false);
        h.succeedWhen(() -> {
            CokeOvenBlockEntity be = (CokeOvenBlockEntity) h.getBlockEntity(new BlockPos(5, 4, 5));
            h.assertTrue(be.getControllerBE() != null && be.getControllerBE().getWidth() == 2
                    && be.getControllerBE().getHeight() == 2, "2x2x2 oven did not form");
        });
    }

    @GameTest(template = PLAYGROUND, timeoutTicks = 100)
    public static void cokeOvenInputIsInsertOnly(GameTestHelper h) {
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
        oven(h, 1, true);
        h.runAfterDelay(5, () -> items(h, new BlockPos(4, 3, 4), Direction.UP).insertItem(0, new ItemStack(Items.COAL, 8), false));
        h.succeedWhen(() -> {
            ItemStack out = items(h, new BlockPos(4, 3, 4), Direction.DOWN).getStackInSlot(1);
            h.assertTrue(out.getCount() == 8, "oven stalled at " + out.getCount() + " coke");
        });
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
}
