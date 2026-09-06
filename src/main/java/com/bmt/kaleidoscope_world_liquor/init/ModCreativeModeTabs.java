package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.integration.KaleidoscopeDollIntegration;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
   public static final CreativeModeTab KALEIDOSCOPE_WORLD_LIQUOR_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
      .icon(() -> new ItemStack(ModItems.BOMBAY_SAPPHIRE_GIN))
      .title(Component.translatable("itemGroup.kaleidoscope_world_liquor_tab"))
      .displayItems((parameters, output) -> {
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.BOMBAY_SAPPHIRE_GIN));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.JACK_DANIEL));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SMIRNOFF_RED_VODKA));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.ABSOLUT_VODKA));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.PINA_COLADA));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.MAOTAI));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.BACARDI_CARTA_BLANCA));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SPIRYT_VODKA));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SKYY_VODKA));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.JOHNNIE_WALKER));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.LAFITE_1982));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.STRONGBOW));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.DASSAI));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.KWAS_CHLEBOWY));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.BAMBOO_LEAF_GREEN_LIQUOR));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.COOL_TEA));
         output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SOUR_PLUM));
         output.accept(ModItems.COLA);
         output.accept(ModItems.TONIC_WATER);
         output.accept(ModItems.JERK);
         output.accept(ModItems.SHRIMP_COOKTAIL);
         output.accept(ModItems.AROUND_THE_WORLD);
         output.accept(ModItems.LONG_ISLAND_ICED_TEA);
         output.accept(ModItems.PINE_COLADA);
         output.accept(ModItems.GIN_TONIC);
         output.accept(ModItems.FREEZER);
         output.accept(ModItems.CUSTOM_RECORD);
         output.accept(ModCompatItems.LIANGSHAN_ICE_CONE);
         output.accept(ModCompatItems.KITA_STUFFED_CRISP);
         output.accept(ModCompatItems.POCHI_PUDDING);
         output.accept(ModCompatItems.MAGIC_CRISPY_CORNER);
         output.accept(BottleBlockItem.getMaxLevelDrink(ModCompatItems.ICE_TEA));
         KaleidoscopeDollIntegration.getDollItems().values().forEach(output::accept);
      })
      .build();
   public static final CreativeModeTab KALEIDOSCOPE_WORLD_LIQUOR_FURNITURE_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
      .icon(() -> new ItemStack(ModItems.BAR_STOOL_WHITE))
      .title(Component.translatable("itemGroup.kaleidoscope_world_liquor_furniture_tab"))
      .displayItems((parameters, output) -> {
         output.accept(ModItems.BAR_STOOL_WHITE);
         output.accept(ModItems.BAR_STOOL_LIGHT_GRAY);
         output.accept(ModItems.BAR_STOOL_GRAY);
         output.accept(ModItems.BAR_STOOL_BLACK);
         output.accept(ModItems.BAR_STOOL_BROWN);
         output.accept(ModItems.BAR_STOOL_RED);
         output.accept(ModItems.BAR_STOOL_ORANGE);
         output.accept(ModItems.BAR_STOOL_YELLOW);
         output.accept(ModItems.BAR_STOOL_LIME);
         output.accept(ModItems.BAR_STOOL_GREEN);
         output.accept(ModItems.BAR_STOOL_CYAN);
         output.accept(ModItems.BAR_STOOL_LIGHT_BLUE);
         output.accept(ModItems.BAR_STOOL_BLUE);
         output.accept(ModItems.BAR_STOOL_PURPLE);
         output.accept(ModItems.BAR_STOOL_MAGENTA);
         output.accept(ModItems.BAR_STOOL_PINK);
         output.accept(ModItems.OAK_BAR_CABINET);
         output.accept(ModItems.OAK_CELLAR_CABINET);
         output.accept(ModItems.BIRCH_BAR_CABINET);
         output.accept(ModItems.BIRCH_CELLAR_CABINET);
         output.accept(ModItems.SPRUCE_BAR_CABINET);
         output.accept(ModItems.SPRUCE_CELLAR_CABINET);
         output.accept(ModItems.DARK_OAK_BAR_CABINET);
         output.accept(ModItems.DARK_OAK_CELLAR_CABINET);
         output.accept(ModItems.CHERRY_BAR_CABINET);
         output.accept(ModItems.CHERRY_CELLAR_CABINET);
      })
      .build();

   public static void registerTabs() {
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "kaleidoscope_world_liquor_tab"), KALEIDOSCOPE_WORLD_LIQUOR_TAB);
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "kaleidoscope_world_liquor_furniture_tab"), KALEIDOSCOPE_WORLD_LIQUOR_FURNITURE_TAB);
   }
}
