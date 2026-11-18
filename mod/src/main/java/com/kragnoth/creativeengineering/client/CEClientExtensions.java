package com.kragnoth.creativeengineering.client;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.registry.ModFluids;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = CreativeEngineering.MODID, value = Dist.CLIENT)
public final class CEClientExtensions {
    private CEClientExtensions() {}

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ResourceLocation still = CreativeEngineering.id("block/creosote_still");
        ResourceLocation flow = CreativeEngineering.id("block/creosote_flow");
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return still;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flow;
            }
        }, ModFluids.CREOSOTE_TYPE.get());
    }
}
