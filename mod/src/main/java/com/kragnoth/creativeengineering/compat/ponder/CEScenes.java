package com.kragnoth.creativeengineering.compat.ponder;

import net.minecraft.world.phys.Vec3;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.kragnoth.creativeengineering.registry.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlock;
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

    /**
     * "blast_furnace" (5x5): a blaze burner at (2,1,2) under a 1x1x3 stack (2,2..4,2); a funnel charging the top at
     * (2,5,2); an encased fan at (1,2,2) blowing east into the bottom layer; an extracting funnel at (3,2,2).
     */
    public static void blastFurnace(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("blast_furnace", "Cast Iron and Steel in a Blast Furnace");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        Selection burner = util.select().position(2, 1, 2);
        Selection stack = util.select().fromTo(2, 2, 2, 2, 4, 2);
        scene.world().showSection(burner, Direction.DOWN);
        scene.world().showSection(stack, Direction.DOWN);
        scene.idle(15);
        for (int y = 2; y <= 4; y++) {
            scene.world().modifyBlock(new BlockPos(2, y, 2),
                    st -> st.setValue(BlastFurnaceBlock.LIT, true).setValue(BlastFurnaceBlock.WORKING, true), false);
        }
        scene.overlay().showText(90)
                .text("Blast Furnace Bricks stack into one furnace, up to 3 wide and 6 tall. It needs a heated Blaze Burner below")
                .pointAt(util.vector().topOf(2, 4, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().position(2, 5, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(2, 5, 2), Pointing.DOWN, 50).withItem(new ItemStack(Items.IRON_INGOT));
        scene.overlay().showText(80)
                .text("Feed iron and fuel from any side. Coke smelts 2 iron, charcoal 1, coal half")
                .pointAt(util.vector().topOf(2, 5, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(90);
        scene.overlay().showControls(util.vector().topOf(2, 4, 2), Pointing.DOWN, 50).withItem(new ItemStack(ModItems.CAST_IRON_INGOT.get()));
        scene.overlay().showText(70)
                .text("On its own it makes Cast Iron")
                .pointAt(util.vector().topOf(2, 4, 2)).placeNearTarget();
        scene.idle(80);
        scene.world().showSection(util.select().position(1, 2, 2), Direction.EAST);
        scene.idle(10);
        scene.overlay().showText(90)
                .text("Encased Fans blowing into the bottom layer give it an air blast: now it makes Steel. Each extra fan makes it a quarter faster")
                .pointAt(util.vector().centerOf(1, 2, 2)).placeNearTarget().attachKeyFrame();
        scene.overlay().showControls(util.vector().topOf(2, 4, 2), Pointing.DOWN, 60).withItem(new ItemStack(ModItems.STEEL_INGOT.get()));
        scene.idle(100);
        scene.world().showSection(util.select().position(3, 2, 2), Direction.WEST);
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Take out metal and slag from any side. Slag is a bonus: when it's full it's thrown away, never blocking the furnace")
                .pointAt(util.vector().topOf(3, 2, 2)).placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }

    /**
     * "alloy_furnace" (6x6): four burners at (2..3,1,2..3) under a 2x2x2 furnace (2..3,2..3,2..3); a funnel on top
     * at (2,4,2); a fan at (1,2,2) blowing east; an extracting funnel at (4,2,3).
     */
    public static void alloyFurnace(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("alloy_furnace", "Bulk Alloys in an Alloy Furnace");
        scene.configureBasePlate(0, 0, 6);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 3, 1, 3), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(2, 2, 2, 3, 3, 3), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .text("Alloy Furnace Bricks form a furnace from any solid box, 2 to 4 blocks on each side")
                .pointAt(util.vector().topOf(3, 3, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.overlay().showText(90)
                .text("Every column needs a heated Blaze Burner below. Superheat them all for the toughest alloys")
                .pointAt(util.vector().centerOf(2, 1, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().position(2, 4, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(2, 4, 2), Pointing.DOWN, 50).withItem(new ItemStack(Items.COPPER_INGOT));
        scene.overlay().showText(90)
                .text("Feed ingredients and coke from any side. Half the bricks' count of batches run at once")
                .pointAt(util.vector().topOf(2, 4, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().position(1, 2, 2), Direction.EAST);
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Fans blowing into the bottom layer are optional: each one makes it a quarter faster")
                .pointAt(util.vector().centerOf(1, 2, 2)).placeNearTarget();
        scene.idle(90);
        scene.world().showSection(util.select().position(4, 2, 3), Direction.WEST);
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Take the alloys out from any side. Create's heated alloys and Ender IO's alloys work here too")
                .pointAt(util.vector().topOf(4, 2, 3)).placeNearTarget();
        scene.idle(90);
        scene.world().modifyBlock(new BlockPos(3, 2, 3), st -> st.setValue(com.kragnoth.creativeengineering.content.shared.Port.PROPERTY,
                com.kragnoth.creativeengineering.content.shared.Port.OUTPUT), false);
        scene.overlay().showControls(util.vector().blockSurface(new BlockPos(3, 2, 3), Direction.EAST), Pointing.RIGHT, 60)
                .withItem(new ItemStack(Items.IRON_INGOT));
        scene.overlay().showText(100)
                .text("When alloys share ingredients, wrench a brick to an output hatch and set its filter: only matching alloys are made")
                .pointAt(util.vector().blockSurface(new BlockPos(3, 2, 3), Direction.EAST)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        scene.markAsFinished();
    }

    /** "press_dies" (5x5): depot (2,1,2), press (2,3,2) facing north, shafts (2,3,3..4). */
    public static void pressDies(SceneBuilder builder, SceneBuildingUtil util) {
        com.simibubi.create.foundation.ponder.CreateSceneBuilder scene = new com.simibubi.create.foundation.ponder.CreateSceneBuilder(builder);
        scene.title("press_dies", "Shaping Metal with Press Dies");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        BlockPos depot = new BlockPos(2, 1, 2), press = new BlockPos(2, 3, 2);
        scene.world().showSection(util.select().position(depot), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(2, 3, 2, 2, 3, 4), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().fromTo(2, 3, 2, 2, 3, 4), 32);
        scene.idle(15);
        Vec3 side = util.vector().blockSurface(press, Direction.WEST);
        scene.overlay().showControls(side, Pointing.LEFT, 50).rightClick().withItem(new ItemStack(ModItems.ROD_DIE.get()));
        scene.overlay().showText(90).text("Right-click a Mechanical Press with a die to fit it. Sneak-right-click with an empty hand takes it out")
                .pointAt(side).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        ItemStack in = new ItemStack(ModItems.STEEL_INGOT.get());
        scene.world().createItemOnBeltLike(depot, Direction.NORTH, in);
        scene.idle(10);
        scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class, p -> p.getPressingBehaviour().start(PressingBehaviour.Mode.BELT));
        scene.idle(30);
        scene.world().removeItemsFromBelt(depot);
        ItemStack out = new ItemStack(ModItems.STEEL_ROD.get(), 2);
        scene.world().createItemOnBeltLike(depot, Direction.UP, out);
        scene.overlay().showControls(util.vector().topOf(depot), Pointing.UP, 50).withItem(out);
        scene.overlay().showText(90).text("With a die fitted, the press only shapes what that die makes: rods or gears. It works on belts and depots as usual")
                .pointAt(util.vector().topOf(depot)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.overlay().showText(80).text("Dies never wear out. Without a die the press is an ordinary press again")
                .pointAt(side).placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }
}
