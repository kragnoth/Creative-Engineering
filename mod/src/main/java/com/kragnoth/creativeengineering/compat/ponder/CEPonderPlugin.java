package com.kragnoth.creativeengineering.compat.ponder;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.registry.ModItems;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Hold the Ponder key over Creative Engineering items to watch how they're made. */
public class CEPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreativeEngineering.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModItems.CARBON_PACKED_IRON.getId(), ModItems.BLISTER_STEEL_INGOT.getId())
                .addStoryBoard("cementation", CEScenes::cementation);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        ResourceLocation tag = CreativeEngineering.id(CreativeEngineering.MODID);
        helper.registerTag(tag)
                .addToIndex()
                .item(ModItems.BLISTER_STEEL_INGOT.get(), true, false)
                .title("Creative Engineering")
                .description("Steel and heavy industry, the Create way")
                .register();
        helper.addToTag(tag)
                .add(ModItems.CARBON_PACKED_IRON.getId())
                .add(ModItems.BLISTER_STEEL_INGOT.getId());
    }
}
