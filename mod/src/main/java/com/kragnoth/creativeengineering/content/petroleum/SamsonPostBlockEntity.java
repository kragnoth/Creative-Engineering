package com.kragnoth.creativeengineering.content.petroleum;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Samson Post (lower half): no logic of its own; it carries the beam's drawing and a goggle summary of the rig. */
public class SamsonPostBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public SamsonPostBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public Direction facing() {
        return getBlockState().getValue(SamsonPostBlock.FACING);
    }

    public PumpjackRig rig() {
        return PumpjackRig.fromPost(level, worldPosition, facing());
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
        return new AABB(worldPosition).inflate(3, 3, 3);   // the beam reaches up to 2 blocks each way and 3 up
    }
}
