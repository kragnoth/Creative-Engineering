package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.blastfurnace.BlastSmeltingRecipe;
import com.kragnoth.creativeengineering.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import net.minecraft.world.item.Items;

/** JEI page for the blast furnace: input, cast iron (no air) vs steel (air blast), slag chance, fuel and heat notes. */
public class BlastSmeltingCategory implements IRecipeCategory<BlastSmeltingRecipe> {
    public static final RecipeType<BlastSmeltingRecipe> TYPE =
            RecipeType.create(CreativeEngineering.MODID, "blast_smelting", BlastSmeltingRecipe.class);

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public BlastSmeltingCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.BLAST_FURNACE_BRICKS.get()));
        this.arrow = gui.createAnimatedRecipeArrow(100);
    }

    @Override public RecipeType<BlastSmeltingRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("creativeengineering.jei.blast_smelting"); }
    @Override public int getWidth() { return 170; }
    @Override public int getHeight() { return 82; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlastSmeltingRecipe r, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 14).setStandardSlotBackground().addIngredients(r.ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 64, 4).setOutputSlotBackground().addItemStack(r.resultCast())
                .addRichTooltipCallback((v, t) -> t.add(Component.translatable("creativeengineering.jei.blast.no_air")));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 64, 26).setOutputSlotBackground().addItemStack(r.resultBlast())
                .addRichTooltipCallback((v, t) -> t.add(Component.translatable("creativeengineering.jei.blast.air")));
        if (r.slagChance() > 0) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 14).setStandardSlotBackground()
                    .addItemStack(new ItemStack(ModItems.SLAG.get()))
                    .addRichTooltipCallback((v, t) -> t.add(Component.translatable("creativeengineering.jei.blast.slag",
                            Math.round(r.slagChance() * 100))));
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 62).addItemStack(CokingCategory.createItem("blaze_burner"));
        builder.addSlot(RecipeIngredientRole.CATALYST, 22, 62).addItemStack(CokingCategory.createItem("encased_fan"));
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 62).addItemStack(new ItemStack(ModItems.COAL_COKE.get()))
                .addItemStack(new ItemStack(Items.CHARCOAL)).addItemStack(new ItemStack(Items.COAL));
    }

    @Override
    public void draw(BlastSmeltingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;
        arrow.draw(g, 28, 15);
        g.drawString(font, Component.translatable("creativeengineering.jei.blast.cast_label"), 84, 8, 0xFF606060, false);
        g.drawString(font, Component.translatable("creativeengineering.jei.blast.steel_label"), 84, 30, 0xFF606060, false);
        g.drawString(font, Component.translatable("creativeengineering.jei.blast.needs"), 4, 50, 0xFF808080, false);
        g.drawString(font, Component.translatable("creativeengineering.jei.blast.fuel", fmt(r.ironUnits())), 60, 66, 0xFF606060, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder t, BlastSmeltingRecipe r, IRecipeSlotsView slots, double mx, double my) {
        if (my >= 60 && mx < 58) {
            t.add(Component.translatable("creativeengineering.jei.blast.needs_tip"));
        }
    }

    private static String fmt(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }
}
