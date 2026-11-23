package com.kragnoth.creativeengineering.compat.jade;

import com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlock;
import com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlockEntity;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlock;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Jade (optional): what our machines are doing at a glance. Jade finds this by its annotation; never loaded without Jade. */
@WailaPlugin
public class CEJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CokeOvenJadeProvider.INSTANCE, CokeOvenBlockEntity.class);
        registration.registerBlockDataProvider(BlastFurnaceJadeProvider.INSTANCE, BlastFurnaceBlockEntity.class);
        registration.registerBlockDataProvider(DistillingTankJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.distilling.DistillingTankBlockEntity.class);
        registration.registerBlockDataProvider(FermentingVatJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlockEntity.class);
        registration.registerBlockDataProvider(DieselEngineJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.engine.DieselEngineBlockEntity.class);
        registration.registerBlockDataProvider(PressDieJadeProvider.INSTANCE, com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CokeOvenJadeProvider.INSTANCE, CokeOvenBlock.class);
        registration.registerBlockComponent(BlastFurnaceJadeProvider.INSTANCE, BlastFurnaceBlock.class);
        registration.registerBlockComponent(DistillingTankJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.distilling.DistillingTankBlock.class);
        registration.registerBlockComponent(FermentingVatJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlock.class);
        registration.registerBlockComponent(DieselEngineJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.engine.DieselCrankcaseBlock.class);
        registration.registerBlockComponent(PressDieJadeProvider.INSTANCE, com.simibubi.create.content.kinetics.press.MechanicalPressBlock.class);
    }
}
