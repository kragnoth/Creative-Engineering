package com.kragnoth.creativeengineering.client.engine;

import com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Fallback (no Flywheel instancing): spins only the crankshaft partial, not the whole crankcase. */
public class CrankshaftRenderer extends KineticBlockEntityRenderer<DieselEngineBlockEntity> {
    public CrankshaftRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(DieselEngineBlockEntity be, BlockState state) {
        SuperByteBuffer buf = CachedBuffers.partial(CEPartials.CRANKSHAFT, state);
        if (state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.Z) {
            buf.rotateCentered(AngleHelper.rad(90), Direction.UP);
        }
        return buf;
    }
}
