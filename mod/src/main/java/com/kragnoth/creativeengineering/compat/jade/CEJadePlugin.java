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
        registration.registerBlockDataProvider(AlloyFurnaceJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CokeOvenJadeProvider.INSTANCE, CokeOvenBlock.class);
        registration.registerBlockComponent(BlastFurnaceJadeProvider.INSTANCE, BlastFurnaceBlock.class);
        registration.registerBlockComponent(AlloyFurnaceJadeProvider.INSTANCE, com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlock.class);
    }
}
