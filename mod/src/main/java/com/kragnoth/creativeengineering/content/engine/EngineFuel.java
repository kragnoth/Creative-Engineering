package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * What a fluid is worth in the diesel engine: data map {@code creativeengineering:engine_fuel} on fluids.
 * {@code stress} = stress capacity (SU) each cylinder adds while burning it; {@code burn} = mB per cylinder per second.
 * Defaults are keyed by fluid tags that also list other mods' fluids (Crafts & Additions, Create Diesel Generators,
 * Immersive Engineering / Petroleum) as optional entries.
 */
public record EngineFuel(float stress, float burn) {
    public static final Codec<EngineFuel> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("stress").forGetter(EngineFuel::stress),
            Codec.FLOAT.optionalFieldOf("burn", 2F).forGetter(EngineFuel::burn)
    ).apply(i, EngineFuel::new));

    public static final DataMapType<Fluid, EngineFuel> TYPE = DataMapType.builder(
            CreativeEngineering.id("engine_fuel"), Registries.FLUID, CODEC).synced(CODEC, false).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(TYPE);
    }

    public static EngineFuel of(FluidStack stack) {
        return stack.isEmpty() ? null : stack.getFluidHolder().getData(TYPE);
    }
}
