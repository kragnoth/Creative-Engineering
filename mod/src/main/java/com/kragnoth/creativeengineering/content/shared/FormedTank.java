package com.kragnoth.creativeengineering.content.shared;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/** A block of a formed tank-like multiblock (vat, distilling column): its controller, readable on the client. */
public interface FormedTank {
    @Nullable
    BlockPos controllerPos();
}
