package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.integration.KaleidoscopeDollIntegration;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ModCreativeModeTabs {
   /** 客户端过滤按钮的数据源：displayItems 回调里同步填充，切分类时直接取快照（对应官方的 LIQUOR_ITEMS / FURNITURE_ITEMS）。 */
   public static final NonNullList<ItemStack> LIQUOR_ITEMS = NonNullList.create();
   public static final NonNullList<ItemStack> FURNITURE_ITEMS = NonNullList.create();

   public static final CreativeModeTab KALEIDOSCOPE_WORLD_LIQUOR_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
      .icon(() -> new ItemStack(ModItems.BOMBAY_SAPPHIRE_GIN))
      .title(Component.translatable("itemGroup.kaleidoscope_world_liquor_tab"))
      .displayItems((parameters, output) -> {
         LIQUOR_ITEMS.clear();
         FURNITURE_ITEMS.clear();
         addLiquorItems(output);
         addFurnitureItems(output);
      })
      .build();

   public static void registerTabs() {
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "kaleidoscope_world_liquor_tab"), KALEIDOSCOPE_WORLD_LIQUOR_TAB);
      // 8 幅作者画挂在 tavern 装饰栏 MASTER_MARISA_PAINTING 之后（1.20.1 原版 putAfter 同位）
      ItemGroupEvents.modifyEntriesEvent(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
         ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "tavern_deco"))).register(entries -> entries.addAfter(
         com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.MASTER_MARISA_PAINTING,
         ModPaintings.BFXM_PAINTING_ITEM, ModPaintings.BMT_PAINTING_ITEM, ModPaintings.CHEN_PAINTING_ITEM,
         ModPaintings.DREAM_PAINTING_ITEM, ModPaintings.CHA_PAINTING_ITEM, ModPaintings.RABBIT_PAINTING_ITEM,
         ModPaintings.CH_PAINTING_ITEM, ModPaintings.QXXY_PAINTING_ITEM));
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
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.COLA);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.TONIC_WATER);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.JERK);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.SHRIMP_COOKTAIL);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.AROUND_THE_WORLD);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.HIGHBALL);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.LONG_ISLAND_ICED_TEA);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.PINE_COLADA);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.GIN_TONIC);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.FREEZER);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModItems.CUSTOM_RECORD);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModCompatItems.LIANGSHAN_ICE_CONE);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModCompatItems.KITA_STUFFED_CRISP);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModCompatItems.POCHI_PUDDING);
      accept(output, LIQUOR_ITEMS, (ItemLike)ModCompatItems.MAGIC_CRISPY_CORNER);
      accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModCompatItems.ICE_TEA));
   }

   private static void addFurnitureItems(Output output) {
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_WHITE);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_LIGHT_GRAY);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_GRAY);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_BLACK);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_BROWN);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_RED);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_ORANGE);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_YELLOW);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_LIME);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_GREEN);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_CYAN);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_LIGHT_BLUE);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_BLUE);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_PURPLE);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_MAGENTA);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BAR_STOOL_PINK);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.OAK_BAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.OAK_CELLAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BIRCH_BAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.BIRCH_CELLAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.SPRUCE_BAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.SPRUCE_CELLAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.DARK_OAK_BAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.DARK_OAK_CELLAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.CHERRY_BAR_CABINET);
      accept(output, FURNITURE_ITEMS, (ItemLike)ModItems.CHERRY_CELLAR_CABINET);
      // 玩偶属于装饰类（官方无此内容，分类由移植决定）
      KaleidoscopeDollIntegration.getDollItems().values().forEach(item -> accept(output, FURNITURE_ITEMS, (ItemLike)item));
   }

   private static void accept(Output output, NonNullList<ItemStack> categoryItems, ItemStack stack) {
      output.accept(stack);
      categoryItems.add(stack.copy());
   }

   private static void accept(Output output, NonNullList<ItemStack> categoryItems, ItemLike item) {
      output.accept(item);
      categoryItems.add(new ItemStack(item));
   }
}
