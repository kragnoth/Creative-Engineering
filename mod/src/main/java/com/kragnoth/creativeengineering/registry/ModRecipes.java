package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
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
}
