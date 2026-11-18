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
}
