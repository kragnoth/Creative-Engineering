package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.metalworking.DiePressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
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

/** JEI page for die pressing: ingredient, the die (kept), results with Create-style chances, the press as catalyst. */
public class DiePressingCategory implements IRecipeCategory<DiePressingRecipe> {
    public static final RecipeType<DiePressingRecipe> TYPE =
            RecipeType.create(CreativeEngineering.MODID, "die_pressing", DiePressingRecipe.class);

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public DiePressingCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(CokingCategory.createItem("mechanical_press"));
        this.arrow = gui.createAnimatedRecipeArrow(40);
    }

    @Override public RecipeType<DiePressingRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("creativeengineering.jei.die_pressing"); }
    @Override public int getWidth() { return 150; }
    @Override public int getHeight() { return 48; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DiePressingRecipe r, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 16).setStandardSlotBackground().addIngredients(r.ingredient());
        builder.addSlot(RecipeIngredientRole.CATALYST, 34, 2).setStandardSlotBackground().addIngredients(r.die())
                .addRichTooltipCallback((v, t) -> t.add(Component.translatable("creativeengineering.jei.die.kept")));
        int x = 90;
        for (ProcessingOutput o : r.results()) {
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, 16).setOutputSlotBackground().addItemStack(o.getStack());
            if (o.getChance() < 1) {
                float c = o.getChance();
                slot.addRichTooltipCallback((v, t) -> t.add(Component.translatable("creativeengineering.jei.chance", Math.round(c * 100))));
            }
            x += 20;
        }
    }

    @Override
    public void draw(DiePressingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        arrow.draw(g, 58, 17);
        g.drawString(Minecraft.getInstance().font, Component.translatable("creativeengineering.jei.die.fit"), 4, 38, 0xFF808080, false);
    }
}
