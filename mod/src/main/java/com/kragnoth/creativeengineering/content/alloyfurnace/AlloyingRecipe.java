package com.kragnoth.creativeengineering.content.alloyfurnace;

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

/**
 * {@code creativeengineering:alloying} (docs/steelworks-spec.md 4b): up to 6 counted ingredients in the same flat form as
 * Ender IO ({@code {"tag": "c:ingots/iron", "count": 1}}), one result. {@code heat} 1 = heated, 2 = superheated burners;
 * {@code time} = ticks per batch at x1. No fuel: the burners are the cost (an old {@code fuel} field is ignored).
 * Create's own heated mixing recipes that make ingots are also run (AlloyRecipes), so packs get those for free.
 */
public record AlloyingRecipe(List<SizedIngredient> ingredients, ItemStack result, int heat, int time)
        implements Recipe<RecipeInput> {

    public static final MapCodec<AlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SizedIngredient.FLAT_CODEC.listOf(1, 6).fieldOf("ingredients").forGetter(AlloyingRecipe::ingredients),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AlloyingRecipe::result),
            Codec.intRange(1, 2).optionalFieldOf("heat", 1).forGetter(AlloyingRecipe::heat),
            Codec.INT.optionalFieldOf("time", 200).forGetter(AlloyingRecipe::time)
    ).apply(i, AlloyingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AlloyingRecipe::ingredients,
            ItemStack.STREAM_CODEC, AlloyingRecipe::result,
            ByteBufCodecs.VAR_INT, AlloyingRecipe::heat,
            ByteBufCodecs.VAR_INT, AlloyingRecipe::time,
            AlloyingRecipe::new);

    @Override public boolean matches(RecipeInput input, Level level) { return false; } // matched by the furnace itself
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider r) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r) { return result; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.ALLOYING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.ALLOYING_TYPE.get(); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> out = NonNullList.create();
        ingredients.forEach(s -> out.add(s.ingredient()));
        return out;
    }

    public static final class Serializer implements RecipeSerializer<AlloyingRecipe> {
        @Override public MapCodec<AlloyingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
