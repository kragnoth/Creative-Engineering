package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Distilling Tank in Jade: size, heat, every level top to bottom (the one looked at highlighted), progress, why stopped. */
public enum DistillingTankJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = CreativeEngineering.id("distilling_tank");

    @Override
    public void appendServerData(CompoundTag d, BlockAccessor a) {
        if (!(a.getBlockEntity() instanceof DistillingTankBlockEntity be)) {
            return;
        }
        var reg = a.getLevel().registryAccess();
        int[] s = be.size();
        d.putIntArray("size", s);
        d.putFloat("speed", be.speed());
        d.putInt("me", be.layer());
        ListTag levels = new ListTag();
        for (int y = 0; y < s[1]; y++) {
            CompoundTag l = new CompoundTag();
            FluidStack f = be.layerFluid(y);
            if (!f.isEmpty()) {
                l.put("f", f.save(reg));
            }
            l.putInt("cap", be.layerCapacity(y));
            levels.add(l);
        }
        d.put("levels", levels);
        d.putFloat("progress", be.progressFraction());
        d.putInt("batches", be.batchCount());
        Component b = be.blocker();
        if (b != null) {
            d.putString("blocker", Component.Serializer.toJson(b, reg));
        }
    }

    @Override
    public void appendTooltip(ITooltip t, BlockAccessor a, IPluginConfig config) {
        CompoundTag d = a.getServerData();
        if (!d.contains("size")) {
            return;
        }
        var reg = a.getLevel().registryAccess();
        int[] s = d.getIntArray("size");
        t.add(Component.translatable("creativeengineering.coke_oven.size", s[0], s[2], s[1]).withStyle(ChatFormatting.GRAY));
        float speed = d.getFloat("speed");
        t.add(Component.translatable("creativeengineering.vat.heat", String.format("%.2f", speed))
                .withStyle(speed > 0 ? ChatFormatting.GOLD : ChatFormatting.RED));
        ListTag levels = d.getList("levels", 10);
        int me = d.getInt("me");
        for (int y = levels.size() - 1; y >= 0; y--) {
            CompoundTag l = levels.getCompound(y);
            FluidStack f = l.contains("f") ? FluidStack.parseOptional(reg, l.getCompound("f")) : FluidStack.EMPTY;
            Component name = f.isEmpty() ? Component.translatable("creativeengineering.vat.empty") : f.getHoverName();
            t.add(Component.translatable(y == 0 ? "creativeengineering.still.boiler" : "creativeengineering.still.layer", y, name,
                    f.getAmount(), l.getInt("cap")).withStyle(y == me ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        }
        if (d.getFloat("progress") >= 0) {
            t.add(Component.translatable("creativeengineering.still.progress", d.getInt("batches"), Math.round(d.getFloat("progress") * 100))
                    .withStyle(ChatFormatting.GREEN));
        }
        if (d.contains("blocker")) {
            Component b = Component.Serializer.fromJson(d.getString("blocker"), reg);
            if (b != null) {
                t.add(b.copy().withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
