package com.kragnoth.creativeengineering.client;

import com.kragnoth.creativeengineering.CreativeEngineering;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CreativeEngineering.MODID, value = Dist.CLIENT)
public final class CEClient {
    static {
        com.kragnoth.creativeengineering.client.engine.CEPartials.init();   // partial models must exist before baking
        com.kragnoth.creativeengineering.client.storage.SteelPipeModel.PARTS.size();   // (steel pipe arms too)
    }

    private CEClient() {}

    /** Create draws filter slots (and their items) through SmartBlockEntityRenderer; without one they're invisible. */
    @SubscribeEvent
    static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.ALLOY_FURNACE.get(),
                com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer::new);
        // the flywheel is drawn spinning about its shaft, like any Create kinetic block
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_FLYWHEEL.get(),
                com.kragnoth.creativeengineering.client.engine.DriveBeltRenderer::new);
        // steel storage tank: Create's own tank renderer (fluid inside)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.STEEL_TANK.get(),
                ctx -> (net.minecraft.client.renderer.blockentity.BlockEntityRenderer) new com.simibubi.create.content.fluids.tank.FluidTankRenderer(ctx));
        // distilling tank dressing: fallback renderer (instanced visual below)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DISTILLING_TANK.get(),
                com.kragnoth.creativeengineering.client.distilling.DistillingTankRenderer::new);
        // drive pulley: same renderer (wheel fallback; the belt itself is drawn by the flywheel end)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DRIVE_PULLEY.get(),
                com.kragnoth.creativeengineering.client.engine.DriveBeltRenderer::new);
        // crankshaft: renderer fallback (Create's renderer skips itself when Flywheel instancing is on)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_ENGINE.get(),
                com.kragnoth.creativeengineering.client.engine.CrankshaftRenderer::new);
        // pumpjack: gearbox shaft + cranks, and the beam bearing's beam and linkage (fallbacks; instanced visuals below)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.PUMPJACK.get(),
                com.kragnoth.creativeengineering.client.petroleum.PumpjackRenderer::new);
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.BEAM_BEARING.get(),
                com.kragnoth.creativeengineering.client.petroleum.BeamBearingRenderer::new);
        // rocker arms: renderer fallback (skips itself when Flywheel instancing is on)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_CYLINDER.get(),
                com.kragnoth.creativeengineering.client.engine.CylinderRenderer::new);
    }

    /** Drink bottles: the liquid layer (tint 0, vanilla's potion overlay) takes the drink's fluid colour. */
    @SubscribeEvent
    static void itemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        for (var drink : com.kragnoth.creativeengineering.registry.ModItems.DRINKS.values()) {
            event.register((stack, layer) -> {
                if (layer != 0) {
                    return -1;
                }
                for (var t : com.kragnoth.creativeengineering.registry.ModFluids.ALL) {
                    if (t.name.equals(((com.kragnoth.creativeengineering.content.drinks.DrinkItem) stack.getItem()).fluid)) {
                        return 0xFF000000 | t.tint;
                    }
                }
                return -1;
            }, drink.get());
        }
    }

    /** Atlas source that steel-shifts Create's tank textures for the Fermenting Vat (assets/minecraft/atlases/blocks.json). */
    @SubscribeEvent
    static void spriteSources(net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent event) {
        com.kragnoth.creativeengineering.client.texture.RecolorSpriteSource.TYPE = event.register(
                CreativeEngineering.id("recolor"), com.kragnoth.creativeengineering.client.texture.RecolorSpriteSource.CODEC);
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        // Create's "Hold [Shift]" item descriptions (lang: <id>.tooltip.summary / conditionN / behaviourN / controlN /
        // actionN) for every item of ours, plus Create's stress / capacity lines on kinetic blocks
        event.enqueueWork(() -> {
            for (var holder : com.kragnoth.creativeengineering.registry.ModItems.ITEMS.getEntries()) {
                net.minecraft.world.item.Item item = holder.get();
                com.simibubi.create.foundation.item.TooltipModifier.REGISTRY.register(item,
                        new com.simibubi.create.foundation.item.ItemDescription.Modifier(item, net.createmod.catnip.lang.FontHelper.Palette.STANDARD_CREATE)
                                .andThen(com.simibubi.create.foundation.item.TooltipModifier.mapNull(com.simibubi.create.foundation.item.KineticStats.create(item))));
            }
        });
        // Ponder ships with Create; PonderCompat is only touched behind this check (same pattern as Tarbound).
        if (ModList.get().isLoaded("ponder")) {
            event.enqueueWork(com.kragnoth.creativeengineering.compat.ponder.PonderCompat::init);
        }
        // instanced rocker arms (Flywheel), as Create's own visuals; the renderer stays registered as the fallback
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_CYLINDER.get())
                .factory(com.kragnoth.creativeengineering.client.engine.CylinderVisual::new)
                .skipVanillaRender(be -> false)
                .apply());
        // the pumpjack: Create's shaft and the cranks through its gearbox; the nodding beam and linkage on its walking beam bearing
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.PUMPJACK.get())
                .factory(com.kragnoth.creativeengineering.client.petroleum.PumpjackVisual::new)
                .skipVanillaRender(be -> false)
                .apply());
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.BEAM_BEARING.get())
                .factory(com.kragnoth.creativeengineering.client.petroleum.BeamBearingVisual::new)
                .skipVanillaRender(be -> false)
                .apply());
        // the crankshaft spins inside the crankcase like any Create shaft (model built along X)
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_ENGINE.get())
                .factory((ctx, be, pt) -> new com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual<>(ctx, be, pt,
                        net.minecraft.core.Direction.EAST,
                        dev.engine_room.flywheel.lib.model.Models.partial(com.kragnoth.creativeengineering.client.engine.CEPartials.CRANKSHAFT)))
                .skipVanillaRender(be -> false)
                .apply());
        // the distilling tank's pipes, collars, gauge and valve: static instances
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.DISTILLING_TANK.get())
                .factory(com.kragnoth.creativeengineering.client.distilling.DistillingTankVisual::new)
                .skipVanillaRender(be -> false)
                .apply());
        // the drive sprocket is Create's own small cogwheel (modelled upright), spun like any Create cog
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.DRIVE_PULLEY.get())
                .factory(com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual.of(com.simibubi.create.AllPartialModels.COGWHEEL))
                .skipVanillaRender(be -> false)
                .apply());
        // the flywheel spins as an instanced visual (Create's renderer skips itself when instancing is on); its model
        // is built along X, so it rotates from EAST, like Create's pump cog uses ofZ
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_FLYWHEEL.get())
                .factory((ctx, be, pt) -> new com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual<>(ctx, be, pt,
                        net.minecraft.core.Direction.EAST,
                        dev.engine_room.flywheel.lib.model.Models.partial(com.kragnoth.creativeengineering.client.engine.CEPartials.FLYWHEEL)))
                .skipVanillaRender(be -> false)
                .apply());
    }
}
