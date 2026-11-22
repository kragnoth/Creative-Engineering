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
        registration.addRecipeCategories(new CokingCategory(registration.getJeiHelpers().getGuiHelper()),
                new BlastSmeltingCategory(registration.getJeiHelpers().getGuiHelper()),
                new AlloyingCategory(registration.getJeiHelpers().getGuiHelper()),
                new DiePressingCategory(registration.getJeiHelpers().getGuiHelper()),
                new EngineFuelCategory(registration.getJeiHelpers().getGuiHelper()),
                new FermentingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        registration.addRecipes(CokingCategory.TYPE, level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.COKING_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(BlastSmeltingCategory.TYPE, level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.BLAST_SMELTING_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(EngineFuelCategory.TYPE, EngineFuelCategory.entries());
        registration.addRecipes(FermentingCategory.TYPE, level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.FERMENTING_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(DiePressingCategory.TYPE, level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.DIE_PRESSING_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(AlloyingCategory.TYPE, com.kragnoth.creativeengineering.content.alloyfurnace.AlloyRecipes.all(level));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.COKE_OVEN_BRICKS.get()), CokingCategory.TYPE);
        // Looking up a Blaze Burner's uses shows coking too: it's the oven's heat source.
        registration.addRecipeCatalyst(CokingCategory.createItem("blaze_burner"), CokingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BLAST_FURNACE_BRICKS.get()), BlastSmeltingCategory.TYPE);
        registration.addRecipeCatalyst(CokingCategory.createItem("encased_fan"), BlastSmeltingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ALLOY_FURNACE_BRICKS.get()), AlloyingCategory.TYPE);
        registration.addRecipeCatalyst(CokingCategory.createItem("blaze_burner"), AlloyingCategory.TYPE);
        registration.addRecipeCatalyst(CokingCategory.createItem("mechanical_press"), DiePressingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DIESEL_CRANKCASE.get()), EngineFuelCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.FERMENTING_VAT.get()), FermentingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(net.minecraft.world.item.Items.CAMPFIRE), FermentingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ANDESITE_CAMPFIRE_STAND.get()), FermentingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BRASS_CAMPFIRE_STAND.get()), FermentingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.COPPER_CAMPFIRE_STAND.get()), FermentingCategory.TYPE);
        registration.addRecipeCatalyst(CokingCategory.createItem("blaze_burner"), FermentingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DIESEL_CYLINDER.get()), EngineFuelCategory.TYPE);
    }
}
