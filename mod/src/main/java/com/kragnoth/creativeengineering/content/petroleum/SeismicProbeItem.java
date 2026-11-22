package com.kragnoth.creativeengineering.content.petroleum;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Seismic Probe: thump the ground to sound out the oil. Says if this chunk has a vein (and its rate); otherwise points
 * to the nearest vein within SEARCH chunks, roughly ("~40 blocks north-east"). One durability per use.
 */
public class SeismicProbeItem extends Item {
    public static final int SEARCH = 8;

    public SeismicProbeItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();
        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }
        level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.35F, 0.55F);
        ((ServerLevel) level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(pos)),
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 16, 0.4, 0.05, 0.4, 0.1);
        player.displayClientMessage(report(level, pos), true);
        ctx.getItemInHand().hurtAndBreak(1, player, ctx.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResult.CONSUME;
    }

    public static Component report(Level level, BlockPos pos) {
        ChunkPos here = new ChunkPos(pos);
        int rate = OilVeins.rate(level, here);
        if (rate > 0) {
            return Component.translatable("creativeengineering.probe.here", rate).withStyle(ChatFormatting.GOLD);
        }
        ChunkPos v = OilVeins.nearest(level, here, SEARCH);
        if (v == null) {
            return Component.translatable("creativeengineering.probe.none", SEARCH * 16).withStyle(ChatFormatting.GRAY);
        }
        int dx = v.getMiddleBlockX() - pos.getX(), dz = v.getMiddleBlockZ() - pos.getZ();
        int dist = (int) Math.round(Math.sqrt(dx * dx + dz * dz) / 8) * 8;   // rough, to 8 blocks
        return Component.translatable("creativeengineering.probe.nearest", Math.max(8, dist),
                Component.translatable("creativeengineering.probe.dir." + compass(dx, dz))).withStyle(ChatFormatting.YELLOW);
    }

    private static String compass(int dx, int dz) {
        double a = Math.toDegrees(Math.atan2(dx, -dz));   // 0 = north, clockwise
        String[] n = {"north", "north_east", "east", "south_east", "south", "south_west", "west", "north_west"};
        return n[(int) Math.round(((a % 360) + 360) % 360 / 45) % 8];
    }
}
