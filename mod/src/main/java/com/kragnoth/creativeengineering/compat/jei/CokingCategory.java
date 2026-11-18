package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.cokeoven.CokingRecipe;
import com.kragnoth.creativeengineering.registry.ModFluids;
import com.kragnoth.creativeengineering.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI page for the Coke Oven. Lists the oven bricks as the catalyst, so Tarbound (and anything else that reads JEI)
 * can write contracts for it. Creosote is shown as a bonus output with its amount.
 */
public class CokingCategory implements IRecipeCategory<CokingRecipe> {
    public static final RecipeType<CokingRecipe> TYPE = RecipeType.create(CreativeEngineering.MODID, "coking", CokingRecipe.class);

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public CokingCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.COKE_OVEN_BRICKS.get()));
        this.arrow = gui.createAnimatedRecipeArrow(200);
    }

    @Override
    public RecipeType<CokingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("creativeengineering.jei.coking");
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 44;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CokingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 8).setStandardSlotBackground().addIngredients(recipe.ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 62, 8).setOutputSlotBackground().addItemStack(recipe.result());
        if (recipe.creosote() > 0) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 8).setStandardSlotBackground()
                    .addFluidStack(ModFluids.CREOSOTE.get(), recipe.creosote())
                    .addRichTooltipCallback((view, tooltip) ->
                            tooltip.add(Component.translatable("creativeengineering.jei.coking.creosote_bonus")));
        }
    }

    @Override
    public void draw(CokingRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        arrow.draw(g, 28, 9);
        String time = (recipe.time() / 20F) + "s @ x1";
        g.drawString(Minecraft.getInstance().font, time, 4, 32, 0xFF808080, false);
    }
}
