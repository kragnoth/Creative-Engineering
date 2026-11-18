package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Blast furnace in Jade: size, air blast (and what it makes), speed, smelting progress, in/fuel/out, stop reason. */
public enum BlastFurnaceJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = CreativeEngineering.id("blast_furnace");

    @Override
    public void appendServerData(CompoundTag d, BlockAccessor a) {
        if (!(a.getBlockEntity() instanceof BlastFurnaceBlockEntity be) || be.getControllerBE() == null) {
            return;
        }
        BlastFurnaceBlockEntity c = be.getControllerBE();
        d.putInt("w", c.getWidth());
        d.putInt("h", c.getHeight());
        d.putFloat("speed", be.speed());
        d.putInt("fans", be.fans());
        int[] use = be.slotUse();
        d.putInt("busy", use[0]);
        d.putInt("slots", use[1]);
        d.putFloat("progress", be.averageProgress());
        for (int i = 0; i < 4; i++) {
            ItemStack s = be.automationItems().getStackInSlot(i);
            if (!s.isEmpty()) {
                d.putString("s" + i, BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
                d.putInt("n" + i, s.getCount());
            }
        }
        Component b = be.automationBlocker(List.of());
        if (b != null) {
            d.putString("blocker", Component.Serializer.toJson(b, a.getLevel().registryAccess()));
        }
    }

    private static Component stack(CompoundTag d, int i) {
        if (!d.contains("s" + i)) {
            return Component.literal("-").withStyle(ChatFormatting.DARK_GRAY);
        }
        return Component.literal(d.getInt("n" + i) + " ")
                .append(BuiltInRegistries.ITEM.get(ResourceLocation.parse(d.getString("s" + i))).getDescription());
    }

    @Override
    public void appendTooltip(ITooltip t, BlockAccessor a, IPluginConfig config) {
        CompoundTag d = a.getServerData();
        if (!d.contains("w")) {
            return;
        }
        int w = d.getInt("w");
        t.add(Component.translatable("creativeengineering.coke_oven.size", w, w, d.getInt("h")).withStyle(ChatFormatting.GRAY));
        int fans = d.getInt("fans");
        t.add(Component.translatable("creativeengineering.blast_furnace.air." + (fans > 0 ? "on" : "off"), fans)
                .withStyle(fans > 0 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        float speed = d.getFloat("speed");
        t.add(Component.translatable("creativeengineering.blast_furnace.speed", String.format("%.2f", speed))
                .withStyle(speed > 0 ? ChatFormatting.GOLD : ChatFormatting.RED));
        int busy = d.getInt("busy");
        Component s = Component.translatable("creativeengineering.blast_furnace.slots", busy, d.getInt("slots"));
        if (busy > 0) {
            s = s.copy().append(Component.literal("  " + Math.round(d.getFloat("progress") * 100) + "%").withStyle(ChatFormatting.WHITE));
        }
        t.add(s.copy().withStyle(ChatFormatting.GRAY));
        t.add(Component.translatable("creativeengineering.jade.blast_in", stack(d, 0), stack(d, 1)).withStyle(ChatFormatting.GRAY));
        t.add(Component.translatable("creativeengineering.jade.blast_out", stack(d, 2), stack(d, 3)).withStyle(ChatFormatting.GRAY));
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
