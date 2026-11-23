package com.kragnoth.creativeengineering.client.ct;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connected textures for the brick multiblocks, the way Create's fluid tanks and vaults do it: RECTANGLE sheets,
 * and a brick only connects to bricks of the SAME formed furnace (two furnaces side by side stay two framed
 * boxes). The side sheet is picked from the quad's own sprite, so the wrench's bricks / input / output models
 * each get their matching sheet.
 */
public class FurnaceCTBehaviour extends ConnectedTextureBehaviour.Base {
    private final CTSpriteShiftEntry top;
    private final List<CTSpriteShiftEntry> sides;

    public FurnaceCTBehaviour(String name) {
        this.top = shift(name + "_top");
        this.sides = List.of(shift(name), shift(name + "_input"), shift(name + "_output"));
    }

    private static CTSpriteShiftEntry shift(String tex) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, CreativeEngineering.id("block/" + tex),
                CreativeEngineering.id("block/" + tex + "_connected"));
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (direction.getAxis().isVertical()) {
            return top;
        }
        if (sprite != null) {
            for (CTSpriteShiftEntry s : sides) {
                if (s.getOriginal() == sprite) {
                    return s;
                }
            }
        }
        return sides.get(0);
    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
            Direction face) {
        if (state.getBlock() != other.getBlock()) {
            return false;
        }
        return ConnectivityHandler.isConnected(reader, pos, otherPos);
    }
}
