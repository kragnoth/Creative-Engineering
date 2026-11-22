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
        // pumpjack: shaft + linkage fallback (instanced visual below)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.PUMPJACK.get(),
                com.kragnoth.creativeengineering.client.petroleum.PumpjackRenderer::new);
        // rocker arms: renderer fallback (skips itself when Flywheel instancing is on)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_CYLINDER.get(),
                com.kragnoth.creativeengineering.client.engine.CylinderRenderer::new);
    }

    /** Atlas source that steel-shifts Create's tank textures for the Fermenting Vat (assets/minecraft/atlases/blocks.json). */
    @SubscribeEvent
    static void spriteSources(net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent event) {
        com.kragnoth.creativeengineering.client.texture.RecolorSpriteSource.TYPE = event.register(
                CreativeEngineering.id("recolor"), com.kragnoth.creativeengineering.client.texture.RecolorSpriteSource.CODEC);
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
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
        // the pumpjack: Create's shaft through its gearbox, the nodding beam and its linkage
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.PUMPJACK.get())
                .factory(com.kragnoth.creativeengineering.client.petroleum.PumpjackVisual::new)
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
