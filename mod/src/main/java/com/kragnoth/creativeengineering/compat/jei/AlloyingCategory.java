package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.alloyfurnace.AlloyRecipes;
import com.kragnoth.creativeengineering.registry.ModItems;
import java.util.Arrays;
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
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** JEI page for the alloy furnace: up to 6 counted ingredients, result, burner level, fuel, source. */
public class AlloyingCategory implements IRecipeCategory<AlloyRecipes.Job> {
    public static final RecipeType<AlloyRecipes.Job> TYPE =
            RecipeType.create(CreativeEngineering.MODID, "alloying", AlloyRecipes.Job.class);

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public AlloyingCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.ALLOY_FURNACE_BRICKS.get()));
        this.arrow = gui.createAnimatedRecipeArrow(100);
    }

    @Override public RecipeType<AlloyRecipes.Job> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("creativeengineering.jei.alloying"); }
    @Override public int getWidth() { return 170; }
    @Override public int getHeight() { return 74; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloyRecipes.Job r, IFocusGroup focuses) {
        int i = 0;
        for (SizedIngredient in : r.inputs()) {
            ItemStack[] stacks = Arrays.stream(in.ingredient().getItems()).map(s -> s.copyWithCount(in.count())).toArray(ItemStack[]::new);
            builder.addSlot(RecipeIngredientRole.INPUT, 4 + (i % 3) * 18, 4 + (i / 3) * 18).setStandardSlotBackground()
                    .addItemStacks(Arrays.asList(stacks));
            i++;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 13).setOutputSlotBackground().addItemStack(r.result());
        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 54).addItemStack(CokingCategory.createItem("blaze_burner"));
        builder.addSlot(RecipeIngredientRole.CATALYST, 22, 54).addItemStack(CokingCategory.createItem("encased_fan"));
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 54).addItemStack(new ItemStack(ModItems.COAL_COKE.get()));
    }

    @Override
    public void draw(AlloyRecipes.Job r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;
        arrow.draw(g, 66, 14);
        g.drawString(font, Component.translatable("creativeengineering.jei.alloy.heat." + r.heat()), 60, 50, 0xFF606060, false);
        g.drawString(font, Component.translatable("creativeengineering.jei.alloy.time", r.time() / 20F), 60, 61, 0xFF808080, false);
        if (r.fromCreate()) {
            g.drawString(font, Component.translatable("creativeengineering.jei.alloy.inherited", r.id().getNamespace()), 124, 18, 0xFF808080, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder t, AlloyRecipes.Job r, IRecipeSlotsView slots, double mx, double my) {
        if (my >= 52 && mx < 58) {
            t.add(Component.translatable("creativeengineering.jei.alloy.needs_tip"));
        }
    }
}
