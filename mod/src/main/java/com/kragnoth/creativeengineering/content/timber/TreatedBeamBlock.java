package com.kragnoth.creativeengineering.content.timber;

import com.simibubi.create.content.decoration.girder.GirderBlock;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Creosote-treated timber beam. Uses Create's girder logic as-is (connects along X / Z, stands as a post, grows
 * top/bottom stubs against blocks above and below, joins Create's metal girders), with our own heavy-timber models.
 * Placement helper as on Create's girders: click a beam with a beam to extend the run (ghost preview shown).
 * The one thing it doesn't do is take a shaft: Create would swap it for its metal encased girder.
 */
public class TreatedBeamBlock extends GirderBlock {
    private static final ResourceLocation SHAFT = ResourceLocation.fromNamespaceAndPath("create", "shaft");
    /** Registered once, like Create's own girder helper; also drives the client-side ghost preview. */
    private static final int PLACEMENT_HELPER = PlacementHelpers.register(new TreatedBeamPlacementHelper());

    public TreatedBeamBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(SHAFT)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        IPlacementHelper helper = PlacementHelpers.get(PLACEMENT_HELPER);
        if (helper.matchesItem(stack) && stack.getItem() instanceof BlockItem item) {
            return helper.getOffset(player, level, state, pos, hit).placeInWorld(level, item, player, hand, hit);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
