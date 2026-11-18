package com.kragnoth.creativeengineering.compat.ponder;

import com.kragnoth.creativeengineering.registry.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Creative Engineering's Ponder scenes. Text lives in the lang file as creativeengineering.ponder.<scene>.header /
 * .text_N; dev/gen_ponder_lang.py rebuilds those keys from the strings here, so edit the English here and re-run it.
 * Schematics are built by dev/ponder_nbt.py; the coordinates in each method's comment must match it.
 */
public final class CEScenes {
    private CEScenes() {}

    /**
     * "cementation" (7x7): basin (1,1,3) under a press (1,3,3); depot (3,1,3); lava (4,1,3); encased fan (5,1,3)
     * blowing west through the lava at the depot.
     */
    public static void cementation(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("cementation", "Cementation: Making Blister Steel");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        Selection basin = util.select().position(1, 1, 3);
        Selection press = util.select().position(1, 3, 3);
        Selection depot = util.select().position(3, 1, 3);
        Selection lava = util.select().position(4, 1, 3);
        Selection fan = util.select().position(5, 1, 3);

        scene.world().showSection(basin, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(press, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(80)
                .text("A Mechanical Press over a Basin packs iron into carbon")
                .pointAt(util.vector().topOf(1, 1, 3))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(1, 1, 3), Pointing.DOWN, 50).withItem(new ItemStack(Items.IRON_INGOT));
        scene.idle(25);
        scene.overlay().showControls(util.vector().topOf(1, 1, 3), Pointing.DOWN, 40).withItem(new ItemStack(Items.COAL, 2));
        scene.idle(45);
        scene.overlay().showText(80)
                .text("One iron ingot and two coal or charcoal make Carbon-Packed Iron")
                .pointAt(util.vector().topOf(1, 1, 3))
                .placeNearTarget();
        scene.overlay().showControls(util.vector().topOf(1, 1, 3), Pointing.DOWN, 60).withItem(new ItemStack(ModItems.CARBON_PACKED_IRON.get()));
        scene.idle(90);
        scene.overlay().showText(70)
                .text("Coke is four times better: two iron and one coke make two")
                .pointAt(util.vector().topOf(1, 1, 3))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(depot, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(lava, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(fan, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.RED, "blast", util.select().fromTo(3, 1, 3, 4, 1, 3), 80);
        scene.overlay().showText(80)
                .text("A fan blowing through lava bakes everything in its air stream: Bulk Blasting")
                .pointAt(util.vector().topOf(4, 1, 3))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);
        scene.overlay().showControls(util.vector().topOf(3, 1, 3), Pointing.DOWN, 40).withItem(new ItemStack(ModItems.CARBON_PACKED_IRON.get()));
        scene.idle(45);
        scene.overlay().showControls(util.vector().topOf(3, 1, 3), Pointing.DOWN, 60).withItem(new ItemStack(ModItems.BLISTER_STEEL_INGOT.get()));
        scene.overlay().showText(80)
                .text("Carbon-Packed Iron comes out as Blister Steel")
                .pointAt(util.vector().topOf(3, 1, 3))
                .placeNearTarget();
        scene.idle(90);
        scene.overlay().showText(80)
                .text("A lit Blaze Burner works in place of lava. A vanilla Blast Furnace can do it too, one at a time")
                .pointAt(util.vector().topOf(4, 1, 3))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);
        scene.markAsFinished();
    }

    /**
     * "coke_oven" (6x6): blaze burners at y=1 under a 2x2x2 oven (x 2-3, y 2-3, z 2-3); a funnel feeding coal at
     * (1,3,2); an extracting funnel at (4,2,3) taking coke out.
     */
    public static void cokeOven(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("coke_oven", "Coking Coal in a Coke Oven");
        scene.configureBasePlate(0, 0, 6);
        scene.showBasePlate();
        scene.idle(10);

        Selection burners = util.select().fromTo(2, 1, 2, 3, 1, 3);
        Selection oven = util.select().fromTo(2, 2, 2, 3, 3, 3);
        Selection in = util.select().position(1, 3, 2);
        Selection out = util.select().position(4, 2, 3);

        scene.world().showSection(oven, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(80)
                .text("Coke Oven Bricks placed together form one oven, up to 3 wide and 3 tall")
                .pointAt(util.vector().topOf(3, 3, 3))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);
        scene.overlay().showText(70)
                .text("Every brick adds four coking slots: bigger ovens coke more at once")
                .pointAt(util.vector().topOf(2, 3, 2))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(burners, Direction.UP);
        scene.idle(15);
        for (int x = 2; x <= 3; x++) {
            for (int y = 2; y <= 3; y++) {
                for (int z = 2; z <= 3; z++) {
                    scene.world().modifyBlock(new BlockPos(x, y, z),
                            st -> st.setValue(CokeOvenBlock.LIT, true).setValue(CokeOvenBlock.WORKING, true), false);
                }
            }
        }
        scene.overlay().showText(90)
                .text("It needs heat below every column. Fire or magma works at half speed, a Blaze Burner at full speed, superheated at double")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.world().showSection(in, Direction.EAST);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(1, 3, 2), Pointing.DOWN, 50).withItem(new ItemStack(Items.COAL));
        scene.overlay().showText(70)
                .text("Funnels, hoppers, belts and arms feed it from any side")
                .pointAt(util.vector().blockSurface(new BlockPos(1, 3, 2), Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);
        scene.world().showSection(out, Direction.WEST);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(4, 2, 3), Pointing.DOWN, 50).withItem(new ItemStack(ModItems.COAL_COKE.get()));
        scene.overlay().showText(70)
                .text("Coal comes out as Coal Coke, logs as Charcoal. Pulling from it only ever takes finished items. By hand, right-click with an empty hand")
                .pointAt(util.vector().topOf(4, 2, 3))
                .placeNearTarget();
        scene.idle(80);
        scene.overlay().showText(80)
                .text("While it cokes, smoke rises from the top and it crackles. Embers along the bottom just mean it has heat")
                .pointAt(util.vector().topOf(3, 3, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showOutline(PonderPalette.OUTPUT, "creosote", oven, 80);
        scene.overlay().showText(90)
                .text("Coking also gives Creosote Oil. Pump it, or use a bucket, on any brick. When the tank is full the rest vents in a dark burst, and the oven keeps going")
                .pointAt(util.vector().topOf(2, 3, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);
        scene.markAsFinished();
    }
}
