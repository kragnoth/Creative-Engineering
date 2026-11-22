package com.kragnoth.creativeengineering.content.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block item for tank-like multiblocks (Fermenting Vat, Distilling Tank) that places a whole layer at once, like Create's
 * fluid tank (Kragnoth, 2026-11-22: the vat "doesn't auto stack 2x2 and 3x3"). Placing one on the top or bottom face of
 * a formed one wider than 1 fills that whole layer of the footprint, if every spot is free and the player holds
 * enough (creative: always). Sneak to place just one.
 */
public class LayerPlacingBlockItem extends BlockItem {
    public LayerPlacingBlockItem(Block block, Properties p) {
        super(block, p);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult result = super.place(ctx);
        if (result.consumesAction() && !ctx.getLevel().isClientSide) {
            fillLayer(ctx);
        }
        return result;
    }

    private void fillLayer(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        Direction face = ctx.getClickedFace();
        if (player == null || player.isShiftKeyDown() || !face.getAxis().isVertical()) {
            return;
        }
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos on = pos.relative(face.getOpposite());
        if (!(level.getBlockEntity(on) instanceof FormedTank tank) || tank.controllerPos() == null) {
            return;
        }
        int[] size = tank.size();
        if (size[0] == 1 && size[2] == 1) {
            return;
        }
        BlockPos c = tank.controllerPos();
        BlockPos start = new BlockPos(c.getX(), face == Direction.DOWN ? c.getY() - 1 : c.getY() + size[1], c.getZ());
        if (start.getY() != pos.getY()) {
            return;
        }
        int needed = 0;
        for (int x = 0; x < size[0]; x++) {
            for (int z = 0; z < size[2]; z++) {
                BlockState s = level.getBlockState(start.offset(x, 0, z));
                if (s.is(getBlock())) {
                    continue;
                }
                if (!s.canBeReplaced()) {
                    return;
                }
                needed++;
            }
        }
        ItemStack stack = ctx.getItemInHand();
        if (!player.isCreative() && stack.getCount() < needed) {
            return;
        }
        for (int x = 0; x < size[0]; x++) {
            for (int z = 0; z < size[2]; z++) {
                BlockPos p = start.offset(x, 0, z);
                if (!level.getBlockState(p).is(getBlock())) {
                    super.place(BlockPlaceContext.at(ctx, p, face));
                }
            }
        }
    }
}
