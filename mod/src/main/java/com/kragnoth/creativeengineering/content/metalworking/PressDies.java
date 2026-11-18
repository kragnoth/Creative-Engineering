package com.kragnoth.creativeengineering.content.metalworking;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.registry.ModRecipes;
import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Press dies (docs/steelworks-spec.md / gap module 4). Right-click a Mechanical Press with a die to fit it (swaps any
 * fitted die back to you); sneak-right-click with an empty hand to take it out. Deployers can do both. While a die is
 * fitted the press only runs die_pressing recipes for that die; without one it is a normal Create press.
 */
@EventBusSubscriber(modid = CreativeEngineering.MODID)
public final class PressDies {
    public static final TagKey<Item> DIES = TagKey.create(Registries.ITEM, CreativeEngineering.id("dies"));

    private static final Map<DiePressingRecipe, RecipeHolder<PressingRecipe>> AS_PRESSING = new WeakHashMap<>();

    /** Create's press block entity type, looked up by id (its Registrate holder isn't on our compile classpath). */
    @SuppressWarnings("unchecked")
    private static final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<MechanicalPressBlockEntity>> PRESS_TYPE =
            com.google.common.base.Suppliers.memoize(() -> (net.minecraft.world.level.block.entity.BlockEntityType<MechanicalPressBlockEntity>)
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("create", "mechanical_press")));

    private PressDies() {}

    @SubscribeEvent
    static void attach(BlockEntityBehaviourEvent event) {
        event.forType(PRESS_TYPE.get(), be -> event.attach(new DieBehaviour(be)));
    }

    @SubscribeEvent
    static void use(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!(level.getBlockEntity(event.getPos()) instanceof MechanicalPressBlockEntity press)) {
            return;
        }
        DieBehaviour dies = DieBehaviour.get(press, DieBehaviour.TYPE);
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        boolean fit = held.is(DIES);
        boolean remove = held.isEmpty() && player.isShiftKeyDown() && dies != null && !dies.getDie().isEmpty();
        if (dies == null || (!fit && !remove)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }
        ItemStack old = dies.swap(fit ? held : ItemStack.EMPTY);
        if (fit && !player.isCreative()) {
            held.shrink(1);
        }
        if (!old.isEmpty()) {
            player.getInventory().placeItemBackInInventory(old);
        }
        level.playSound(null, event.getPos(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.25F, 1.6F);
        player.displayClientMessage(fit
                ? Component.translatable("creativeengineering.die.fitted", held.isEmpty() ? dies.getDie().getHoverName() : held.getHoverName())
                : Component.translatable("creativeengineering.die.removed"), true);
    }

    /** Called from the press mixin. A fitted die decides the recipe; no die = Create's own result. */
    public static Optional<RecipeHolder<PressingRecipe>> override(MechanicalPressBlockEntity press, ItemStack item,
            Optional<RecipeHolder<PressingRecipe>> original) {
        DieBehaviour dies = DieBehaviour.get(press, DieBehaviour.TYPE);
        if (dies == null || dies.getDie().isEmpty() || press.getLevel() == null) {
            return original;
        }
        ItemStack die = dies.getDie();
        for (RecipeHolder<DiePressingRecipe> h : press.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.DIE_PRESSING_TYPE.get())) {
            DiePressingRecipe r = h.value();
            if (r.die().test(die) && r.ingredient().test(item)) {
                return Optional.of(asPressing(h.id(), r));
            }
        }
        return Optional.empty(); // a die is fitted: no plain pressing (a steel ingot must not become a sheet)
    }

    private static synchronized RecipeHolder<PressingRecipe> asPressing(ResourceLocation id, DiePressingRecipe r) {
        return AS_PRESSING.computeIfAbsent(r, k -> {
            NonNullList<ProcessingOutput> outs = NonNullList.create();
            outs.addAll(k.results());
            PressingRecipe p = new StandardProcessingRecipe.Builder<>(PressingRecipe::new, id)
                    .withItemIngredients(k.ingredient()).withItemOutputs(outs).build();
            return new RecipeHolder<>(id, p);
        });
    }
}
