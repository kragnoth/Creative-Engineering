package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.distilling.DistillingRecipe;
import com.kragnoth.creativeengineering.registry.ModItems;
import java.util.Arrays;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** JEI: distilling. Boiler fluid in (bottom left) -> one output per level, drawn as a column (lightest on top). */
public class DistillingCategory implements IRecipeCategory<DistillingRecipe> {
    public static final RecipeType<DistillingRecipe> TYPE = RecipeType.create(CreativeEngineering.MODID, "distilling", DistillingRecipe.class);
    private static final int ROW = 19, LEVELS = 7;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public DistillingCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.DISTILLING_TANK.get()));
        this.arrow = gui.createAnimatedRecipeArrow(60);
    }

    @Override public RecipeType<DistillingRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("creativeengineering.jei.distilling"); }
    @Override public int getWidth() { return 160; }
    @Override public int getHeight() { return ROW * (LEVELS - 2) + 22; }
    @Override public IDrawable getIcon() { return icon; }

    private int rowY(DistillingRecipe r, int level) {   // level 0 = boiler, at the bottom
        return getHeight() - 20 - level * ROW;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, DistillingRecipe r, IFocusGroup focuses) {
        b.addSlot(RecipeIngredientRole.INPUT, 4, rowY(r, 0)).setStandardSlotBackground()
                .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.stream(r.input().getFluids()).toList())
                .setFluidRenderer(1000, false, 16, 16);
        for (int i = 0; i < r.fractions().size(); i++) {
            b.addSlot(RecipeIngredientRole.OUTPUT, 96, rowY(r, i + 1)).setOutputSlotBackground()
                    .addIngredient(NeoForgeTypes.FLUID_STACK, r.fractions().get(i)).setFluidRenderer(1000, false, 16, 16);
        }
        r.residue().ifPresent(res -> b.addSlot(RecipeIngredientRole.OUTPUT, 96, rowY(r, 0)).setOutputSlotBackground()
                .addItemStack(res).addRichTooltipCallback((v, t) -> {
                    if (r.chance() < 1) {
                        t.add(Component.literal(Math.round(r.chance() * 100) + "%"));
                    }
                }));
        b.addSlot(RecipeIngredientRole.CATALYST, 4, rowY(r, 0) - 20).addItemStack(new ItemStack(ModItems.DISTILLING_TANK.get()));
    }

    @Override
    public void draw(DistillingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;
        arrow.draw(g, 28, rowY(r, 0));
        for (int i = 1; i <= r.fractions().size(); i++) {
            g.drawString(font, Component.translatable("creativeengineering.jei.distilling.level", i), 116, rowY(r, i) + 4, 0xFF606060, false);
        }
        g.drawString(font, Component.translatable("creativeengineering.jei.distilling.needs", r.minHeight(), r.heat()), 4, 4, 0xFF606060, false);
        g.drawString(font, Component.translatable("creativeengineering.jei.distilling.time", r.time() / 20), 4, 14, 0xFF606060, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder t, DistillingRecipe r, IRecipeSlotsView slots, double mx, double my) {
        if (my < 24) {
            t.add(Component.translatable("creativeengineering.jei.distilling.tip"));
        }
    }
}
