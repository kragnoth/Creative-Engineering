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
        // rocker arms: renderer fallback (skips itself when Flywheel instancing is on)
        event.registerBlockEntityRenderer(com.kragnoth.creativeengineering.registry.ModBlockEntities.DIESEL_CYLINDER.get(),
                com.kragnoth.creativeengineering.client.engine.CylinderRenderer::new);
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
    }
}
