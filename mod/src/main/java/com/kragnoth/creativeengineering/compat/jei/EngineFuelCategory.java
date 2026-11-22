package com.kragnoth.creativeengineering.compat.jei;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.engine.EngineFuel;
import com.kragnoth.creativeengineering.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** JEI: every fluid the diesel engine burns, with its stress per cylinder and burn rate (from the engine_fuel map). */
public class EngineFuelCategory implements IRecipeCategory<EngineFuelCategory.Entry> {
    public record Entry(Fluid fluid, EngineFuel fuel) {}

    public static final RecipeType<Entry> TYPE = RecipeType.create(CreativeEngineering.MODID, "engine_fuel", Entry.class);
    private final IDrawable icon;

    public EngineFuelCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModItems.DIESEL_CYLINDER.get()));
    }

    /** Source fluids that carry an engine_fuel value, best first. */
    public static List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (Map.Entry<ResourceKey<Fluid>, EngineFuel> e : BuiltInRegistries.FLUID.getDataMap(EngineFuel.TYPE).entrySet()) {
            Fluid f = BuiltInRegistries.FLUID.get(e.getKey());
            if (f.isSource(f.defaultFluidState())) {
                out.add(new Entry(f, e.getValue()));
            }
        }
        out.sort(Comparator.comparingDouble((Entry x) -> -x.fuel().stress()));
        return out;
    }

    @Override public RecipeType<Entry> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("creativeengineering.jei.engine_fuel"); }
    @Override public int getWidth() { return 150; }
    @Override public int getHeight() { return 40; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry e, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 12).setStandardSlotBackground()
                .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(e.fluid(), 1000));
    }

    @Override
    public void draw(Entry e, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, Component.translatable("creativeengineering.jei.engine_fuel.stress", (int) e.fuel().stress()), 28, 6, 0xFF404040, false);
        g.drawString(font, Component.translatable("creativeengineering.jei.engine_fuel.burn", e.fuel().burn()), 28, 17, 0xFF606060, false);
        g.drawString(font, Component.translatable("creativeengineering.jei.engine_fuel.rpm"), 28, 28, 0xFF808080, false);
    }
}
