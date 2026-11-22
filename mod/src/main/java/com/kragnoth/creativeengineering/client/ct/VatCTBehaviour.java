package com.kragnoth.creativeengineering.client.ct;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.shared.FormedTank;
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
 * Tank-style connected textures exactly like Create's fluid tank (HorizontalCTBehaviour, RECTANGLE sheets) on
 * recoloured copies of Create's tank textures (RecolorSpriteSource): steel for the Fermenting Vat (vat_tank*), copper
 * for the Distilling Tank too (both vat_tank*). Joins only within one formed multiblock (same controller).
 */
public class VatCTBehaviour extends HorizontalCTBehaviour {
    public VatCTBehaviour() {
        this("vat_tank");
    }

    public VatCTBehaviour(String prefix) {
        super(shift(prefix), shift(prefix + "_top"));
    }

    public static CTSpriteShiftEntry shift(String tex) {
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
                && reader.getBlockEntity(pos) instanceof FormedTank a && a.controllerPos() != null
                && reader.getBlockEntity(otherPos) instanceof FormedTank b
                && Objects.equals(a.controllerPos(), b.controllerPos());
    }
}
