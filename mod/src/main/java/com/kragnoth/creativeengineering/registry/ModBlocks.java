package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.cokeoven.CokeOvenBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Blocks. IDs are save-facing: see dev/SAVE_FORMAT.md before renaming anything. */
public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreativeEngineering.MODID);

    /** Tier 1 storage block. Same toughness as a block of iron. */
    public static final DeferredBlock<Block> BLISTER_STEEL_BLOCK = BLOCKS.registerSimpleBlock("blister_steel_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL));

    /** Tier 2: forms a coke oven with its neighbours (1-3 wide, 1-3 tall). Glows while heated. */
    public static final DeferredBlock<CokeOvenBlock> COKE_OVEN_BRICKS = BLOCKS.register("coke_oven_bricks",
            () -> new CokeOvenBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(CokeOvenBlock.LIT) ? 7 : 0)));

    public static final DeferredBlock<Block> COKE_BLOCK = BLOCKS.registerSimpleBlock("coke_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.STONE));
}
