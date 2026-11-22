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

    // Tier 1: cementation (press iron + carbon in a basin, then bulk-blast it)
    public static final DeferredItem<Item> CARBON_PACKED_IRON = ITEMS.registerSimpleItem("carbon_packed_iron");
    public static final DeferredItem<Item> BLISTER_STEEL_INGOT = ITEMS.registerSimpleItem("blister_steel_ingot");
    public static final DeferredItem<Item> BLISTER_STEEL_NUGGET = ITEMS.registerSimpleItem("blister_steel_nugget");
    public static final DeferredItem<Item> BLISTER_STEEL_SHEET = ITEMS.registerSimpleItem("blister_steel_sheet");
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLISTER_STEEL_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.BLISTER_STEEL_BLOCK);

    // Tier 2: coking
    public static final DeferredItem<Item> COAL_COKE = ITEMS.registerSimpleItem("coal_coke");
    public static final DeferredItem<Item> COKE_DUST = ITEMS.registerSimpleItem("coke_dust");
    public static final DeferredItem<net.minecraft.world.item.BlockItem> COKE_OVEN_BRICKS =
            ITEMS.registerSimpleBlockItem(ModBlocks.COKE_OVEN_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> COKE_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.COKE_BLOCK);

    // Tier 3: blast furnace
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLAST_FURNACE_BRICKS = ITEMS.registerSimpleBlockItem(ModBlocks.BLAST_FURNACE_BRICKS);
    public static final DeferredItem<Item> CAST_IRON_INGOT = ITEMS.registerSimpleItem("cast_iron_ingot");
    public static final DeferredItem<Item> CAST_IRON_NUGGET = ITEMS.registerSimpleItem("cast_iron_nugget");
    public static final DeferredItem<Item> CAST_IRON_SHEET = ITEMS.registerSimpleItem("cast_iron_sheet");
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

    // Diesel engine (first pass, look only)
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_CRANKCASE = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_CRANKCASE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_CYLINDER = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_CYLINDER);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_EXHAUST = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_EXHAUST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_FLYWHEEL = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_FLYWHEEL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DIESEL_EXHAUST_STACK = ITEMS.registerSimpleBlockItem(ModBlocks.DIESEL_EXHAUST_STACK);

    // Metalworking: press dies and what they make
    public static final DeferredItem<Item> ROD_DIE = ITEMS.registerSimpleItem("rod_die", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> GEAR_DIE = ITEMS.registerSimpleItem("gear_die", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> COPPER_ROD = ITEMS.registerSimpleItem("copper_rod");
    public static final DeferredItem<Item> IRON_ROD = ITEMS.registerSimpleItem("iron_rod");
    public static final DeferredItem<Item> STEEL_ROD = ITEMS.registerSimpleItem("steel_rod");
    public static final DeferredItem<Item> IRON_GEAR = ITEMS.registerSimpleItem("iron_gear");
    public static final DeferredItem<Item> STEEL_GEAR = ITEMS.registerSimpleItem("steel_gear");
    public static final DeferredItem<Item> COPPER_WIRE = ITEMS.registerSimpleItem("copper_wire");
    public static final DeferredItem<Item> STEEL_WIRE = ITEMS.registerSimpleItem("steel_wire");
    // Tier 4: alloy furnace
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ALLOY_FURNACE_BRICKS = ITEMS.registerSimpleBlockItem(ModBlocks.ALLOY_FURNACE_BRICKS);

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

    /** Every item, in creative-tab order. */
    public static List<DeferredItem<? extends Item>> tabOrder() {
        return List.of(CARBON_PACKED_IRON, BLISTER_STEEL_NUGGET, BLISTER_STEEL_INGOT, BLISTER_STEEL_SHEET, BLISTER_STEEL_BLOCK,
                BLISTER_STEEL_SWORD, BLISTER_STEEL_PICKAXE, BLISTER_STEEL_AXE, BLISTER_STEEL_SHOVEL, BLISTER_STEEL_HOE,
                BLISTER_STEEL_HELMET, BLISTER_STEEL_CHESTPLATE, BLISTER_STEEL_LEGGINGS, BLISTER_STEEL_BOOTS,
                COKE_OVEN_BRICKS, COAL_COKE, COKE_DUST, COKE_BLOCK, com.kragnoth.creativeengineering.registry.ModFluids.CREOSOTE_BUCKET,
                BLAST_FURNACE_BRICKS, CAST_IRON_NUGGET, CAST_IRON_INGOT, CAST_IRON_SHEET, CAST_IRON_BLOCK,
                STEEL_NUGGET, STEEL_INGOT, STEEL_SHEET, STEEL_BLOCK, SLAG, ALLOY_FURNACE_BRICKS,
                ROD_DIE, GEAR_DIE, COPPER_ROD, IRON_ROD, STEEL_ROD, IRON_GEAR, STEEL_GEAR, COPPER_WIRE, STEEL_WIRE,
                TREATED_LOG, TREATED_WOOD, TREATED_PLANKS, TREATED_STAIRS, TREATED_SLAB, TREATED_BEAM,
                DIESEL_CRANKCASE, DIESEL_CYLINDER, DIESEL_EXHAUST, DIESEL_FLYWHEEL, DIESEL_EXHAUST_STACK);
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
