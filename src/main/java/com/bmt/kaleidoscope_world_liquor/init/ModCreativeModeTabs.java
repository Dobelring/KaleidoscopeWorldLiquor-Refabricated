package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.level.ItemLike;

public class ModCreativeModeTabs {
    public static final NonNullList<ItemStack> LIQUOR_ITEMS = NonNullList.create();
    public static final NonNullList<ItemStack> FURNITURE_ITEMS = NonNullList.create();
    public static final CreativeModeTab KALEIDOSCOPE_WORLD_LIQUOR_TAB = FabricItemGroup.builder()
        .icon(() -> new ItemStack((ItemLike) ModItems.BOMBAY_SAPPHIRE_GIN))
        .title(Component.translatable("itemGroup.kaleidoscope_world_liquor_tab"))
        .displayItems((pParameters, output) -> {
            LIQUOR_ITEMS.clear();
            FURNITURE_ITEMS.clear();
            addLiquorItems(output);
            addFurnitureItems(output);
        })
        .build();

    public ModCreativeModeTabs() {
    }

    private static void addLiquorItems(Output output) {
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.BOMBAY_SAPPHIRE_GIN));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.JACK_DANIEL));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SMIRNOFF_RED_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.ABSOLUT_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.PINA_COLADA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.MAOTAI));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.BACARDI_CARTA_BLANCA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SPIRYT_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SKYY_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.JOHNNIE_WALKER));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.LAFITE_1982));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.STRONGBOW));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.DASSAI));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.BAMBOO_LEAF_GREEN_LIQUOR));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.KWAS_CHLEBOWY));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.COOL_TEA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SOUR_PLUM));
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.COLA);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.TONIC_WATER);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.JERK);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.SHRIMP_COOKTAIL);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.AROUND_THE_WORLD);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.HIGHBALL);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.LONG_ISLAND_ICED_TEA);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.PINE_COLADA);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.GIN_TONIC);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModBlocks.FREEZER);
        accept(output, LIQUOR_ITEMS, (ItemLike) ModItems.CUSTOM_RECORD);
        if (!FabricLoader.getInstance().isModLoaded("kaleidoscope_twilight")) {
            accept(output, LIQUOR_ITEMS, (ItemLike) KTItems.LIANGSHAN_ICE_CONE);
            accept(output, LIQUOR_ITEMS, (ItemLike) KTItems.KITA_STUFFED_CRISP);
            accept(output, LIQUOR_ITEMS, (ItemLike) KTItems.POCHI_PUDDING);
            accept(output, LIQUOR_ITEMS, (ItemLike) KTItems.MAGIC_CRISPY_CORNER);
        }

        if (!FabricLoader.getInstance().isModLoaded("smc")) {
            accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(SMCItems.SMC_ICE_TEA_ITEM));
        }
    }

    private static void addFurnitureItems(Output output) {
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_WHITE);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_LIGHT_GRAY);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_GRAY);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_BLACK);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_BROWN);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_RED);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_ORANGE);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_YELLOW);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_LIME);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_GREEN);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_CYAN);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_LIGHT_BLUE);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_BLUE);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_PURPLE);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_MAGENTA);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAR_STOOL_PINK);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.OAK_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.OAK_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BIRCH_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BIRCH_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.SPRUCE_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.SPRUCE_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.DARK_OAK_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.DARK_OAK_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.CHERRY_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.CHERRY_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.JUNGLE_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.JUNGLE_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.ACACIA_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.ACACIA_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.MANGROVE_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.MANGROVE_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAMBOO_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.BAMBOO_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.CRIMSON_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.CRIMSON_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.WARPED_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, (ItemLike) ModBlocks.WARPED_CELLAR_CABINET);
    }

    private static void accept(Output output, NonNullList<ItemStack> categoryItems, ItemStack stack) {
        output.accept(stack);
        categoryItems.add(stack.copy());
    }

    private static void accept(Output output, NonNullList<ItemStack> categoryItems, ItemLike item) {
        output.accept(item);
        categoryItems.add(new ItemStack(item));
    }

    public static void registerCreativeModeTabs() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KaleidoscopeWorldLiquor.id("kaleidoscope_world_liquor_tab"), KALEIDOSCOPE_WORLD_LIQUOR_TAB);
    }
}
