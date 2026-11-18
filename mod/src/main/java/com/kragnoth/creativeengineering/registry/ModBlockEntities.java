package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
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

    /** Same compact view on every face and on the side-less query (spec section 0, tarbound-compat T1/T5). */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COKE_OVEN.get(), (be, side) -> be.automationItems());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COKE_OVEN.get(), (be, side) -> be.automationFluids());
    }
}
