package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.registry.ModItems;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** JEI integration. Only loaded by JEI itself, so nothing here runs without it. */
@JeiPlugin
public class CEJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return CreativeEngineering.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CokingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        registration.addRecipes(CokingCategory.TYPE, level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.COKING_TYPE.get()).stream().map(RecipeHolder::value).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.COKE_OVEN_BRICKS.get()), CokingCategory.TYPE);
    }
}
