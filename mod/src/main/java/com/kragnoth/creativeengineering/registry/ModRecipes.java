package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.blastfurnace.BlastSmeltingRecipe;
import com.kragnoth.creativeengineering.content.cokeoven.CokingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    private ModRecipes() {}

    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, CreativeEngineering.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreativeEngineering.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CokingRecipe>> COKING_TYPE =
            TYPES.register("coking", () -> RecipeType.simple(CreativeEngineering.id("coking")));
    public static final DeferredHolder<RecipeSerializer<?>, CokingRecipe.Serializer> COKING_SERIALIZER =
            SERIALIZERS.register("coking", CokingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BlastSmeltingRecipe>> BLAST_SMELTING_TYPE =
            TYPES.register("blast_smelting", () -> RecipeType.simple(CreativeEngineering.id("blast_smelting")));
    public static final DeferredHolder<RecipeSerializer<?>, BlastSmeltingRecipe.Serializer> BLAST_SMELTING_SERIALIZER =
            SERIALIZERS.register("blast_smelting", BlastSmeltingRecipe.Serializer::new);


    public static final DeferredHolder<RecipeType<?>, RecipeType<com.kragnoth.creativeengineering.content.metalworking.DiePressingRecipe>> DIE_PRESSING_TYPE =
            TYPES.register("die_pressing", () -> RecipeType.simple(CreativeEngineering.id("die_pressing")));
    public static final DeferredHolder<RecipeSerializer<?>, com.kragnoth.creativeengineering.content.metalworking.DiePressingRecipe.Serializer> DIE_PRESSING_SERIALIZER =
            SERIALIZERS.register("die_pressing", com.kragnoth.creativeengineering.content.metalworking.DiePressingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<com.kragnoth.creativeengineering.content.fermenting.FermentingRecipe>> FERMENTING_TYPE =
            TYPES.register("fermenting", () -> RecipeType.simple(CreativeEngineering.id("fermenting")));
    public static final DeferredHolder<RecipeSerializer<?>, com.kragnoth.creativeengineering.content.fermenting.FermentingRecipe.Serializer> FERMENTING_SERIALIZER =
            SERIALIZERS.register("fermenting", com.kragnoth.creativeengineering.content.fermenting.FermentingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<com.kragnoth.creativeengineering.content.distilling.DistillingRecipe>> DISTILLING_TYPE =
            TYPES.register("distilling", () -> RecipeType.simple(CreativeEngineering.id("distilling")));
    public static final DeferredHolder<RecipeSerializer<?>, com.kragnoth.creativeengineering.content.distilling.DistillingRecipe.Serializer> DISTILLING_SERIALIZER =
            SERIALIZERS.register("distilling", com.kragnoth.creativeengineering.content.distilling.DistillingRecipe.Serializer::new);
}
