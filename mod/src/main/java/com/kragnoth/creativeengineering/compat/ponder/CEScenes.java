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

    /**
     * "diesel_engine" (7x7): crankcases (2..4,1,3) facing east; cylinders (2..4,2,4) south and (2..4,2,2) north;
     * Create tank (3,1,2); Create hand crank (1,1,3); flywheel (5,1,3); Create shaft (6,1,3).
     */
    public static void dieselEngine(SceneBuilder builder, SceneBuildingUtil util) {
        com.simibubi.create.foundation.ponder.CreateSceneBuilder scene = new com.simibubi.create.foundation.ponder.CreateSceneBuilder(builder);
        scene.title("diesel_engine", "Building and Starting a Diesel Engine");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);
        Selection crank = util.select().fromTo(2, 1, 3, 4, 1, 3);
        scene.world().showSection(crank, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .text("Crankcases in a line, all facing the same way, make one engine. The back end is where power comes out")
                .pointAt(util.vector().topOf(4, 1, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().fromTo(2, 2, 4, 4, 2, 4), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(2, 2, 2, 4, 2, 2), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .text("Cylinders sit diagonally above, leaning out: up to two per crankcase. Each one adds stress capacity")
                .pointAt(util.vector().centerOf(3, 2, 4)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().position(3, 1, 2), Direction.SOUTH);
        scene.idle(15);
        scene.overlay().showText(100)
                .text("A tank against a crankcase side feeds it directly, no pump. Creosote works; plant oil, ethanol and better fuels give more")
                .pointAt(util.vector().centerOf(3, 1, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        scene.world().showSection(util.select().position(1, 1, 3), Direction.EAST);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(1, 1, 3), Pointing.DOWN, 40).rightClick();
        scene.overlay().showText(90)
                .text("To start it, turn whatever is at the front: a Hand Crank works. The front never connects to the output")
                .pointAt(util.vector().centerOf(1, 1, 3)).placeNearTarget().attachKeyFrame();
        scene.world().setKineticSpeed(util.select().position(1, 1, 3), 32);
        scene.idle(40);
        scene.world().setKineticSpeed(util.select().position(1, 1, 3), 0);
        for (int x = 2; x <= 4; x++) {
            for (int z : new int[]{2, 4}) {
                scene.world().modifyBlock(new BlockPos(x, 2, z),
                        st -> st.setValue(com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock.RUNNING, true), false);
            }
        }
        scene.world().showSection(util.select().fromTo(5, 1, 3, 6, 1, 3), Direction.WEST);
        scene.world().setKineticSpeed(util.select().fromTo(2, 1, 3, 6, 1, 3), 64);
        scene.idle(60);
        scene.overlay().showText(100)
                .text("Once it fires it runs on its own at 64 RPM. Take power off the flywheel shaft; it stops when the fuel runs out")
                .pointAt(util.vector().centerOf(6, 1, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        scene.markAsFinished();
    }

    // ---------------------------------------------------------------------------------------------- 0.20.0
    private static void show(SceneBuilder scene, Selection sel, Direction from, int wait) {
        scene.world().showSection(sel, from);
        scene.idle(wait);
    }

    /** "campfire_stand" (5x5): fan (1,1,2) east through a lit stand (2,1,2) onto a depot (3,1,2); soul stand (2,1,4). */
    public static void campfireStand(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("campfire_stand", "Campfire Stands: Heat and Fan Catalysts");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        show(scene, util.select().position(2, 1, 2), Direction.DOWN, 15);
        scene.overlay().showControls(util.vector().topOf(2, 1, 2), Pointing.DOWN, 40).withItem(new ItemStack(Items.CAMPFIRE));
        scene.overlay().showText(90)
                .text("A Campfire Stand holds a campfire under your machines. Right-click it with a campfire to fit one")
                .pointAt(util.vector().topOf(2, 1, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.overlay().showText(90)
                .text("Lit, it gives passive heat to a Fermenting Vat, a Distilling Tank or a Create boiler above it")
                .pointAt(util.vector().centerOf(2, 1, 2)).placeNearTarget();
        scene.idle(100);
        show(scene, util.select().position(1, 1, 2), Direction.EAST, 5);
        show(scene, util.select().position(3, 1, 2), Direction.DOWN, 15);
        scene.overlay().showOutline(PonderPalette.WHITE, "smoke", util.select().fromTo(2, 1, 2, 3, 1, 2), 80);
        scene.overlay().showText(90)
                .text("Air blows straight through a stand. Through a lit campfire, a fan's stream smokes the items it reaches")
                .pointAt(util.vector().topOf(3, 1, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        show(scene, util.select().position(2, 1, 4), Direction.DOWN, 15);
        scene.overlay().showText(80)
                .text("With a soul campfire the same stream haunts them instead")
                .pointAt(util.vector().topOf(2, 1, 4)).placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }

    /** "fermenting_vat" (6x6): stands (2..3,1,2..3), vat (2..3,2..3,2..3), funnel (1,3,2), pipe (4,2,3), tank (5,1..2,3). */
    public static void fermentingVat(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("fermenting_vat", "Fermenting Drinks in a Vat");
        scene.configureBasePlate(0, 0, 6);
        scene.showBasePlate();
        scene.idle(10);
        show(scene, util.select().fromTo(2, 2, 2, 3, 3, 3), Direction.DOWN, 15);
        scene.overlay().showText(90)
                .text("Fermenting Vats placed together form one vat, up to 3 wide and 4 tall, like a Create tank")
                .pointAt(util.vector().topOf(3, 3, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        show(scene, util.select().fromTo(2, 1, 2, 3, 1, 3), Direction.UP, 15);
        scene.overlay().showText(90)
                .text("It needs heat underneath. A lit Campfire Stand is enough; a lit Blaze Burner is twice as fast, superheated three times")
                .pointAt(util.vector().centerOf(2, 1, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        show(scene, util.select().position(1, 3, 2), Direction.EAST, 15);
        scene.overlay().showControls(util.vector().topOf(1, 3, 2), Pointing.DOWN, 50).withItem(new ItemStack(Items.APPLE, 4));
        scene.overlay().showText(90)
                .text("Add water and the ingredients by pipe, funnel or hand: apples make cider, berries wine, honey mead, wheat ale, potatoes hooch")
                .pointAt(util.vector().topOf(1, 3, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        show(scene, util.select().fromTo(4, 1, 3, 5, 2, 3), Direction.WEST, 15);
        scene.overlay().showText(90)
                .text("Pipe the finished drink out of any side, or bucket it. Bottle it with a Spout to drink it")
                .pointAt(util.vector().centerOf(4, 2, 3)).placeNearTarget();
        scene.idle(100);
        scene.markAsFinished();
    }

    /** "distilling" (7x7): stand (3,1,3), column (3,2..4,3), taps (4,3,3) east and (2,4,3) west, pipes, tanks (5,..) and (1,..). */
    public static void distilling(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("distilling", "Distilling with a Column and Taps");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);
        show(scene, util.select().position(3, 1, 3), Direction.DOWN, 5);
        show(scene, util.select().fromTo(3, 2, 3, 3, 4, 3), Direction.DOWN, 15);
        scene.overlay().showText(90)
                .text("Distilling Tanks stacked on heat form a column, 1 to 3 wide and up to 8 tall")
                .pointAt(util.vector().topOf(3, 4, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.overlay().showOutline(PonderPalette.RED, "boiler", util.select().position(3, 2, 3), 80);
        scene.overlay().showText(90)
                .text("The bottom is the boiler: pipe the brew in there. Each level above collects one fraction, the heaviest lowest")
                .pointAt(util.vector().centerOf(3, 2, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        show(scene, util.select().position(4, 3, 3), Direction.WEST, 15);
        scene.overlay().showControls(util.vector().topOf(4, 3, 3), Pointing.DOWN, 40).rightClick();
        scene.overlay().showText(90)
                .text("A Tap on a level drains it. Right-click to open or close it; redstone holds it shut")
                .pointAt(util.vector().centerOf(4, 3, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        show(scene, util.select().fromTo(5, 1, 3, 5, 3, 3), Direction.WEST, 15);
        scene.overlay().showText(100)
                .text("It pours down Steel Pipe into any tank at its own height or lower: gravity does the work, no pump needed")
                .pointAt(util.vector().centerOf(5, 2, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        show(scene, util.select().fromTo(1, 1, 3, 2, 4, 3), Direction.EAST, 15);
        scene.overlay().showText(90)
                .text("One Tap per level splits the run: hooch gives vodka and ethanol; crude oil, on a Blaze Burner, gives four fuels")
                .pointAt(util.vector().centerOf(2, 4, 3)).placeNearTarget();
        scene.idle(100);
        scene.markAsFinished();
    }

    /** "drive_chain" (7x7): motor (1,1,3) east, shaft (2,1,3), flywheel (3,1,3); sprocket (3,4,3), shaft (4,4,3), cog (5,4,3). */
    public static void driveChain(SceneBuilder builder, SceneBuildingUtil util) {
        com.simibubi.create.foundation.ponder.CreateSceneBuilder scene = new com.simibubi.create.foundation.ponder.CreateSceneBuilder(builder);
        scene.title("drive_chain", "Drive Chains: Flywheels and Sprockets");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 3, 3, 1, 3), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().fromTo(1, 1, 3, 3, 1, 3), 16);
        scene.idle(15);
        scene.world().showSection(util.select().fromTo(3, 4, 3, 5, 4, 3), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .text("A Drive Chain links a Flywheel and a Drive Sprocket on parallel shafts, 2 to 8 blocks apart")
                .pointAt(util.vector().centerOf(3, 4, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.overlay().showControls(util.vector().centerOf(3, 1, 3), Pointing.LEFT, 30).rightClick().withItem(new ItemStack(ModItems.DRIVE_BELT.get()));
        scene.idle(35);
        scene.overlay().showControls(util.vector().centerOf(3, 4, 3), Pointing.LEFT, 30).rightClick().withItem(new ItemStack(ModItems.DRIVE_BELT.get()));
        scene.idle(30);
        scene.world().modifyBlockEntityNBT(util.select().position(3, 1, 3), com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.class, nbt -> {
            net.minecraft.nbt.ListTag l = new net.minecraft.nbt.ListTag();
            l.add(net.minecraft.nbt.NbtUtils.writeBlockPos(new BlockPos(0, 3, 0)));
            nbt.put("Chains", l);
        });
        scene.world().modifyBlockEntityNBT(util.select().position(3, 4, 3), com.kragnoth.creativeengineering.content.engine.BeltWheelBlockEntity.class, nbt -> {
            net.minecraft.nbt.ListTag l = new net.minecraft.nbt.ListTag();
            l.add(net.minecraft.nbt.NbtUtils.writeBlockPos(new BlockPos(0, -3, 0)));
            nbt.put("Chains", l);
        });
        scene.world().setKineticSpeed(util.select().fromTo(3, 4, 3, 5, 4, 3), 64);
        scene.overlay().showText(90)
                .text("Right-click one wheel with the chain, then the other. Flywheel to sprocket steps speed up four times")
                .pointAt(util.vector().centerOf(3, 4, 3)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.overlay().showText(90)
                .text("It is a real kinetic link: stress passes along it. A sprocket can take a second chain to relay power on")
                .pointAt(util.vector().centerOf(5, 4, 3)).placeNearTarget();
        scene.idle(100);
        scene.markAsFinished();
    }

    /** "pumpjack" (9x9): gearbox (2,1,1) south, motor (3,1,1); girders (2,1..2,2), bearing (2,3,2); Wellhead (2,1,4); pipe (3,1,4); tank (4,1..2,4). */
    public static void pumpjack(SceneBuilder builder, SceneBuildingUtil util) {
        com.simibubi.create.foundation.ponder.CreateSceneBuilder scene = new com.simibubi.create.foundation.ponder.CreateSceneBuilder(builder);
        scene.title("pumpjack", "Prospecting and Pumping Oil");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(6, 0, 6), Pointing.DOWN, 50).rightClick().withItem(new ItemStack(ModItems.SEISMIC_PROBE.get()));
        scene.overlay().showText(90)
                .text("Use a Seismic Probe on the ground: it hears an oil vein in this chunk, or tells you roughly where the nearest one is")
                .pointAt(util.vector().topOf(6, 0, 6)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().position(2, 1, 1), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .text("On a vein chunk, start with a Crank Gearbox. It faces away from you and takes a shaft on either side")
                .pointAt(util.vector().topOf(2, 1, 1)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2, 3, 2), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(100)
                .text("Build a column of 2 to 6 girders or industrial iron 1 to 3 blocks ahead, and put a Walking Beam Bearing on top")
                .pointAt(util.vector().topOf(2, 3, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        scene.world().showSection(util.select().position(2, 1, 4), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(100)
                .text("A Wellhead goes 1 to 6 blocks past the column. A longer reach strokes deeper, for more oil and more stress")
                .pointAt(util.vector().topOf(2, 1, 4)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        scene.overlay().showText(80)
                .text("A long reach swings the horsehead low: it needs a taller column to clear the well")
                .pointAt(util.vector().topOf(2, 3, 2)).placeNearTarget();
        scene.idle(90);
        scene.world().showSection(util.select().position(3, 1, 1), Direction.WEST);
        scene.idle(10);
        scene.world().setKineticSpeed(util.select().fromTo(2, 1, 1, 3, 1, 1), 64);
        scene.idle(20);
        scene.overlay().showText(90)
                .text("Turn the gearbox and the beam nods. Full stroke at 64 RPM; nothing is formed, every part is its own block")
                .pointAt(util.vector().centerOf(2, 1, 1)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.world().showSection(util.select().fromTo(3, 1, 4, 4, 2, 4), Direction.WEST);
        scene.idle(15);
        scene.overlay().showText(100)
                .text("The Wellhead pushes into pipes on any side, no pump needed. Off a vein it brings up water, in the Nether lava")
                .pointAt(util.vector().centerOf(3, 1, 4)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        scene.overlay().showControls(util.vector().topOf(2, 1, 1), Pointing.DOWN, 40).rightClick();
        scene.overlay().showText(80)
                .text("Right-click the gearbox with an empty hand to outline where the parts go")
                .pointAt(util.vector().topOf(2, 1, 1)).placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }

    /** "lubrication" (5x5): creative motor (1,2,2) east, shaft (2,2,2), press (3,2,2) over a depot (3,1,2). */
    public static void lubrication(SceneBuilder builder, SceneBuildingUtil util) {
        com.simibubi.create.foundation.ponder.CreateSceneBuilder scene = new com.simibubi.create.foundation.ponder.CreateSceneBuilder(builder);
        scene.title("lubrication", "Lubricating Create Machines");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 3, 2, 2), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().fromTo(1, 2, 2, 3, 2, 2), 32);
        scene.idle(15);
        scene.overlay().showControls(util.vector().topOf(3, 2, 2), Pointing.DOWN, 50).rightClick().withItem(new ItemStack(ModItems.OIL_CAN.get()));
        scene.overlay().showText(90)
                .text("Use an Oil Can of lubricant on any Create machine to oil it")
                .pointAt(util.vector().topOf(3, 2, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(100);
        scene.overlay().showText(100)
                .text("For a Minecraft day it weighs a quarter less on its network: more machines on the same power. Goggles show the time left")
                .pointAt(util.vector().centerOf(3, 2, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(110);
        scene.overlay().showText(90)
                .text("Lubricant is heavy oil mixed with kerosene. Fill the can with a Spout, or use it on a tank; a Deployer can use it too")
                .pointAt(util.vector().centerOf(1, 2, 2)).placeNearTarget();
        scene.idle(100);
        scene.markAsFinished();
    }
}
