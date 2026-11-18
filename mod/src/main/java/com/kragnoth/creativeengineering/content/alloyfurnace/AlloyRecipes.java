package com.kragnoth.creativeengineering.content.alloyfurnace;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Everything the alloy furnace can run, as plain jobs, built once per recipe reload:
 * <ul>
 *   <li>our {@code alloying} recipes (JSON, any mod's items);</li>
 *   <li>Create's heated / superheated mixing recipes that are item-only, have no chance outputs and make ingots
 *       (brass, and any pack alloy done in a basin);</li>
 *   <li>Ender IO's {@code enderio:alloy_smelting} recipes that make ingots, when Ender IO is installed. That covers
 *       base Ender IO, Ender IO: Evolution and pack-added alloys. Read through the recipe's own codec, so there is
 *       no compile dependency. Energy / 16 = ticks; 10000+ FE needs superheated burners.</li>
 * </ul>
 */
public final class AlloyRecipes {
    /** One runnable recipe. heat: 1 heated, 2 superheated. */
    public record Job(ResourceLocation id, List<SizedIngredient> inputs, ItemStack result, int heat, float fuel, int time,
                      boolean fromCreate) {
        public boolean uses(ItemStack stack) {
            return inputs.stream().anyMatch(s -> s.ingredient().test(stack));
        }
    }

    /** Per recipe manager: the integrated server and its client each keep their own; old ones drop on reload. */
    private static final java.util.Map<RecipeManager, List<Job>> CACHE = new java.util.WeakHashMap<>();

    private AlloyRecipes() {}

    public static synchronized List<Job> all(Level level) {
        RecipeManager rm = level.getRecipeManager();
        return CACHE.computeIfAbsent(rm, m -> Collections.unmodifiableList(build(m, level.registryAccess())));
    }

    private static List<Job> build(RecipeManager rm, RegistryAccess access) {
        List<Job> out = new ArrayList<>();
        for (RecipeHolder<AlloyingRecipe> h : rm.getAllRecipesFor(ModRecipes.ALLOYING_TYPE.get())) {
            AlloyingRecipe r = h.value();
            out.add(new Job(h.id(), r.ingredients(), r.result(), r.heat(), r.fuel(), r.time(), false));
        }
        for (RecipeHolder<? extends Recipe<?>> h : rm.getAllRecipesFor(AllRecipeTypes.MIXING.getType())) {
            if (!(h.value() instanceof BasinRecipe r)) {
                continue;
            }
            HeatCondition heat = r.getRequiredHeat();
            if (heat == HeatCondition.NONE || !r.getFluidIngredients().isEmpty() || !r.getFluidResults().isEmpty()
                    || r.getRollableResults().size() != 1 || r.getIngredients().isEmpty()) {
                continue;
            }
            ProcessingOutput o = r.getRollableResults().get(0);
            if (o.getChance() < 1 || !o.getStack().is(Tags.Items.INGOTS)) {
                continue;
            }
            List<SizedIngredient> in = new ArrayList<>();
            for (Ingredient i : r.getIngredients()) {
                in.add(new SizedIngredient(i, 1));
            }
            out.add(new Job(h.id(), in, o.getStack().copy(), heat == HeatCondition.SUPERHEATED ? 2 : 1, 1F, 200, true));
        }
        enderIo(rm, access, out);
        return out;
    }

    private static final ResourceLocation EIO_ALLOY = ResourceLocation.fromNamespaceAndPath("enderio", "alloy_smelting");

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void enderIo(RecipeManager rm, RegistryAccess access, List<Job> out) {
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(EIO_ALLOY);
        if (type == null) {
            return;
        }
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, access);
        for (Object o : rm.getAllRecipesFor((RecipeType) type)) {
            RecipeHolder<?> h = (RecipeHolder<?>) o;
            try {
                RecipeSerializer ser = h.value().getSerializer();
                JsonElement el = (JsonElement) ((MapCodec) ser.codec()).codec().encodeStart(ops, h.value()).getOrThrow();
                if (!(el instanceof JsonObject json) || !json.has("inputs") || !json.has("output")) {
                    continue; // inherited furnace recipes and other shapes
                }
                List<SizedIngredient> in = SizedIngredient.FLAT_CODEC.listOf().parse(ops, json.get("inputs")).getOrThrow();
                ItemStack result = ItemStack.CODEC.parse(ops, json.get("output")).getOrThrow();
                if (in.isEmpty() || !result.is(Tags.Items.INGOTS)) {
                    continue;
                }
                int energy = json.has("energy") ? json.get("energy").getAsInt() : 3200;
                out.add(new Job(h.id(), in, result, energy >= 10000 ? 2 : 1, 1F, Math.clamp(energy / 16, 100, 1200), true));
            } catch (RuntimeException e) {
                CreativeEngineering.LOGGER.debug("Alloy furnace skipped {}: {}", h.id(), e.getMessage());
            }
        }
    }
}
