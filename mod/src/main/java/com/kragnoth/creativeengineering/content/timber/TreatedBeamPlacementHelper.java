package com.kragnoth.creativeengineering.content.timber;

import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.simibubi.create.content.decoration.girder.GirderPlacementHelper;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Create's girder placement (click a beam with a beam to extend the run, with the ghost preview), pointed at the
 * Treated Beam. Create's helper only recognises its own Metal Girder; its offset logic works for any GirderBlock.
 */
public class TreatedBeamPlacementHelper extends GirderPlacementHelper {
    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return stack -> stack.is(ModBlocks.TREATED_BEAM.get().asItem());
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return state -> state.is(ModBlocks.TREATED_BEAM.get());
    }
}
