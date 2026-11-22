package com.kragnoth.creativeengineering.client.ct;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fermenting Vat connected textures exactly like Create's fluid tank (HorizontalCTBehaviour, RECTANGLE sheets) on
 * steel-recoloured copies of Create's tank textures (RecolorSpriteSource). Joins only within one formed vat.
 */
public class VatCTBehaviour extends HorizontalCTBehaviour {
    public VatCTBehaviour() {
        super(shift("vat_tank"), shift("vat_tank_top"));
    }

    private static CTSpriteShiftEntry shift(String tex) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, CreativeEngineering.id("block/" + tex),
                CreativeEngineering.id("block/" + tex + "_connected"));
    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
            Direction face) {
        return state.getBlock() == other.getBlock()
                && reader.getBlockEntity(pos) instanceof FermentingVatBlockEntity a && a.controllerPos() != null
                && reader.getBlockEntity(otherPos) instanceof FermentingVatBlockEntity b
                && Objects.equals(a.controllerPos(), b.controllerPos());
    }
}
