package com.kragnoth.creativeengineering.content.distilling;

import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * {@code creativeengineering:distilling} (docs/distilling-spec.md): one fluid boiled in the column's bottom layer splits
 * into {@code fractions}, listed bottom (heaviest) to top (lightest); fraction i collects in layer i + 1, so the column
 * must be at least fractions + 1 tall. Optional {@code residue} item with a {@code chance} stays in the boiler.
 * {@code heat}: minimum heat (1 passive, 2 lit Blaze Burner, 3 superheated); {@code time}: ticks per batch at x1.
 * <pre>{"input":{"fluid":"creativeengineering:hooch","amount":250},
 *  "fractions":[{"id":"minecraft:water","amount":150},{"id":"creativeengineering:vodka","amount":100}],
 *  "heat":1, "time":200}</pre>
 */
public record DistillingRecipe(SizedFluidIngredient input, List<FluidStack> fractions, Optional<ItemStack> residue, float chance,
        int heat, int time) implements Recipe<RecipeInput> {

    public static final MapCodec<DistillingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SizedFluidIngredient.FLAT_CODEC.fieldOf("input").forGetter(DistillingRecipe::input),
            FluidStack.CODEC.listOf(1, 7).fieldOf("fractions").forGetter(DistillingRecipe::fractions),
            ItemStack.CODEC.optionalFieldOf("residue").forGetter(DistillingRecipe::residue),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 1F).forGetter(DistillingRecipe::chance),
            Codec.intRange(1, 3).optionalFieldOf("heat", 1).forGetter(DistillingRecipe::heat),
            Codec.INT.optionalFieldOf("time", 200).forGetter(DistillingRecipe::time)
    ).apply(i, DistillingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DistillingRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedFluidIngredient.STREAM_CODEC, DistillingRecipe::input,
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), DistillingRecipe::fractions,
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC), DistillingRecipe::residue,
            ByteBufCodecs.FLOAT, DistillingRecipe::chance,
            ByteBufCodecs.VAR_INT, DistillingRecipe::heat,
            ByteBufCodecs.VAR_INT, DistillingRecipe::time,
            DistillingRecipe::new);

    /** Layers the column needs: the boiler plus one per fraction. */
    public int minHeight() {
        return fractions.size() + 1;
    }

    @Override public boolean matches(RecipeInput input, Level level) { return false; } // matched by the column
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider r) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider r) { return ItemStack.EMPTY; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.DISTILLING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.DISTILLING_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<DistillingRecipe> {
        @Override public MapCodec<DistillingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, DistillingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
