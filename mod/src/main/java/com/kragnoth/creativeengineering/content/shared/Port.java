package com.kragnoth.creativeengineering.content.shared;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * What a furnace brick's sides look like. Cycled with Create's wrench. Purely visual: every brick still accepts
 * input and gives output on every face (steelworks-spec section 0), the look just tells the player where the
 * funnels go.
 */
public enum Port implements StringRepresentable {
    BRICKS, INPUT, OUTPUT;

    public static final EnumProperty<Port> PROPERTY = EnumProperty.create("port", Port.class);

    public Port next() {
        return values()[(ordinal() + 1) % values().length];
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
