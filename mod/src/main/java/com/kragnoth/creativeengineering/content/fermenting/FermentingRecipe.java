package com.kragnoth.creativeengineering.content.fermenting;

import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * {@code creativeengineering:fermenting} (docs/fermentation-spec.md): counted item ingredients (flat form, like
 * alloying) + a fluid (e.g. 250 mB of water) -> a fluid, over {@code time} ticks at passive heat (x1).
 * {@code {"ingredients":[{"tag":"c:crops/potato","count":4}], "fluid":{"fluid":"minecraft:water","amount":250},
 * "result":{"id":"creativeengineering:hooch","amount":250}, "time":400}}
 */
public record FermentingRecipe(List<SizedIngredient> ingredients, SizedFluidIngredient fluid, FluidStack result, int time)
        implements Recipe<RecipeInput> {

    public static final MapCodec<FermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SizedIngredient.FLAT_CODEC.listOf(1, 4).fieldOf("ingredients").forGetter(FermentingRecipe::ingredients),
            SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid").forGetter(FermentingRecipe::fluid),
            FluidStack.CODEC.fieldOf("result").forGetter(FermentingRecipe::result),
            Codec.INT.optionalFieldOf("time", 400).forGetter(FermentingRecipe::time)
    ).apply(i, FermentingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), FermentingRecipe::ingredients,
            SizedFluidIngredient.STREAM_CODEC, FermentingRecipe::fluid,
            FluidStack.STREAM_CODEC, FermentingRecipe::result,
            ByteBufCodecs.VAR_INT, FermentingRecipe::time,
            FermentingRecipe::new);

    public boolean usesItem(ItemStack s) {
        return ingredients.stream().anyMatch(i -> i.ingredient().test(s));
    }

    @Override public boolean matches(RecipeInput input, Level level) { return false; } // matched by the vat
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider r) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r) { return ItemStack.EMPTY; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.FERMENTING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.FERMENTING_TYPE.get(); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> out = NonNullList.create();
        ingredients.forEach(s -> out.add(s.ingredient()));
        return out;
    }

    public static final class Serializer implements RecipeSerializer<FermentingRecipe> {
        @Override public MapCodec<FermentingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
