package com.kragnoth.creativeengineering.content.metalworking;

import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * {@code creativeengineering:die_pressing}: what a Mechanical Press makes from {@code ingredient} while a die matching
 * {@code die} is fitted. {@code results} use Create's output format (id, count, optional chance), so this reads like a
 * Create pressing recipe with one extra field.
 */
public record DiePressingRecipe(Ingredient die, Ingredient ingredient, List<ProcessingOutput> results)
        implements Recipe<SingleRecipeInput> {

    public static final MapCodec<DiePressingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("die").forGetter(DiePressingRecipe::die),
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DiePressingRecipe::ingredient),
            ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(DiePressingRecipe::results)
    ).apply(i, DiePressingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiePressingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, DiePressingRecipe::die,
            Ingredient.CONTENTS_STREAM_CODEC, DiePressingRecipe::ingredient,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), DiePressingRecipe::results,
            DiePressingRecipe::new);

    @Override public boolean matches(SingleRecipeInput input, Level level) { return ingredient.test(input.item()); }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider r) { return getResultItem(r).copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r) { return results.get(0).getStack(); }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, ingredient); }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.DIE_PRESSING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.DIE_PRESSING_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<DiePressingRecipe> {
        @Override public MapCodec<DiePressingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, DiePressingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
