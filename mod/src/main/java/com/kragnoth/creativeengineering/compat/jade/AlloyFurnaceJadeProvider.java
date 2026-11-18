package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Alloy furnace in Jade: size (or "loose"), burner level, fans, speed, batches + progress, items held, stop reason. */
public enum AlloyFurnaceJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = CreativeEngineering.id("alloy_furnace");

    @Override
    public void appendServerData(CompoundTag d, BlockAccessor a) {
        if (!(a.getBlockEntity() instanceof AlloyFurnaceBlockEntity be)) {
            return;
        }
        d.putBoolean("formed", be.isFormed());
        if (be.isFormed()) {
            d.putIntArray("size", be.size());
            d.putInt("heat", be.heatMin());
            d.putInt("fans", be.fans());
            d.putFloat("speed", be.speed());
            int[] use = be.batchUse();
            d.putInt("busy", use[0]);
            d.putInt("cap", use[1]);
            d.putFloat("progress", be.averageProgress());
            d.putInt("filters", be.filterCount());
            int in = 0, out = 0;
            var view = be.automationItems();
            for (int i = 0; i < view.getSlots(); i++) {
                ItemStack s = view.getStackInSlot(i);
                if (i < AlloyFurnaceBlockEntity.INPUTS) in += s.getCount();
                else if (i >= AlloyFurnaceBlockEntity.OUT_START) out += s.getCount();
            }
            d.putInt("in", in);
            d.putInt("out", out);
        }
        Component b = be.automationBlocker(List.of());
        if (b != null) {
            d.putString("blocker", Component.Serializer.toJson(b, a.getLevel().registryAccess()));
        }
    }

    @Override
    public void appendTooltip(ITooltip t, BlockAccessor a, IPluginConfig config) {
        CompoundTag d = a.getServerData();
        if (d.getBoolean("formed")) {
            int[] s = d.getIntArray("size");
            t.add(Component.translatable("creativeengineering.coke_oven.size", s[0], s[2], s[1]).withStyle(ChatFormatting.GRAY));
            t.add(Component.translatable("creativeengineering.alloy_furnace.heat." + d.getInt("heat")).withStyle(ChatFormatting.GOLD));
            t.add(Component.translatable("creativeengineering.alloy_furnace.fans", d.getInt("fans")).withStyle(ChatFormatting.AQUA));
            t.add(Component.translatable("creativeengineering.blast_furnace.speed", String.format("%.2f", d.getFloat("speed"))).withStyle(ChatFormatting.GRAY));
            Component batches = Component.translatable("creativeengineering.alloy_furnace.batches", d.getInt("busy"), d.getInt("cap"));
            if (d.getInt("busy") > 0) {
                batches = batches.copy().append("  " + Math.round(d.getFloat("progress") * 100) + "%");
            }
            t.add(batches.copy().withStyle(ChatFormatting.GRAY));
            if (d.getInt("filters") > 0) {
                t.add(Component.translatable("creativeengineering.alloy_furnace.filtered", d.getInt("filters")).withStyle(ChatFormatting.GOLD));
            }
            t.add(Component.translatable("creativeengineering.jade.alloy_items", d.getInt("in"), d.getInt("out")).withStyle(ChatFormatting.GRAY));
        }
        if (d.contains("blocker")) {
            Component b = Component.Serializer.fromJson(d.getString("blocker"), a.getLevel().registryAccess());
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
