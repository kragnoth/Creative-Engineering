package com.kragnoth.creativeengineering.mixin;

import com.kragnoth.creativeengineering.content.petroleum.Lubrication;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lubrication state on every Create kinetic block entity (content/petroleum/Lubrication): the game time it lasts until,
 * saved and sent to clients ("CELubedUntil"), expired once a second on the server, shown on the goggles.
 */
@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticBlockEntityLubeMixin implements Lubrication.Lubricated {
    @Unique
    private long creativeengineering$lubedUntil;

    @Override
    public long creativeengineering$lubedUntil() {
        return creativeengineering$lubedUntil;
    }

    @Override
    public void creativeengineering$setLubedUntil(long gameTime) {
        creativeengineering$lubedUntil = gameTime;
    }

    @Inject(method = "write", at = @At("TAIL"), require = 0)
    private void creativeengineering$write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (creativeengineering$lubedUntil > 0) {
            tag.putLong("CELubedUntil", creativeengineering$lubedUntil);
        }
    }

    @Inject(method = "read", at = @At("TAIL"), require = 0)
    private void creativeengineering$read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        creativeengineering$lubedUntil = tag.getLong("CELubedUntil");
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void creativeengineering$tick(CallbackInfo ci) {
        KineticBlockEntity self = (KineticBlockEntity) (Object) this;
        if (creativeengineering$lubedUntil > 0 && self.getLevel() != null && !self.getLevel().isClientSide && self.getLevel().getGameTime() % 20 == 0) {
            Lubrication.expire(self);
        }
    }

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true, require = 0)
    private void creativeengineering$goggles(List<Component> tooltip, boolean sneaking, CallbackInfoReturnable<Boolean> cir) {
        KineticBlockEntity self = (KineticBlockEntity) (Object) this;
        long left = Lubrication.remaining(self);
        if (left > 0) {
            tooltip.add(Component.literal("    ").append(Component.translatable("creativeengineering.lubricated", left / 1200 + 1))
                    .withStyle(ChatFormatting.GOLD));
            cir.setReturnValue(true);
        }
    }
}
