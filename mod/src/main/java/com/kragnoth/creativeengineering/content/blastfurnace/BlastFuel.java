package com.kragnoth.creativeengineering.content.blastfurnace;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * Data map {@code creativeengineering:blast_furnace_fuel}: how many iron units one fuel item smelts.
 * Defaults (data/creativeengineering/data_maps/item/blast_furnace_fuel.json): coke 2, charcoal 1, coal 0.5.
 */
public record BlastFuel(float ironUnits) {
    public static final Codec<BlastFuel> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("iron_units").forGetter(BlastFuel::ironUnits)).apply(i, BlastFuel::new));

    public static final DataMapType<Item, BlastFuel> TYPE = DataMapType.builder(
            CreativeEngineering.id("blast_furnace_fuel"), Registries.ITEM, CODEC).synced(CODEC, false).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(TYPE);
    }

    public static float unitsOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        BlastFuel f = stack.getItemHolder().getData(TYPE);
        return f == null ? 0 : f.ironUnits();
    }
}
