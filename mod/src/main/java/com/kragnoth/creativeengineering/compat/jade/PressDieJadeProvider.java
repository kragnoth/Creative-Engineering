package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.metalworking.DieBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Shows the die fitted in a Create Mechanical Press. */
public enum PressDieJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = CreativeEngineering.id("press_die");

    @Override
    public void appendServerData(CompoundTag d, BlockAccessor a) {
        DieBehaviour b = DieBehaviour.get(a.getBlockEntity(), DieBehaviour.TYPE);
        if (b != null && !b.getDie().isEmpty()) {
            d.putString("die", BuiltInRegistries.ITEM.getKey(b.getDie().getItem()).toString());
        }
    }

    @Override
    public void appendTooltip(ITooltip t, BlockAccessor a, IPluginConfig config) {
        if (a.getServerData().contains("die")) {
            t.add(Component.translatable("creativeengineering.jade.die",
                    BuiltInRegistries.ITEM.get(ResourceLocation.parse(a.getServerData().getString("die"))).getDescription())
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
