package com.kragnoth.creativeengineering.registry;

import com.kragnoth.creativeengineering.CreativeEngineering;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Items. IDs are save-facing: see dev/SAVE_FORMAT.md before renaming anything. */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreativeEngineering.MODID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, CreativeEngineering.MODID);
    /** The fluid an Oil Can holds. */
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<net.neoforged.neoforge.fluids.SimpleFluidContent>> CAN_FLUID =
            COMPONENTS.registerComponentType("can_fluid", b -> b.persistent(net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC)
                    .networkSynchronized(net.neoforged.neoforge.fluids.SimpleFluidContent.STREAM_CODEC));
    /** Petroleum byproducts (docs/petroleum-spec.md): paraffin wax (waxes copper like honeycomb, makes candles) and the Oil Can. */
    public static final DeferredItem<com.kragnoth.creativeengineering.content.petroleum.ParaffinWaxItem> PARAFFIN_WAX = ITEMS.register("paraffin_wax",
            () -> new com.kragnoth.creativeengineering.content.petroleum.ParaffinWaxItem(new Item.Properties()));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.petroleum.OilCanItem> OIL_CAN = ITEMS.register("oil_can",
            () -> new com.kragnoth.creativeengineering.content.petroleum.OilCanItem(new Item.Properties().stacksTo(1)));

    // Tier 1: cementation (press iron + carbon in a basin, then bulk-blast it)
    public static final DeferredItem<Item> CARBON_PACKED_IRON = ITEMS.registerSimpleItem("carbon_packed_iron");
    public static final DeferredItem<Item> BLISTER_STEEL_INGOT = ITEMS.registerSimpleItem("blister_steel_ingot");
    public static final DeferredItem<Item> BLISTER_STEEL_NUGGET = ITEMS.registerSimpleItem("blister_steel_nugget");
    public static final DeferredItem<Item> BLISTER_STEEL_SHEET = ITEMS.registerSimpleItem("blister_steel_sheet");
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLISTER_STEEL_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.BLISTER_STEEL_BLOCK);

    // Tier 2: coking
    public static final DeferredItem<Item> COAL_COKE = ITEMS.registerSimpleItem("coal_coke");
    public static final DeferredItem<net.minecraft.world.item.BlockItem> COKE_OVEN_BRICKS =
            ITEMS.registerSimpleBlockItem(ModBlocks.COKE_OVEN_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> COKE_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.COKE_BLOCK);

    // Tier 3: blast furnace
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLAST_FURNACE_BRICKS = ITEMS.registerSimpleBlockItem(ModBlocks.BLAST_FURNACE_BRICKS);
    public static final DeferredItem<Item> CAST_IRON_INGOT = ITEMS.registerSimpleItem("cast_iron_ingot");
    public static final DeferredItem<Item> CAST_IRON_NUGGET = ITEMS.registerSimpleItem("cast_iron_nugget");
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CAST_IRON_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.CAST_IRON_BLOCK);
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot");
    public static final DeferredItem<Item> STEEL_NUGGET = ITEMS.registerSimpleItem("steel_nugget");
    public static final DeferredItem<Item> STEEL_SHEET = ITEMS.registerSimpleItem("steel_sheet");
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STEEL_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.STEEL_BLOCK);
    public static final DeferredItem<Item> SLAG = ITEMS.registerSimpleItem("slag");
    // Treated timber
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TREATED_PLANKS = ITEMS.registerSimpleBlockItem(ModBlocks.TREATED_PLANKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TREATED_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.TREATED_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TREATED_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.TREATED_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TREATED_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.TREATED_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TREATED_WOOD = ITEMS.registerSimpleBlockItem(ModBlocks.TREATED_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TREATED_BEAM = ITEMS.registerSimpleBlockItem(ModBlocks.TREATED_BEAM);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANDESITE_CAMPFIRE_STAND = ITEMS.registerSimpleBlockItem(ModBlocks.ANDESITE_CAMPFIRE_STAND);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BRASS_CAMPFIRE_STAND = ITEMS.registerSimpleBlockItem(ModBlocks.BRASS_CAMPFIRE_STAND);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> COPPER_CAMPFIRE_STAND = ITEMS.registerSimpleBlockItem(ModBlocks.COPPER_CAMPFIRE_STAND);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DRIVE_PULLEY = ITEMS.registerSimpleBlockItem(ModBlocks.DRIVE_PULLEY);
    public static final DeferredItem<com.kragnoth.creativeengineering.content.engine.DriveBeltItem> DRIVE_BELT = ITEMS.register("drive_belt",
            () -> new com.kragnoth.creativeengineering.content.engine.DriveBeltItem(new net.minecraft.world.item.Item.Properties()));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.petroleum.SeismicProbeItem> SEISMIC_PROBE = ITEMS.register("seismic_probe",
            () -> new com.kragnoth.creativeengineering.content.petroleum.SeismicProbeItem(new net.minecraft.world.item.Item.Properties().durability(64)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PUMPJACK = ITEMS.registerSimpleBlockItem(ModBlocks.PUMPJACK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BEAM_BEARING = ITEMS.registerSimpleBlockItem(ModBlocks.BEAM_BEARING);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> WELLHEAD = ITEMS.registerSimpleBlockItem(ModBlocks.WELLHEAD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ASPHALT = ITEMS.registerSimpleBlockItem(ModBlocks.ASPHALT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ASPHALT_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.ASPHALT_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ASPHALT_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.ASPHALT_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STEEL_PIPE = ITEMS.registerSimpleBlockItem(ModBlocks.STEEL_PIPE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TAP = ITEMS.registerSimpleBlockItem(ModBlocks.TAP);
    public static final DeferredItem<com.simibubi.create.content.fluids.tank.FluidTankItem> STEEL_TANK = ITEMS.register("steel_tank",
            () -> new com.simibubi.create.content.fluids.tank.FluidTankItem(ModBlocks.STEEL_TANK.get(), new net.minecraft.world.item.Item.Properties()));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.shared.LayerPlacingBlockItem> DISTILLING_TANK = ITEMS.register("distilling_tank",
            () -> new com.kragnoth.creativeengineering.content.shared.LayerPlacingBlockItem(ModBlocks.DISTILLING_TANK.get(), new net.minecraft.world.item.Item.Properties()));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.shared.LayerPlacingBlockItem> FERMENTING_VAT = ITEMS.register("fermenting_vat",
            () -> new com.kragnoth.creativeengineering.content.shared.LayerPlacingBlockItem(ModBlocks.FERMENTING_VAT.get(), new net.minecraft.world.item.Item.Properties()));
    // Diesel engine
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_CRANKCASE = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_CRANKCASE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_CYLINDER = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_CYLINDER);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_FLYWHEEL = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_FLYWHEEL);

    // Metalworking: press dies and what they make
    public static final DeferredItem<Item> ROD_DIE = ITEMS.registerSimpleItem("rod_die", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> GEAR_DIE = ITEMS.registerSimpleItem("gear_die", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> STEEL_ROD = ITEMS.registerSimpleItem("steel_rod");
    public static final DeferredItem<Item> STEEL_GEAR = ITEMS.registerSimpleItem("steel_gear");

    // Blister steel gear: iron < blister steel < diamond. Attack values follow vanilla's iron set.
    public static final DeferredItem<SwordItem> BLISTER_STEEL_SWORD = ITEMS.register("blister_steel_sword",
            () -> new ShearForged.Sword(new Item.Properties().attributes(SwordItem.createAttributes(ModTiers.BLISTER_STEEL, 3, -2.4F))));
    public static final DeferredItem<PickaxeItem> BLISTER_STEEL_PICKAXE = ITEMS.register("blister_steel_pickaxe",
            () -> new ShearForged.Pickaxe(new Item.Properties().attributes(PickaxeItem.createAttributes(ModTiers.BLISTER_STEEL, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> BLISTER_STEEL_AXE = ITEMS.register("blister_steel_axe",
            () -> new ShearForged.Axe(new Item.Properties().attributes(AxeItem.createAttributes(ModTiers.BLISTER_STEEL, 6.0F, -3.1F))));
    public static final DeferredItem<ShovelItem> BLISTER_STEEL_SHOVEL = ITEMS.register("blister_steel_shovel",
            () -> new ShearForged.Shovel(new Item.Properties().attributes(ShovelItem.createAttributes(ModTiers.BLISTER_STEEL, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> BLISTER_STEEL_HOE = ITEMS.register("blister_steel_hoe",
            () -> new ShearForged.Hoe(new Item.Properties().attributes(HoeItem.createAttributes(ModTiers.BLISTER_STEEL, -2.0F, -1.0F))));

    public static final DeferredItem<ArmorItem> BLISTER_STEEL_HELMET = armor("blister_steel_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ArmorItem> BLISTER_STEEL_CHESTPLATE = armor("blister_steel_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ArmorItem> BLISTER_STEEL_LEGGINGS = armor("blister_steel_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ArmorItem> BLISTER_STEEL_BOOTS = armor("blister_steel_boots", ArmorItem.Type.BOOTS);

    private static DeferredItem<ArmorItem> armor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ShearForged.Armor(ModArmorMaterials.BLISTER_STEEL, type,
                new Item.Properties().durability(type.getDurability(ModArmorMaterials.BLISTER_STEEL_DURABILITY))));
    }

    // ------------------------------------------------------------------------------------------------ drinks
    /**
     * A bottle (250 mB) of each alcoholic drink: fermented drinks give a light buff and a little nausea, distilled
     * spirits a stronger buff and more of it. Drink it like a potion; it leaves the glass bottle.
     */
    public static final java.util.Map<String, DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem>> DRINKS = new java.util.LinkedHashMap<>();
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> HOOCH_BOTTLE = drink("hooch", 1, e(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 1200, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 200, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> WINE_BOTTLE = drink("wine", 2, e(net.minecraft.world.effect.MobEffects.REGENERATION, 200, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> CIDER_BOTTLE = drink("cider", 2, e(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 1200, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> MEAD_BOTTLE = drink("mead", 3, e(net.minecraft.world.effect.MobEffects.ABSORPTION, 2400, 0), e(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> ALE_BOTTLE = drink("ale", 3, e(net.minecraft.world.effect.MobEffects.DIG_SPEED, 1800, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> VODKA_BOTTLE = drink("vodka", 0, e(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 1200, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 300, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> BRANDY_BOTTLE = drink("brandy", 0, e(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 1800, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 300, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> WHISKEY_BOTTLE = drink("whiskey", 0, e(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 900, 1), e(net.minecraft.world.effect.MobEffects.CONFUSION, 300, 0));
    public static final DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> APPLEJACK_BOTTLE = drink("applejack", 0, e(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 900, 1), e(net.minecraft.world.effect.MobEffects.JUMP, 900, 0), e(net.minecraft.world.effect.MobEffects.CONFUSION, 300, 0));

    private static java.util.function.Supplier<net.minecraft.world.effect.MobEffectInstance> e(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int ticks, int amp) {
        return () -> new net.minecraft.world.effect.MobEffectInstance(effect, ticks, amp);
    }

    @SafeVarargs
    private static DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> drink(String fluid, int nutrition,
                                                                                                 java.util.function.Supplier<net.minecraft.world.effect.MobEffectInstance>... effects) {
        DeferredItem<com.kragnoth.creativeengineering.content.drinks.DrinkItem> item = ITEMS.register(fluid + "_bottle", () -> {
            net.minecraft.world.food.FoodProperties.Builder food = new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(nutrition).saturationModifier(0.1F).alwaysEdible().usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE);
            for (var effect : effects) {
                food.effect(effect, 1F);
            }
            return new com.kragnoth.creativeengineering.content.drinks.DrinkItem(fluid, new Item.Properties().stacksTo(16).food(food.build()));
        });
        DRINKS.put(fluid, item);
        return item;
    }

    /** Every item, in creative-tab order. */
    public static List<DeferredItem<? extends Item>> tabOrder() {
        return List.of(CARBON_PACKED_IRON, BLISTER_STEEL_NUGGET, BLISTER_STEEL_INGOT, BLISTER_STEEL_SHEET, BLISTER_STEEL_BLOCK,
                BLISTER_STEEL_SWORD, BLISTER_STEEL_PICKAXE, BLISTER_STEEL_AXE, BLISTER_STEEL_SHOVEL, BLISTER_STEEL_HOE,
                BLISTER_STEEL_HELMET, BLISTER_STEEL_CHESTPLATE, BLISTER_STEEL_LEGGINGS, BLISTER_STEEL_BOOTS,
                COKE_OVEN_BRICKS, COAL_COKE, COKE_BLOCK, com.kragnoth.creativeengineering.registry.ModFluids.CREOSOTE_BUCKET, com.kragnoth.creativeengineering.registry.ModFluids.PLANT_OIL.bucket,
                com.kragnoth.creativeengineering.registry.ModFluids.ETHANOL.bucket, com.kragnoth.creativeengineering.registry.ModFluids.HOOCH.bucket,
                com.kragnoth.creativeengineering.registry.ModFluids.WINE.bucket, com.kragnoth.creativeengineering.registry.ModFluids.CIDER.bucket,
                com.kragnoth.creativeengineering.registry.ModFluids.MEAD.bucket, com.kragnoth.creativeengineering.registry.ModFluids.ALE.bucket,
                com.kragnoth.creativeengineering.registry.ModFluids.VODKA.bucket, com.kragnoth.creativeengineering.registry.ModFluids.BRANDY.bucket,
                com.kragnoth.creativeengineering.registry.ModFluids.WHISKEY.bucket, com.kragnoth.creativeengineering.registry.ModFluids.APPLEJACK.bucket,
                HOOCH_BOTTLE, WINE_BOTTLE, CIDER_BOTTLE, MEAD_BOTTLE, ALE_BOTTLE, VODKA_BOTTLE, BRANDY_BOTTLE, WHISKEY_BOTTLE, APPLEJACK_BOTTLE,
                com.kragnoth.creativeengineering.registry.ModFluids.CRUDE_OIL.bucket, com.kragnoth.creativeengineering.registry.ModFluids.DIESEL.bucket,
                com.kragnoth.creativeengineering.registry.ModFluids.KEROSENE.bucket, com.kragnoth.creativeengineering.registry.ModFluids.GASOLINE.bucket,
                com.kragnoth.creativeengineering.registry.ModFluids.HEAVY_OIL.bucket, com.kragnoth.creativeengineering.registry.ModFluids.LUBRICANT.bucket,
                PARAFFIN_WAX, OIL_CAN, ASPHALT, ASPHALT_SLAB, ASPHALT_STAIRS,
                BLAST_FURNACE_BRICKS, CAST_IRON_NUGGET, CAST_IRON_INGOT, CAST_IRON_BLOCK,
                STEEL_NUGGET, STEEL_INGOT, STEEL_SHEET, STEEL_BLOCK, SLAG,
                ROD_DIE, GEAR_DIE, STEEL_ROD, STEEL_GEAR,
                TREATED_LOG, TREATED_WOOD, TREATED_PLANKS, TREATED_STAIRS, TREATED_SLAB, TREATED_BEAM,
                DIESEL_CRANKCASE, DIESEL_CYLINDER, DIESEL_FLYWHEEL, DRIVE_PULLEY, DRIVE_BELT, FERMENTING_VAT, DISTILLING_TANK, TAP, STEEL_PIPE, STEEL_TANK, SEISMIC_PROBE, PUMPJACK, BEAM_BEARING, WELLHEAD,
            ANDESITE_CAMPFIRE_STAND, BRASS_CAMPFIRE_STAND, COPPER_CAMPFIRE_STAND);
    }

    /** Blister steel gear carries a one-line "shear-forged" flavour tooltip. */
    static final class ShearForged {
        private ShearForged() {}

        static void flavour(List<Component> tooltip) {
            tooltip.add(Component.translatable("tooltip.creativeengineering.shear_forged").withStyle(ChatFormatting.GRAY));
        }

        static final class Sword extends SwordItem {
            Sword(Properties p) { super(ModTiers.BLISTER_STEEL, p); }
            @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { flavour(t); }
        }
        static final class Pickaxe extends PickaxeItem {
            Pickaxe(Properties p) { super(ModTiers.BLISTER_STEEL, p); }
            @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { flavour(t); }
        }
        static final class Axe extends AxeItem {
            Axe(Properties p) { super(ModTiers.BLISTER_STEEL, p); }
            @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { flavour(t); }
        }
        static final class Shovel extends ShovelItem {
            Shovel(Properties p) { super(ModTiers.BLISTER_STEEL, p); }
            @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { flavour(t); }
        }
        static final class Hoe extends HoeItem {
            Hoe(Properties p) { super(ModTiers.BLISTER_STEEL, p); }
            @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { flavour(t); }
        }
        static final class Armor extends ArmorItem {
            Armor(net.minecraft.core.Holder<net.minecraft.world.item.ArmorMaterial> m, Type type, Properties p) { super(m, type, p); }
            @Override public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) { flavour(t); }
        }
    }
}
