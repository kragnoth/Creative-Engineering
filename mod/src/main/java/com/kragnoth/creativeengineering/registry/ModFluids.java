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

/** Creosote: a thick oil from coking coal. Tagged c:creosote so other mods' creosote and ours are interchangeable. */
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

    /** Called from the mod constructor so the holders above are created before registration fires. */
    public static void init() {}
}
