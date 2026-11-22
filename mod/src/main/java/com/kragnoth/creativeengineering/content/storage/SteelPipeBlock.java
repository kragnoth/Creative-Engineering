package com.kragnoth.creativeengineering.content.storage;

import com.kragnoth.creativeengineering.registry.ModBlockEntities;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Steel Pipe (Kragnoth, 2026-11-22): Create's fluid pipe in the distillery's steel. It connects and carries flow like
 * Create's (pumps, valves, taps), but has no window form (the wrench doesn't make it glass) and isn't encased.
 */
public class SteelPipeBlock extends FluidPipeBlock {
    public SteelPipeBlock(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;   // no glass form
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;   // no encasing
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return ModBlockEntities.STEEL_PIPE.get();
    }
}
