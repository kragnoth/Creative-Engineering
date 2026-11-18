package com.kragnoth.creativeengineering.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/** Tool tiers (docs/steelworks-spec.md 6a). */
public final class ModTiers {
    private ModTiers() {}

    /**
     * Blister steel: between iron and diamond, shear-forged edge tools. Mines at iron level.
     * Iron is 250 uses / 6.0 speed / +2.0 damage / 14 enchantability; diamond 1561 / 8.0 / +3.0 / 10.
     */
    public static final Tier BLISTER_STEEL = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL,
            500, 6.5F, 2.5F, 12, () -> Ingredient.of(ModItems.BLISTER_STEEL_INGOT.get()));
}
