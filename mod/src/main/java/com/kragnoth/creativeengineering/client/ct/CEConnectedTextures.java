package com.kragnoth.creativeengineering.client.ct;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.registry.ModBlocks;
import com.simibubi.create.foundation.block.connected.CTModel;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Wraps every blockstate model of the furnace bricks in Create's CTModel (Registrate does this for Create itself). */
@EventBusSubscriber(modid = CreativeEngineering.MODID, value = Dist.CLIENT)
public final class CEConnectedTextures {
    private CEConnectedTextures() {}

    @SubscribeEvent
    static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        wrap(event.getModels(), ModBlocks.COKE_OVEN_BRICKS.get(), new FurnaceCTBehaviour("coke_oven_bricks"));
        wrap(event.getModels(), ModBlocks.BLAST_FURNACE_BRICKS.get(), new FurnaceCTBehaviour("blast_furnace_bricks"));
        wrap(event.getModels(), ModBlocks.FERMENTING_VAT.get(), new VatCTBehaviour());
        wrap(event.getModels(), ModBlocks.DISTILLING_TANK.get(), new VatCTBehaviour("vat_tank"));
        // steel pipe: arms / rims / drains drawn like Create's pipe, in steel
        for (BlockState state : ModBlocks.STEEL_PIPE.get().getStateDefinition().getPossibleStates()) {
            ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = event.getModels().get(loc);
            if (model != null && !(model instanceof com.kragnoth.creativeengineering.client.storage.SteelPipeModel)) {
                event.getModels().put(loc, new com.kragnoth.creativeengineering.client.storage.SteelPipeModel(model));
            }
        }
        // steel storage tank: Create's own tank connection rules on the steel sheets
        wrap(event.getModels(), ModBlocks.STEEL_TANK.get(), new com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour(
                VatCTBehaviour.shift("vat_tank"), VatCTBehaviour.shift("vat_tank_top"), VatCTBehaviour.shift("vat_tank_inner")));
    }

    private static void wrap(Map<ModelResourceLocation, BakedModel> models, Block block,
            com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour behaviour) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = models.get(loc);
            if (model != null && !(model instanceof CTModel)) {
                models.put(loc, new CTModel(model, behaviour));
            }
        }
    }
}
