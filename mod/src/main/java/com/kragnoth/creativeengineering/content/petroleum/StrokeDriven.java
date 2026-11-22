package com.kragnoth.creativeengineering.content.petroleum;

/**
 * A block entity that works when something strokes it up and down: the block a Pumpjack's horsehead hangs over
 * (docs/petroleum-spec.md). The Pumpjack calls {@link #stroke} every tick it turns, with its power: min(1, RPM / 64).
 * Today that is the Wellhead; later stroke-driven machines (stamp mill, bellows) implement the same.
 */
public interface StrokeDriven {
    /** Called server-side once per tick while a turning Pumpjack drives this block. power is 0..1. */
    void stroke(float power);
}
