package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import com.kragnoth.creativeengineering.content.alloyfurnace.AlloyFurnaceBlock;
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
    /** Tier 4: any solid box of these, 2..4 per side, is one bulk alloy furnace. */
    public static final DeferredBlock<AlloyFurnaceBlock> ALLOY_FURNACE_BRICKS = BLOCKS.register("alloy_furnace_bricks",
            () -> new AlloyFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN)
                    .requiresCorrectToolForDrops().strength(3.5F, 8.0F).sound(SoundType.NETHER_BRICKS)));
    // Creosote-treated timber (coke-oven creosote, applied with a Spout or a bucket)
    public static final DeferredBlock<Block> TREATED_PLANKS = BLOCKS.registerSimpleBlock("treated_planks",
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(2.5F, 4.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> TREATED_STAIRS = BLOCKS.register("treated_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(TREATED_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(TREATED_PLANKS.get())));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> TREATED_SLAB = BLOCKS.register("treated_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(TREATED_PLANKS.get())));
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> TREATED_LOG = BLOCKS.register("treated_log",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(2.5F, 4.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> TREATED_WOOD = BLOCKS.register("treated_wood",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(TREATED_LOG.get())));
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.timber.TreatedBeamBlock> TREATED_BEAM = BLOCKS.register("treated_beam",
            () -> new com.kragnoth.creativeengineering.content.timber.TreatedBeamBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BROWN).strength(2.5F, 5.0F).sound(SoundType.WOOD).noOcclusion()));

    // Module 1, diesel engine (docs/engines-spec.md)
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.engine.DieselCrankcaseBlock> DIESEL_CRANKCASE = BLOCKS.register("diesel_crankcase",
            () -> new com.kragnoth.creativeengineering.content.engine.DieselCrankcaseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(4.0F, 6.0F).sound(SoundType.NETHERITE_BLOCK).noOcclusion()));
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock> DIESEL_CYLINDER = BLOCKS.register("diesel_cylinder",
            () -> new com.kragnoth.creativeengineering.content.engine.DieselCylinderBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(4.0F, 6.0F).sound(SoundType.NETHERITE_BLOCK).noOcclusion()));
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.engine.DieselFlywheelBlock> DIESEL_FLYWHEEL = BLOCKS.register("diesel_flywheel",
            () -> new com.kragnoth.creativeengineering.content.engine.DieselFlywheelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(4.0F, 6.0F).sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

    /** Drive Pulley: small end of a Drive Belt from the flywheel (4x speed). */
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.engine.DrivePulleyBlock> DRIVE_PULLEY = BLOCKS.register("drive_pulley",
            () -> new com.kragnoth.creativeengineering.content.engine.DrivePulleyBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion()));

    /** Campfire Stands: Create scaffolding frames holding a campfire (passive heat on a full block). */
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.heat.CampfireStandBlock> ANDESITE_CAMPFIRE_STAND = stand("andesite", MapColor.STONE);
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.heat.CampfireStandBlock> BRASS_CAMPFIRE_STAND = stand("brass", MapColor.TERRACOTTA_YELLOW);
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.heat.CampfireStandBlock> COPPER_CAMPFIRE_STAND = stand("copper", MapColor.COLOR_ORANGE);

    private static DeferredBlock<com.kragnoth.creativeengineering.content.heat.CampfireStandBlock> stand(String metal, MapColor color) {
        return BLOCKS.register(metal + "_campfire_stand", () -> new com.kragnoth.creativeengineering.content.heat.CampfireStandBlock(
                BlockBehaviour.Properties.of().mapColor(color).strength(2F, 6F).sound(SoundType.SCAFFOLDING).noOcclusion()
                        .lightLevel(com.kragnoth.creativeengineering.content.heat.CampfireStandBlock::light)));
    }

    /** Chemistry: Distilling Tank (docs/distilling-spec.md). */
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.distilling.DistillingTankBlock> DISTILLING_TANK = BLOCKS.register("distilling_tank",
            () -> new com.kragnoth.creativeengineering.content.distilling.DistillingTankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(2.5F, 6.0F).sound(SoundType.COPPER).noOcclusion()));

    /** Steel Pipe (Create's pipe, steel, no window) and the Distilling Tank's Tap. */
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.storage.SteelPipeBlock> STEEL_PIPE = BLOCKS.register("steel_pipe",
            () -> new com.kragnoth.creativeengineering.content.storage.SteelPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).forceSolidOn().strength(1.5F).sound(SoundType.COPPER).noOcclusion()));
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.distilling.TapBlock> TAP = BLOCKS.register("tap",
            () -> new com.kragnoth.creativeengineering.content.distilling.TapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD).strength(1.5F).sound(SoundType.COPPER).noOcclusion()));

    /** Steel Storage Tank: Create's fluid tank in the Distilling Tank's steel (content/storage). */
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.storage.SteelTankBlock> STEEL_TANK = BLOCKS.register("steel_tank",
            () -> new com.kragnoth.creativeengineering.content.storage.SteelTankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.5F, 6.0F).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
                    .isRedstoneConductor((s, l, p) -> true)));

    /** Chemistry: Fermenting Vat (docs/fermentation-spec.md). */
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlock> FERMENTING_VAT = BLOCKS.register("fermenting_vat",
            () -> new com.kragnoth.creativeengineering.content.fermenting.FermentingVatBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(2.5F, 6.0F).sound(SoundType.NETHERITE_BLOCK).noOcclusion()));

    /** Petroleum (docs/petroleum-spec.md): the Pumpjack's Crank Gearbox, Walking Beam Bearing (on a built column) and the Wellhead: separate blocks. */
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.petroleum.PumpjackBlock> PUMPJACK = BLOCKS.register("pumpjack",
            () -> new com.kragnoth.creativeengineering.content.petroleum.PumpjackBlock(pumpjackProps()));
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.petroleum.BeamBearingBlock> BEAM_BEARING = BLOCKS.register("walking_beam_bearing",
            () -> new com.kragnoth.creativeengineering.content.petroleum.BeamBearingBlock(pumpjackProps()));
    public static final DeferredBlock<com.kragnoth.creativeengineering.content.petroleum.WellheadBlock> WELLHEAD = BLOCKS.register("wellhead",
            () -> new com.kragnoth.creativeengineering.content.petroleum.WellheadBlock(pumpjackProps()));

    private static BlockBehaviour.Properties pumpjackProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5F, 6.0F)
                .sound(SoundType.NETHERITE_BLOCK).noOcclusion();
    }

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
