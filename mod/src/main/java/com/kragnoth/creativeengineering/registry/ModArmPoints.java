package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Create's Mechanical Arm has no generic "any inventory" target: each block it can use is a registered point type.
 * The default ArmInteractionPoint goes through the block's item capability, which is our compact oven view.
 */
public final class ModArmPoints {
    private ModArmPoints() {}

    public static final DeferredRegister<ArmInteractionPointType> TYPES =
            DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, CreativeEngineering.MODID);

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> COKE_OVEN =
            TYPES.register("coke_oven", () -> new ArmInteractionPointType() {
                @Override
                public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
                    return state.is(ModBlocks.COKE_OVEN_BRICKS.get());
                }

                @Override
                public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
                    return new ArmInteractionPoint(this, level, pos, state);
                }
            });

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> BLAST_FURNACE =
            TYPES.register("blast_furnace", () -> new ArmInteractionPointType() {
                @Override
                public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
                    return state.is(ModBlocks.BLAST_FURNACE_BRICKS.get());
                }

                @Override
                public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
                    return new ArmInteractionPoint(this, level, pos, state);
                }
            });

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> ALLOY_FURNACE =
            TYPES.register("alloy_furnace", () -> new ArmInteractionPointType() {
                @Override
                public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
                    return state.is(ModBlocks.ALLOY_FURNACE_BRICKS.get());
                }

                @Override
                public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
                    return new ArmInteractionPoint(this, level, pos, state);
                }
            });
}
