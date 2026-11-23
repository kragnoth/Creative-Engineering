package com.kragnoth.creativeengineering.mixin;

import com.kragnoth.creativeengineering.content.petroleum.Lubrication;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A lubricated member weighs a quarter less on its network (content/petroleum/Lubrication). */
@Mixin(value = KineticNetwork.class, remap = false)
public class KineticNetworkLubeMixin {
    @ModifyReturnValue(method = "getActualStressOf", at = @At("RETURN"), require = 0)
    private float creativeengineering$lubricated(float original, KineticBlockEntity be) {
        return Lubrication.isLubricated(be) ? original * Lubrication.STRESS_FACTOR : original;
    }
}
