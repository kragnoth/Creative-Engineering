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
        return 160;
    }

    @Override
    public int getHeight() {
        return 72;
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
        var font = Minecraft.getInstance().font;
        arrow.draw(g, 28, 9);
        g.drawString(font, Component.translatable("creativeengineering.jei.coking.time", trim(recipe.time() / 20F)), 4, 28, 0xFF808080, false);
        // Heat row: what can sit under the oven and how fast it runs on each
        g.drawString(font, Component.translatable("creativeengineering.jei.coking.heat"), 4, 40, 0xFF808080, false);
        int x = 4;
        for (int i = 0; i < HEAT_ICONS.length; i++) {
            g.renderItem(HEAT_ICONS[i].get(), x, 51);
            g.drawString(font, HEAT_LABELS[i], x + 17, 56, 0xFF606060, false);
            x += 38;
        }
    }

    @Override
    public void getTooltip(mezz.jei.api.gui.builder.ITooltipBuilder tooltip, CokingRecipe recipe, IRecipeSlotsView slots,
                           double mouseX, double mouseY) {
        if (mouseY >= 50 && mouseY < 68 && mouseX >= 4 && mouseX < 4 + 38 * HEAT_ICONS.length) {
            int i = (int) ((mouseX - 4) / 38);
            tooltip.add(Component.translatable(HEAT_TIPS[i]));
            tooltip.add(Component.translatable("creativeengineering.jei.coking.heat.every_column"));
        }
    }

    private static String trim(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }

    // Create's items looked up by id (Create is required at runtime; no compile-time item references needed).
    private static final java.util.function.Supplier<ItemStack>[] HEAT_ICONS = heatIcons();
    private static final String[] HEAT_LABELS = {"x0.5", "x1", "x2"};
    private static final String[] HEAT_TIPS = {
            "creativeengineering.jei.coking.heat.passive",
            "creativeengineering.jei.coking.heat.burner",
            "creativeengineering.jei.coking.heat.superheated"};

    @SuppressWarnings("unchecked")
    private static java.util.function.Supplier<ItemStack>[] heatIcons() {
        return new java.util.function.Supplier[]{
                () -> new ItemStack(net.minecraft.world.item.Items.CAMPFIRE),
                () -> createItem("blaze_burner"),
                () -> createItem("blaze_cake")};
    }

    static ItemStack createItem(String path) {
        return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create", path)));
    }
}
