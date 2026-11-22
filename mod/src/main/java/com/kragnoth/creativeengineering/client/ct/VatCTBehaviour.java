package com.kragnoth.creativeengineering.client.ct;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fermenting Vat connected textures (RECTANGLE, like Create's tanks): walls and lid join across every block of the
 * SAME formed vat, so the copper strap / steel rim run only round its outside. Only quads using the vat's own wall
 * or lid sprite are shifted; the valve and feet keep their Create textures.
 */
public class VatCTBehaviour extends ConnectedTextureBehaviour.Base {
    private final CTSpriteShiftEntry wall = shift("fermenting_vat"), lid = shift("fermenting_vat_lid");

    private static CTSpriteShiftEntry shift(String tex) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, CreativeEngineering.id("block/" + tex),
                CreativeEngineering.id("block/" + tex + "_connected"));
    }

    @Override
    @Nullable
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        CTSpriteShiftEntry want = direction == Direction.UP ? lid : direction == Direction.DOWN ? null : wall;
        if (want == null || (sprite != null && want.getOriginal() != sprite)) {
            return null;
        }
        return want;
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
