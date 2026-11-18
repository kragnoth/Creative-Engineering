package com.kragnoth.creativeengineering.client;

import com.kragnoth.creativeengineering.CreativeEngineering;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CreativeEngineering.MODID, value = Dist.CLIENT)
public final class CEClient {
    private CEClient() {}

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        // Ponder ships with Create; PonderCompat is only touched behind this check (same pattern as Tarbound).
        if (ModList.get().isLoaded("ponder")) {
            event.enqueueWork(com.kragnoth.creativeengineering.compat.ponder.PonderCompat::init);
        }
    }
}
