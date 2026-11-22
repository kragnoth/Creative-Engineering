package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.fermenting.FermentingRecipe;
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
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

/** JEI: fermenting. Counted items + input fluid -> product fluid; heat sources (campfire / Blaze Burner) and time. */
public class FermentingCategory implements IRecipeCategory<FermentingRecipe> {
    public static final RecipeType<FermentingRecipe> TYPE = RecipeType.create(CreativeEngineering.MODID, "fermenting", FermentingRecipe.class);

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public FermentingCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.FERMENTING_VAT.get()));
        this.arrow = gui.createAnimatedRecipeArrow(80);
    }

    @Override public RecipeType<FermentingRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("creativeengineering.jei.fermenting"); }
    @Override public int getWidth() { return 150; }
    @Override public int getHeight() { return 58; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, FermentingRecipe r, IFocusGroup focuses) {
        int i = 0;
        for (SizedIngredient in : r.ingredients()) {
            b.addSlot(RecipeIngredientRole.INPUT, 4 + i * 18, 4).setStandardSlotBackground()
                    .addItemStacks(Arrays.stream(in.ingredient().getItems()).map(s -> s.copyWithCount(in.count())).toList());
            i++;
        }
        b.addSlot(RecipeIngredientRole.INPUT, 4, 24).setStandardSlotBackground()
                .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.stream(r.fluid().getFluids()).toList())
                .setFluidRenderer(1000, false, 16, 16);
        b.addSlot(RecipeIngredientRole.OUTPUT, 110, 14).setOutputSlotBackground()
                .addIngredient(NeoForgeTypes.FLUID_STACK, r.result()).setFluidRenderer(1000, false, 16, 16);
        b.addSlot(RecipeIngredientRole.CATALYST, 40, 40).addItemStack(new ItemStack(Items.CAMPFIRE));
        b.addSlot(RecipeIngredientRole.CATALYST, 58, 40).addItemStack(CokingCategory.createItem("blaze_burner"));
    }

    @Override
    public void draw(FermentingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        arrow.draw(g, 76, 15);
        g.drawString(Minecraft.getInstance().font, Component.translatable("creativeengineering.jei.fermenting.time", r.time() / 20),
                80, 44, 0xFF606060, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder t, FermentingRecipe r, IRecipeSlotsView slots, double mx, double my) {
        if (my >= 38 && mx >= 38 && mx < 78) {
            t.add(Component.translatable("creativeengineering.jei.fermenting.tip"));
        }
    }
}
