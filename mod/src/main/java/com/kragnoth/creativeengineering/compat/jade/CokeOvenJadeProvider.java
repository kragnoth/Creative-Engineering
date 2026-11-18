package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlockEntity;
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

/**
 * Coke oven in Jade: size, heat (and what it means), coking progress, what's in and out, creosote, and why it's
 * stopped if it is. Numbers come from the server (the client doesn't know the other bricks' contents).
 */
public enum CokeOvenJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = CreativeEngineering.id("coke_oven");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof CokeOvenBlockEntity be) || be.getControllerBE() == null) {
            return;
        }
        CokeOvenBlockEntity c = be.getControllerBE();
        data.putInt("w", c.getWidth());
        data.putInt("h", c.getHeight());
        data.putFloat("speed", be.heatSpeed());
        int[] use = be.slotUse();
        data.putInt("busy", use[0]);
        data.putInt("slots", use[1]);
        data.putFloat("progress", be.averageProgress());
        putStack(data, "in", be.automationItems().getStackInSlot(0));
        putStack(data, "out", be.automationItems().getStackInSlot(1));
        data.putInt("creosote", be.creosoteTank().getFluidAmount());
        data.putInt("creosoteCap", be.creosoteTank().getCapacity());
        Component blocker = be.automationBlocker(List.of());
        if (blocker != null) {
            data.putString("blocker", Component.Serializer.toJson(blocker, accessor.getLevel().registryAccess()));
        }
    }

    private static void putStack(CompoundTag data, String key, ItemStack stack) {
        if (!stack.isEmpty()) {
            data.putString(key, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            data.putInt(key + "N", stack.getCount());
        }
    }

    private static Component stackLine(CompoundTag data, String key) {
        if (!data.contains(key)) {
            return Component.literal("-").withStyle(ChatFormatting.DARK_GRAY);
        }
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.getString(key)));
        return Component.literal(data.getInt(key + "N") + " ").append(item.getDescription());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("w")) {
            return;
        }
        int w = data.getInt("w"), h = data.getInt("h");
        tooltip.add(Component.translatable("creativeengineering.coke_oven.size", w, w, h).withStyle(ChatFormatting.GRAY));

        float speed = data.getFloat("speed");
        Component heat = speed <= 0
                ? Component.translatable("creativeengineering.coke_oven.heat.none").withStyle(ChatFormatting.RED)
                : Component.literal("x" + trim(speed)).withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                        .append(Component.translatable(speed >= 2 ? "creativeengineering.jade.heat.superheated"
                                : speed >= 1 ? "creativeengineering.jade.heat.burner" : "creativeengineering.jade.heat.passive")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(")").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("creativeengineering.coke_oven.heat", heat).withStyle(ChatFormatting.GRAY));

        int busy = data.getInt("busy");
        Component coking = Component.translatable("creativeengineering.coke_oven.slots", busy, data.getInt("slots"));
        if (busy > 0) {
            coking = coking.copy().append(Component.literal("  " + Math.round(data.getFloat("progress") * 100) + "%")
                    .withStyle(ChatFormatting.WHITE));
        }
        tooltip.add(coking.copy().withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("creativeengineering.jade.in_out", stackLine(data, "in"), stackLine(data, "out"))
                .withStyle(ChatFormatting.GRAY));
        int cre = data.getInt("creosote"), cap = data.getInt("creosoteCap");
        tooltip.add(Component.translatable("creativeengineering.coke_oven.creosote", cre, cap)
                .withStyle(cre >= cap ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
        if (cre >= cap) {
            tooltip.add(Component.translatable("creativeengineering.jade.venting").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (data.contains("blocker")) {
            Component blocker = Component.Serializer.fromJson(data.getString("blocker"), accessor.getLevel().registryAccess());
            if (blocker != null) {
                tooltip.add(blocker.copy().withStyle(ChatFormatting.RED));
            }
        }
    }

    private static String trim(float f) {
        return f == (int) f ? Integer.toString((int) f) : String.format("%.2f", f).replaceAll("0+$", "");
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
