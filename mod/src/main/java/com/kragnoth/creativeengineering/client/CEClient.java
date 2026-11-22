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
    }

    private CEClient() {}

    /** Create draws filter slots (and their items) through SmartBlockEntityRenderer; without one they're invisible. */
    @SubscribeEvent
    static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.ALLOY_FURNACE.get(),
                com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer::new);
        // the flywheel is drawn spinning about its shaft, like any Create kinetic block
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_FLYWHEEL.get(),
                com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer::new);
        // crankshaft: renderer fallback (Create's renderer skips itself when Flywheel instancing is on)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_ENGINE.get(),
                com.kragnoth.creativeengineering.client.engine.CrankshaftRenderer::new);
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
        // the crankshaft spins inside the crankcase like any Create shaft (model built along X)
        event.enqueueWork(() -> dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
                .builder(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_ENGINE.get())
                .factory((ctx, be, pt) -> new com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual<>(ctx, be, pt,
                        net.minecraft.core.Direction.EAST,
                        dev.engine_room.flywheel.lib.model.Models.partial(com.kragnoth.creativeengineering.client.engine.CEPartials.CRANKSHAFT)))
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
