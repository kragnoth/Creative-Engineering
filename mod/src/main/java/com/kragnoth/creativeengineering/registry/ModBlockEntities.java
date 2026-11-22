package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlockEntity;
import com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlockEntity;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreativeEngineering.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CokeOvenBlockEntity>> COKE_OVEN =
            BLOCK_ENTITIES.register("coke_oven", () -> BlockEntityType.Builder
                    .of((pos, state) -> new CokeOvenBlockEntity(ModBlockEntities.COKE_OVEN.get(), pos, state),
                            ModBlocks.COKE_OVEN_BRICKS.get())
                    .build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE =
            BLOCK_ENTITIES.register("blast_furnace", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BlastFurnaceBlockEntity(ModBlockEntities.BLAST_FURNACE.get(), pos, state),
                            ModBlocks.BLAST_FURNACE_BRICKS.get())
                    .build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloyFurnaceBlockEntity>> ALLOY_FURNACE =
            BLOCK_ENTITIES.register("alloy_furnace", () -> BlockEntityType.Builder
                    .of((pos, state) -> new AlloyFurnaceBlockEntity(ModBlockEntities.ALLOY_FURNACE.get(), pos, state),
                            ModBlocks.ALLOY_FURNACE_BRICKS.get())
                    .build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity>> DIESEL_ENGINE =
            BLOCK_ENTITIES.register("diesel_engine", () -> BlockEntityType.Builder
                    .of((pos, state) -> new com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity(ModBlockEntities.DIESEL_ENGINE.get(), pos, state),
                            ModBlocks.DIESEL_CRANKCASE.get())
                    .build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.kragnoth.creativeengineering.content.storage.SteelTankBlockEntity>> STEEL_TANK =
            BLOCK_ENTITIES.register("steel_tank", () -> BlockEntityType.Builder
                    .of((pos, state) -> new com.kragnoth.creativeengineering.content.storage.SteelTankBlockEntity(ModBlockEntities.STEEL_TANK.get(), pos, state),
                            ModBlocks.STEEL_TANK.get())
                    .build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity>> DISTILLING_TANK =
            BLOCK_ENTITIES.register("distilling_tank", () -> BlockEntityType.Builder
                    .of((pos, state) -> new com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity(ModBlockEntities.DISTILLING_TANK.get(), pos, state),
                            ModBlocks.DISTILLING_TANK.get())
                    .build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity>> FERMENTING_VAT =
            BLOCK_ENTITIES.register("fermenting_vat", () -> BlockEntityType.Builder
                    .of((pos, state) -> new com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity(ModBlockEntities.FERMENTING_VAT.get(), pos, state),
                            ModBlocks.FERMENTING_VAT.get())
                    .build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity>> DIESEL_CYLINDER =
            BLOCK_ENTITIES.register("diesel_cylinder", () -> BlockEntityType.Builder
                    .of((pos, state) -> new com.kragnoth.creativeengineering.content.engine.DieselCylinderBlockEntity(ModBlockEntities.DIESEL_CYLINDER.get(), pos, state),
                            ModBlocks.DIESEL_CYLINDER.get())
                    .build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.kragnoth.creativeengineering.content.engine.FlywheelBlockEntity>> DIESEL_FLYWHEEL =
            BLOCK_ENTITIES.register("diesel_flywheel", () -> BlockEntityType.Builder
                    .of((pos, state) -> new com.kragnoth.creativeengineering.content.engine.FlywheelBlockEntity(ModBlockEntities.DIESEL_FLYWHEEL.get(), pos, state),
                            ModBlocks.DIESEL_FLYWHEEL.get())
                    .build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.kragnoth.creativeengineering.content.engine.DrivePulleyBlockEntity>> DRIVE_PULLEY =
            BLOCK_ENTITIES.register("drive_pulley", () -> BlockEntityType.Builder
                    .of((pos, state) -> new com.kragnoth.creativeengineering.content.engine.DrivePulleyBlockEntity(ModBlockEntities.DRIVE_PULLEY.get(), pos, state),
                            ModBlocks.DRIVE_PULLEY.get())
                    .build(null));

    /** Same compact view on every face and on the side-less query (spec section 0, tarbound-compat T1/T5). */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COKE_OVEN.get(), (be, side) -> be.automationItems());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COKE_OVEN.get(), (be, side) -> be.automationFluids());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BLAST_FURNACE.get(), (be, side) -> be.automationItems());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ALLOY_FURNACE.get(), (be, side) -> be.automationItems());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FERMENTING_VAT.get(), (be, side) -> be.automationItems());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DIESEL_ENGINE.get(), (be, side) -> be.sideFluids(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FERMENTING_VAT.get(), (be, side) -> be.automationFluids());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DISTILLING_TANK.get(), (be, side) -> be.automationFluids());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, STEEL_TANK.get(), (be, side) -> be.capability());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DISTILLING_TANK.get(), (be, side) -> be.automationItems());
    }
}
