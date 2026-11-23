package com.kragnoth.creativeengineering.content.petroleum;

import com.kragnoth.creativeengineering.registry.ModFluids;
import com.kragnoth.creativeengineering.registry.ModItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.joml.Vector3f;

/**
 * Oil Can (docs/petroleum-spec.md): holds 1 B of lubricant. Use it on any Create kinetic block (by hand or in a
 * Deployer) to lubricate it (Lubrication: a dose of 50 mB, a day of -25% stress). Refill it with a Spout, or by using it
 * on any tank holding lubricant.
 */
public class OilCanItem extends Item {
    public static final int CAPACITY = 1000;

    public OilCanItem(Properties p) {
        super(p);
    }

    public static FluidHandlerItemStack handler(ItemStack stack) {
        return new FluidHandlerItemStack(ModItems.CAN_FLUID, stack, CAPACITY) {
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return fluid.is(ModFluids.LUBRICANT.source.get());
            }
        };
    }

    public static int amount(ItemStack stack) {
        return handler(stack).getFluidInTank(0).getAmount();
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();
        IFluidHandler can = handler(stack);
        if (level.getBlockEntity(pos) instanceof KineticBlockEntity kbe) {
            if (can.getFluidInTank(0).getAmount() < Lubrication.DOSE) {
                message(ctx, "creativeengineering.oil_can.empty");
                return InteractionResult.FAIL;
            }
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (!Lubrication.apply(kbe)) {
                message(ctx, "creativeengineering.oil_can.full");
                return InteractionResult.FAIL;
            }
            if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) {
                can.drain(Lubrication.DOSE, IFluidHandler.FluidAction.EXECUTE);
            }
            level.playSound(null, pos, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 0.8F, 1.3F);
            ((ServerLevel) level).sendParticles(new DustParticleOptions(new Vector3f(0.72F, 0.55F, 0.2F), 1F),
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0);
            return InteractionResult.CONSUME;
        }
        // refill from any tank holding lubricant
        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, ctx.getClickedFace());
        if (tank != null) {
            int space = CAPACITY - can.getFluidInTank(0).getAmount();
            FluidStack drawn = tank.drain(new FluidStack(ModFluids.LUBRICANT.source.get(), space), IFluidHandler.FluidAction.SIMULATE);
            if (space > 0 && !drawn.isEmpty()) {
                if (!level.isClientSide) {
                    int filled = can.fill(drawn, IFluidHandler.FluidAction.EXECUTE);
                    tank.drain(new FluidStack(ModFluids.LUBRICANT.source.get(), filled), IFluidHandler.FluidAction.EXECUTE);
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.6F, 1.4F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    private static void message(UseOnContext ctx, String key) {
        if (ctx.getPlayer() != null && !ctx.getLevel().isClientSide) {
            ctx.getPlayer().displayClientMessage(Component.translatable(key), true);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13F * amount(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xC99A3C;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("creativeengineering.oil_can.amount", amount(stack), CAPACITY).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("creativeengineering.oil_can.doses", amount(stack) / Lubrication.DOSE).withStyle(ChatFormatting.GRAY));
    }

    /** A full can, for the creative tab and tests. */
    public static ItemStack full() {
        ItemStack s = new ItemStack(ModItems.OIL_CAN.get());
        handler(s).fill(new FluidStack(ModFluids.LUBRICANT.source.get(), CAPACITY), IFluidHandler.FluidAction.EXECUTE);
        return s;
    }

    static int clamp(int v) {
        return Mth.clamp(v, 0, CAPACITY);
    }
}
