package com.kragnoth.creativeengineering.content.blastfurnace;

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
 * {@code creativeengineering:blast_smelting} (docs/steelworks-spec.md 4): one item in; {@code result_cast} without
 * an air blast, {@code result_blast} with one (fans blowing in). {@code iron_units} of fuel are burned per item;
 * {@code slag_chance} gives a bonus slag that never blocks the furnace. {@code time} = ticks at x1.
 */
public record BlastSmeltingRecipe(Ingredient ingredient, ItemStack resultCast, ItemStack resultBlast, float ironUnits,
                                  float slagChance, int time) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<BlastSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(BlastSmeltingRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result_cast").forGetter(BlastSmeltingRecipe::resultCast),
            ItemStack.STRICT_CODEC.fieldOf("result_blast").forGetter(BlastSmeltingRecipe::resultBlast),
            Codec.FLOAT.optionalFieldOf("iron_units", 1F).forGetter(BlastSmeltingRecipe::ironUnits),
            Codec.FLOAT.optionalFieldOf("slag_chance", 0F).forGetter(BlastSmeltingRecipe::slagChance),
            Codec.INT.optionalFieldOf("time", 200).forGetter(BlastSmeltingRecipe::time)
    ).apply(i, BlastSmeltingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlastSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, BlastSmeltingRecipe::ingredient,
            ItemStack.STREAM_CODEC, BlastSmeltingRecipe::resultCast,
            ItemStack.STREAM_CODEC, BlastSmeltingRecipe::resultBlast,
            ByteBufCodecs.FLOAT, BlastSmeltingRecipe::ironUnits,
            ByteBufCodecs.FLOAT, BlastSmeltingRecipe::slagChance,
            ByteBufCodecs.VAR_INT, BlastSmeltingRecipe::time,
            BlastSmeltingRecipe::new);

    public ItemStack result(boolean airBlast) {
        return (airBlast ? resultBlast : resultCast).copy();
    }

    @Override public boolean matches(SingleRecipeInput input, Level level) { return ingredient.test(input.item()); }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider r) { return resultCast.copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r) { return resultBlast; }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, ingredient); }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.BLAST_SMELTING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.BLAST_SMELTING_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<BlastSmeltingRecipe> {
        @Override public MapCodec<BlastSmeltingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, BlastSmeltingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
