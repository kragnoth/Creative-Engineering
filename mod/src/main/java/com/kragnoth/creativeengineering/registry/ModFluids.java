package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Creosote (a thick oil from coking coal, its own textures) and the tinted fluids (plant oil, later the fermented
 * fluids). Each is tagged with the common c: tag, so other mods' versions and ours are interchangeable.
 */
public final class ModFluids {
    private ModFluids() {}

    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreativeEngineering.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, CreativeEngineering.MODID);

    public static final DeferredHolder<FluidType, FluidType> CREOSOTE_TYPE = TYPES.register("creosote", () -> new FluidType(
            FluidType.Properties.create()
                    .descriptionId("fluid_type.creativeengineering.creosote")
                    .density(1100).viscosity(3000).temperature(300)
                    .canSwim(false).canDrown(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, FlowingFluid> CREOSOTE = FLUIDS.register("creosote",
            () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CREOSOTE = FLUIDS.register("flowing_creosote",
            () -> new BaseFlowingFluid.Flowing(properties()));

    public static final DeferredBlock<LiquidBlock> CREOSOTE_BLOCK = ModBlocks.BLOCKS.register("creosote",
            () -> new LiquidBlock(CREOSOTE.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN).replaceable().noCollission().strength(100.0F)
                    .pushReaction(PushReaction.DESTROY).noLootTable().liquid()));

    public static final DeferredItem<BucketItem> CREOSOTE_BUCKET = ModItems.ITEMS.register("creosote_bucket",
            () -> new BucketItem(CREOSOTE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(CREOSOTE_TYPE, CREOSOTE, FLOWING_CREOSOTE)
                .block(CREOSOTE_BLOCK).bucket(CREOSOTE_BUCKET)
                .slopeFindDistance(2).levelDecreasePerBlock(2).tickRate(30);
    }

    // ------------------------------------------------------------------------------------------------ tinted fluids
    /**
     * A plain liquid drawn with vanilla's water textures and a colour tint (as Create does for its own fluids), with a
     * placeable block and a bucket whose model fills from the fluid at runtime. Used for plant oil and the fermented
     * fluids: one line each, no per-fluid textures.
     */
    public static final class Tinted {
        public final String name;
        public final int tint;
        public final DeferredHolder<FluidType, FluidType> type;
        public final DeferredHolder<Fluid, FlowingFluid> source, flowing;
        public final DeferredBlock<LiquidBlock> block;
        public final DeferredItem<BucketItem> bucket;

        Tinted(String name, int tint, int density, int viscosity, MapColor color) {
            this.name = name;
            this.tint = tint;
            this.type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type." + CreativeEngineering.MODID + "." + name)
                    .density(density).viscosity(viscosity).canSwim(true).canDrown(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));
            this.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(props()));
            this.flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(props()));
            this.block = ModBlocks.BLOCKS.register(name, () -> new LiquidBlock(source.get(), BlockBehaviour.Properties.of()
                    .mapColor(color).replaceable().noCollission().strength(100.0F)
                    .pushReaction(PushReaction.DESTROY).noLootTable().liquid()));
            this.bucket = ModItems.ITEMS.register(name + "_bucket",
                    () -> new BucketItem(source.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
            ALL.add(this);
        }

        private BaseFlowingFluid.Properties props() {
            return new BaseFlowingFluid.Properties(type, source, flowing).block(block).bucket(bucket)
                    .slopeFindDistance(3).levelDecreasePerBlock(1).tickRate(10);
        }
    }

    public static final java.util.List<Tinted> ALL = new java.util.ArrayList<>();

    /** Pressed from seeds in a Basin under a Mechanical Press (Create compacting). Tagged c:plantoil. */
    public static final Tinted PLANT_OIL = new Tinted("plant_oil", 0xFFD9B44A, 920, 2500, MapColor.COLOR_YELLOW);

    /** Called from the mod constructor so the holders above are created before registration fires. */
    public static void init() {}
}
