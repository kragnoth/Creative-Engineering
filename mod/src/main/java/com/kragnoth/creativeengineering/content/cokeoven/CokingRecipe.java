package com.kragnoth.creativeengineering.content.cokeoven;

import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * {@code creativeengineering:coking}: one item in, one item out, plus creosote (mB) as a bonus that is vented when
 * the oven's tank is full (docs/steelworks-spec.md 3). {@code time} is ticks per coking slot at x1 heat.
 */
public record CokingRecipe(Ingredient ingredient, ItemStack result, int creosote, int time) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<CokingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CokingRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CokingRecipe::result),
            Codec.INT.optionalFieldOf("creosote", 0).forGetter(CokingRecipe::creosote),
            Codec.INT.optionalFieldOf("time", 400).forGetter(CokingRecipe::time)
    ).apply(i, CokingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CokingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CokingRecipe::ingredient,
            ItemStack.STREAM_CODEC, CokingRecipe::result,
            ByteBufCodecs.VAR_INT, CokingRecipe::creosote,
            ByteBufCodecs.VAR_INT, CokingRecipe::time,
            CokingRecipe::new);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public boolean isSpecial() {
        return true; // not shown in the vanilla recipe book
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.COKING_TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<CokingRecipe> {
        @Override
        public MapCodec<CokingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CokingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
