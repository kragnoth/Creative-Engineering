package com.kragnoth.creativeengineering.mixin;

import com.kragnoth.creativeengineering.content.metalworking.PressDies;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Press dies. Stacks with other mods' injects on the same method: Create (and anyone else) picks a recipe first;
 * we only replace it while a die is fitted, otherwise the value passes through untouched.
 */
@Mixin(value = MechanicalPressBlockEntity.class, remap = false)
public abstract class MechanicalPressRecipeMixin {
    @ModifyReturnValue(method = "getRecipe", at = @At("RETURN"), require = 0)
    private Optional<RecipeHolder<PressingRecipe>> creativeengineering$dies(Optional<RecipeHolder<PressingRecipe>> original,
            ItemStack item) {
        return PressDies.override((MechanicalPressBlockEntity) (Object) this, item, original);
    }
}
