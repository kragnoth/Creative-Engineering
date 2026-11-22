package com.kragnoth.creativeengineering.mixin;

import com.kragnoth.creativeengineering.content.heat.CampfireStandBlock;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Campfire Stands as fan catalysts (like Create Connected's): air blows through them (create:fan_transparent) and a
 * lit stand with a soul campfire makes the current haunt. Tags can't see the stand's fire / lit state, so
 * this decides for stands only and leaves every other block to Create. Optional (require = 0).
 */
@Mixin(value = AllFanProcessingTypes.HauntingType.class, remap = false)
public class FanCatalystHauntingMixin {
    @ModifyReturnValue(method = "isValidAt", at = @At("RETURN"), require = 0)
    private boolean creativeengineering$stand(boolean original, Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (s.getBlock() instanceof CampfireStandBlock) {
            return s.getValue(CampfireStandBlock.LIT) && s.getValue(CampfireStandBlock.FIRE) == CampfireStandBlock.Fire.SOUL;
        }
        return original;
    }
}
