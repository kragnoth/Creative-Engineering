package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.blastfurnace.BlastFurnaceBlock;
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

    /** Tier 2: forms a coke oven with its neighbours (1-3 wide, 1-3 tall). Smokes while working, embers while heated. */
    public static final DeferredBlock<CokeOvenBlock> COKE_OVEN_BRICKS = BLOCKS.register("coke_oven_bricks",
            () -> new CokeOvenBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)));

    /** Tier 3: stacks into a blast furnace (1-3 wide, up to 6 tall). Cast iron without air, steel with fans blowing in. */
    public static final DeferredBlock<BlastFurnaceBlock> BLAST_FURNACE_BRICKS = BLOCKS.register("blast_furnace_bricks",
            () -> new BlastFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE_BRICKS)));
    public static final DeferredBlock<Block> CAST_IRON_BLOCK = BLOCKS.registerSimpleBlock("cast_iron_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STEEL_BLOCK = BLOCKS.registerSimpleBlock("steel_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(6.0F, 7.0F).sound(SoundType.METAL));

    public static final DeferredBlock<Block> COKE_BLOCK = BLOCKS.registerSimpleBlock("coke_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.STONE));
}
