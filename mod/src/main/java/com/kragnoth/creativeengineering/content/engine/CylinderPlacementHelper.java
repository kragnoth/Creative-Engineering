package com.kragnoth.creativeengineering.content.engine;

import com.kragnoth.creativeengineering.registry.ModBlocks;
import java.util.function.Predicate;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Create placement helper: holding a Diesel Cylinder and looking at a crankcase shows a ghost cylinder on the bank
 * nearest where you're looking (diagonally above, leaning out); right-click places it there, already facing the right
 * way. No support block needed. Falls back to the other bank if that one is taken.
 */
public class CylinderPlacementHelper implements IPlacementHelper {
    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return stack -> stack.is(ModBlocks.DIESEL_CYLINDER.get().asItem());
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return state -> state.is(ModBlocks.DIESEL_CRANKCASE.get());
    }

    @Override
    public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {
        Direction out = state.getValue(DieselCrankcaseBlock.HORIZONTAL_FACING);
        Direction a = out.getClockWise(), b = out.getCounterClockWise();
        Vec3 rel = ray.getLocation().subtract(Vec3.atCenterOf(pos));
        double toward = rel.x * a.getStepX() + rel.z * a.getStepZ();
        for (Direction side : toward >= 0 ? new Direction[]{a, b} : new Direction[]{b, a}) {
            BlockPos target = pos.above().relative(side);
            if (level.getBlockState(target).canBeReplaced()) {
                return PlacementOffset.success(target, s -> s.setValue(DieselCylinderBlock.FACING, side));
            }
        }
        return PlacementOffset.fail();
    }
}
