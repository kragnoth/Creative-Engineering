package com.kragnoth.creativeengineering.compat.ponder;

import com.kragnoth.creativeengineering.registry.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
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
}
