package com.kragnoth.creativeengineering.content.engine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Drive Belt: click a flywheel, then a Drive Pulley 2..8 blocks straight above or below it on the same axis (either
 * order). Fitting it costs the item; breaking either wheel drops it again. Sneak-use on air clears a started belt.
 */
public class DriveBeltItem extends Item {
    public DriveBeltItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (!(level.getBlockEntity(pos) instanceof BeltWheelBlockEntity wheel)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        BlockPos first = data.contains("BeltStart") ? NbtUtils.readBlockPos(data.copyTag(), "BeltStart").orElse(null) : null;
        if (first == null || first.equals(pos)) {
            if (wheel.hasBelt()) {
                tell(player, "taken");
                return InteractionResult.FAIL;
            }
            CompoundTag t = new CompoundTag();
            t.put("BeltStart", NbtUtils.writeBlockPos(pos));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
            tell(player, "started");
            return InteractionResult.SUCCESS;
        }
        stack.remove(DataComponents.CUSTOM_DATA);
        String problem = BeltWheelBlockEntity.linkProblem(level, first, pos);
        if (problem != null) {
            tell(player, problem);
            return InteractionResult.FAIL;
        }
        BeltWheelBlockEntity.link(level, first, pos);
        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1F, 0.8F);
        if (player == null || !player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).contains("BeltStart");
    }

    private static void tell(Player p, String key) {
        if (p != null) {
            p.displayClientMessage(Component.translatable("creativeengineering.drive_belt." + key)
                    .withStyle(key.equals("started") ? ChatFormatting.GOLD : ChatFormatting.RED), true);
        }
    }
}
