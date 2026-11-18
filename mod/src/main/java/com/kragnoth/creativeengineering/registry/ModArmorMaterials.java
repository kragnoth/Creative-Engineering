package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Armour materials (docs/steelworks-spec.md 6a). Layer textures: textures/models/armor/<name>_layer_1/2.png */
public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, CreativeEngineering.MODID);

    /** Blister steel: 17 total defence (iron 15, diamond 20), toughness 1. Durability factor 22 (iron 15, diamond 33). */
    public static final int BLISTER_STEEL_DURABILITY = 22;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BLISTER_STEEL = ARMOR_MATERIALS.register("blister_steel", () -> {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 2);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 7);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 6);
        return new ArmorMaterial(defense, 10, SoundEvents.ARMOR_EQUIP_IRON,
                () -> Ingredient.of(ModItems.BLISTER_STEEL_INGOT.get()),
                List.of(new ArmorMaterial.Layer(CreativeEngineering.id("blister_steel"))), 1.0F, 0.0F);
    });
}
