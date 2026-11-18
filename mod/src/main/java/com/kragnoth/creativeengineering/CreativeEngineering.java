package com.kragnoth.creativeengineering;

import com.kragnoth.creativeengineering.registry.ModArmorMaterials;
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

    public CreativeEngineering(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
