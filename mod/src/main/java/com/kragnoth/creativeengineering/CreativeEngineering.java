package com.kragnoth.creativeengineering;

import com.kragnoth.creativeengineering.registry.ModArmPoints;
import com.kragnoth.creativeengineering.registry.ModArmorMaterials;
import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.kragnoth.creativeengineering.registry.ModFluids;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.kragnoth.creativeengineering.registry.ModCreativeTabs;
import com.kragnoth.creativeengineering.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Creative Engineering: Immersive-style industry built the Create way (rotation only, blaze-burner heat,
 * base Create logistics). Design docs live in the repo's docs/ folder; read docs/create-core-concepts.md first.
 */
@Mod(CreativeEngineering.MODID)
public final class CreativeEngineering {
    public static final String MODID = "creativeengineering";
    public static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public CreativeEngineering(IEventBus modBus, net.neoforged.fml.ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, CEConfig.CLIENT_SPEC);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.kragnoth.creativeengineering.demo.DemoCommands::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.kragnoth.creativeengineering.content.petroleum.WellSources::register);   // data/*/well_sources   // /test rundistill
        ModFluids.init(); // its block and bucket live in the BLOCKS / ITEMS registers below
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModItems.COMPONENTS.register(modBus);
        ModFluids.TYPES.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModArmPoints.TYPES.register(modBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(ModBlockEntities::registerCapabilities);
        modBus.addListener(com.kragnoth.creativeengineering.content.blastfurnace.BlastFuel::register);
        modBus.addListener(com.kragnoth.creativeengineering.content.engine.EngineFuel::register);
        com.kragnoth.creativeengineering.compat.create.CEDisplaySources.register(modBus);
        // Campfire Stands are passive heat for Create boilers and the Fermenting Vat while lit
        modBus.addListener(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent.class, e -> e.enqueueWork(() -> {
            // Pumpjack: 8 SU per RPM (512 SU at its full-rate 64 RPM)
            com.simibubi.create.api.stress.BlockStressValues.IMPACTS.register(ModBlocks.PUMPJACK.get(), () -> 8.0);
            for (var stand : java.util.List.of(ModBlocks.ANDESITE_CAMPFIRE_STAND, ModBlocks.BRASS_CAMPFIRE_STAND, ModBlocks.COPPER_CAMPFIRE_STAND)) {
                com.simibubi.create.api.boiler.BoilerHeater.REGISTRY.register(stand.get(),
                        com.kragnoth.creativeengineering.content.heat.CampfireStandBlock.HEATER);
            }
        }));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
