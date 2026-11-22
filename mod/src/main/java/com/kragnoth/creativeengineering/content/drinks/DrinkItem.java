package com.kragnoth.creativeengineering.content.drinks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * A bottle of one of the drinks (docs/fermentation-spec.md, docs/distilling-spec.md). It is food that is always
 * drinkable, with the drink animation and sound; its effects and the empty glass bottle it leaves come from its
 * FoodProperties (ModItems.DRINKS). 250 mB a bottle: Create's Spout fills it and the Item Drain empties it.
 * The liquid layer is tinted with the drink's fluid colour (client).
 */
public class DrinkItem extends Item {
    public final String fluid;

    public DrinkItem(String fluid, Properties p) {
        super(p);
        this.fluid = fluid;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 32;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }
}
