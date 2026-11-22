package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Diesel engine in Jade, from any crankcase of the line: status, layout, fuel, stress capacity. */
public enum DieselEngineJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = CreativeEngineering.id("diesel_engine");

    @Override
    public void appendServerData(CompoundTag d, BlockAccessor a) {
        if (!(a.getBlockEntity() instanceof DieselEngineBlockEntity be) || be.controller() == null) {
            return;
        }
        DieselEngineBlockEntity c = be.controller();
        d.putString("status", c.statusKey());
        d.putInt("length", c.lineLength());
        d.putInt("cyl", c.cylinderCount());
        if (c.fuelFluid() != null) {
            d.putString("fuel", BuiltInRegistries.FLUID.getKey(c.fuelFluid()).toString());
        }
        d.putFloat("su", c.totalCapacity());
    }

    @Override
    public void appendTooltip(ITooltip t, BlockAccessor a, IPluginConfig config) {
        CompoundTag d = a.getServerData();
        if (!d.contains("status")) {
            return;
        }
        boolean running = "running".equals(d.getString("status"));
        t.add(Component.translatable("creativeengineering.engine.state." + d.getString("status"))
                .withStyle(running ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        t.add(Component.translatable("creativeengineering.engine.layout", d.getInt("length"), d.getInt("cyl")).withStyle(ChatFormatting.GRAY));
        Component fuel = d.contains("fuel")
                ? new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(d.getString("fuel"))), 1).getHoverName()
                : Component.translatable("creativeengineering.engine.no_fuel");
        t.add(Component.translatable("creativeengineering.engine.fuel", fuel).withStyle(ChatFormatting.GRAY));
        if (running) {
            t.add(Component.translatable("creativeengineering.jade.engine_su", String.format("%,d", (int) d.getFloat("su")))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
