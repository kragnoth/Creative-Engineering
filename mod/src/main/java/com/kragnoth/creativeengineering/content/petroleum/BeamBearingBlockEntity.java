package com.kragnoth.creativeengineering.content.petroleum;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Walking Beam Bearing: no logic of its own; it carries the beam's drawing and a goggle summary of the rig. */
public class BeamBearingBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public BeamBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public Direction facing() {
        return getBlockState().getValue(BeamBearingBlock.FACING);
    }

    public PumpjackRig rig() {
        return PumpjackRig.fromBearing(level, worldPosition, facing());
    }

    /** The crank angle to draw (radians): the gearbox's, or a level beam with no gearbox. */
    public float crankAngle(PumpjackRig rig, float pt) {
        if (rig.gearbox() != null && level.getBlockEntity(rig.gearbox()) instanceof PumpjackBlockEntity g) {
            return g.crankAngle(pt);
        }
        return (float) Math.PI / 2;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        return PumpjackBlockEntity.rigTooltip(tooltip, level, rig());
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(7, 1, 7).expandTowards(0, -PumpjackRig.MAX_H - 1, 0);   // beam ends, cranks, rod
    }
}
