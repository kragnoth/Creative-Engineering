package com.kragnoth.creativeengineering.content.petroleum;

import net.minecraft.world.item.HoneycombItem;

/**
 * Paraffin wax, pressed from heavy oil (docs/petroleum-spec.md). It waxes copper exactly like honeycomb (by hand or in
 * a Deployer) and makes candles; the oil field's answer to the bee farm.
 */
public class ParaffinWaxItem extends HoneycombItem {
    public ParaffinWaxItem(Properties p) {
        super(p);
    }
}
