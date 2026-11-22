package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Fermenting Vat in Jade (any block of it): size, heat, input / product tanks, ingredients, progress, why it's stopped. */
public enum FermentingVatJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = CreativeEngineering.id("fermenting_vat");

    @Override
    public void appendServerData(CompoundTag d, BlockAccessor a) {
        if (!(a.getBlockEntity() instanceof FermentingVatBlockEntity be)) {
            return;
        }
        var reg = a.getLevel().registryAccess();
        d.putIntArray("size", be.size());
        d.putFloat("speed", be.speed());
        for (int t = 0; t < 2; t++) {
            FluidStack f = be.automationFluids().getFluidInTank(t);
            if (!f.isEmpty()) {
                d.put("f" + t, f.save(reg));
            }
            d.putInt("cap" + t, be.automationFluids().getTankCapacity(t));
        }
        d.putInt("items", be.automationItems().getStackInSlot(0).getCount());
        if (!be.automationItems().getStackInSlot(0).isEmpty()) {
            d.putString("itemName", Component.Serializer.toJson(be.automationItems().getStackInSlot(0).getHoverName(), reg));
        }
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
        for (int i = 0; i < 2; i++) {
            FluidStack f = d.contains("f" + i) ? FluidStack.parseOptional(reg, d.getCompound("f" + i)) : FluidStack.EMPTY;
            Component name = f.isEmpty() ? Component.translatable("creativeengineering.vat.empty") : f.getHoverName();
            t.add(Component.translatable("creativeengineering.vat.tank." + i, name, f.getAmount(), d.getInt("cap" + i))
                    .withStyle(i == 1 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        }
        if (d.contains("itemName")) {
            Component n = Component.Serializer.fromJson(d.getString("itemName"), reg);
            t.add(Component.translatable("creativeengineering.jade.vat_items", Component.literal(d.getInt("items") + " ").append(n))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (d.getFloat("progress") >= 0) {
            t.add(Component.translatable("creativeengineering.vat.progress", d.getInt("batches"), Math.round(d.getFloat("progress") * 100))
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
