package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.item.BottledDrinkItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;




public class ModItems {
   public static final Item BOMBAY_SAPPHIRE_GIN = new DrinkBlockItem(ModBlocks.BOMBAY_SAPPHIRE_GIN);
   public static final Item JACK_DANIEL = new DrinkBlockItem(ModBlocks.JACK_DANIEL);
   public static final Item SMIRNOFF_RED_VODKA = new DrinkBlockItem(ModBlocks.SMIRNOFF_RED_VODKA);
   public static final Item ABSOLUT_VODKA = new DrinkBlockItem(ModBlocks.ABSOLUT_VODKA);
   public static final Item PINA_COLADA = new DrinkBlockItem(ModBlocks.PINA_COLADA);
   public static final Item MAOTAI = new DrinkBlockItem(ModBlocks.MAOTAI);
   public static final Item BACARDI_CARTA_BLANCA = new DrinkBlockItem(ModBlocks.BACARDI_CARTA_BLANCA);
   public static final Item SPIRYT_VODKA = new DrinkBlockItem(ModBlocks.SPIRYT_VODKA);
   public static final Item SKYY_VODKA = new DrinkBlockItem(ModBlocks.SKYY_VODKA);
   public static final Item JOHNNIE_WALKER = new DrinkBlockItem(ModBlocks.JOHNNIE_WALKER);
   public static final Item LAFITE_1982 = new DrinkBlockItem(ModBlocks.LAFITE_1982);
   public static final Item STRONGBOW = new DrinkBlockItem(ModBlocks.STRONGBOW);
   public static final Item DASSAI = new DrinkBlockItem(ModBlocks.DASSAI);
   public static final Item KWAS_CHLEBOWY = new DrinkBlockItem(ModBlocks.KWAS_CHLEBOWY);
   public static final Item BAMBOO_LEAF_GREEN_LIQUOR = new DrinkBlockItem(ModBlocks.BAMBOO_LEAF_GREEN_LIQUOR);
   public static final Item COOL_TEA = new DrinkBlockItem(ModBlocks.COOL_TEA);
   public static final Item SOUR_PLUM = new DrinkBlockItem(ModBlocks.SOUR_PLUM);
   public static final Item COLA = new BottledDrinkItem(
         new Properties()
            .stacksTo(16)
            .food(
               new Builder()
                  .alwaysEdible()
                  .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 0), 1.0F)
                  .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0), 1.0F)
                  .build()
            )
      );
   public static final Item TONIC_WATER = new BottledDrinkItem(
         new Properties().stacksTo(16).food(new Builder().alwaysEdible().effect(new MobEffectInstance(MobEffects.REGENERATION, 300, 0), 1.0F).build())
      );
   public static final Item JERK = new CocktailBlockItem(ModBlocks.JERK);
   public static final Item AROUND_THE_WORLD = new CocktailBlockItem(ModBlocks.AROUND_THE_WORLD);
   public static final Item LONG_ISLAND_ICED_TEA = new CocktailBlockItem(ModBlocks.LONG_ISLAND_ICED_TEA);
   public static final Item SHRIMP_COOKTAIL = new CocktailBlockItem(ModBlocks.SHRIMP_COOKTAIL);
   public static final Item PINE_COLADA = new CocktailBlockItem(ModBlocks.PINE_COLADA);
   public static final Item GIN_TONIC = new CocktailBlockItem(ModBlocks.GIN_TONIC);
   public static final Item BAR_STOOL_BLACK = new BlockItem(ModBlocks.BAR_STOOL_BLACK, new Properties());
   public static final Item BAR_STOOL_WHITE = new BlockItem(ModBlocks.BAR_STOOL_WHITE, new Properties());
   public static final Item BAR_STOOL_LIGHT_GRAY = new BlockItem(ModBlocks.BAR_STOOL_LIGHT_GRAY, new Properties());
   public static final Item BAR_STOOL_GRAY = new BlockItem(ModBlocks.BAR_STOOL_GRAY, new Properties());
   public static final Item BAR_STOOL_BROWN = new BlockItem(ModBlocks.BAR_STOOL_BROWN, new Properties());
   public static final Item BAR_STOOL_RED = new BlockItem(ModBlocks.BAR_STOOL_RED, new Properties());
   public static final Item BAR_STOOL_ORANGE = new BlockItem(ModBlocks.BAR_STOOL_ORANGE, new Properties());
   public static final Item BAR_STOOL_YELLOW = new BlockItem(ModBlocks.BAR_STOOL_YELLOW, new Properties());
   public static final Item BAR_STOOL_LIME = new BlockItem(ModBlocks.BAR_STOOL_LIME, new Properties());
   public static final Item BAR_STOOL_GREEN = new BlockItem(ModBlocks.BAR_STOOL_GREEN, new Properties());
   public static final Item BAR_STOOL_CYAN = new BlockItem(ModBlocks.BAR_STOOL_CYAN, new Properties());
   public static final Item BAR_STOOL_LIGHT_BLUE = new BlockItem(ModBlocks.BAR_STOOL_LIGHT_BLUE, new Properties());
   public static final Item BAR_STOOL_BLUE = new BlockItem(ModBlocks.BAR_STOOL_BLUE, new Properties());
   public static final Item BAR_STOOL_PURPLE = new BlockItem(ModBlocks.BAR_STOOL_PURPLE, new Properties());
   public static final Item BAR_STOOL_MAGENTA = new BlockItem(ModBlocks.BAR_STOOL_MAGENTA, new Properties());
   public static final Item BAR_STOOL_PINK = new BlockItem(ModBlocks.BAR_STOOL_PINK, new Properties());
   public static final Item OAK_BAR_CABINET = new BlockItem(ModBlocks.OAK_BAR_CABINET, new Properties());
   public static final Item OAK_GLASS_BAR_CABINET = new BlockItem(ModBlocks.OAK_GLASS_BAR_CABINET, new Properties());
   public static final Item BIRCH_BAR_CABINET = new BlockItem(ModBlocks.BIRCH_BAR_CABINET, new Properties());
   public static final Item SPRUCE_BAR_CABINET = new BlockItem(ModBlocks.SPRUCE_BAR_CABINET, new Properties());
   public static final Item DARK_OAK_BAR_CABINET = new BlockItem(ModBlocks.DARK_OAK_BAR_CABINET, new Properties());
   public static final Item CHERRY_BAR_CABINET = new BlockItem(ModBlocks.CHERRY_BAR_CABINET, new Properties());
   public static final Item OAK_CELLAR_CABINET = new BlockItem(ModBlocks.OAK_CELLAR_CABINET, new Properties());
   public static final Item BIRCH_CELLAR_CABINET = new BlockItem(ModBlocks.BIRCH_CELLAR_CABINET, new Properties());
   public static final Item SPRUCE_CELLAR_CABINET = new BlockItem(ModBlocks.SPRUCE_CELLAR_CABINET, new Properties());
   public static final Item DARK_OAK_CELLAR_CABINET = new BlockItem(ModBlocks.DARK_OAK_CELLAR_CABINET, new Properties());
   public static final Item CHERRY_CELLAR_CABINET = new BlockItem(ModBlocks.CHERRY_CELLAR_CABINET, new Properties());
   public static final Item FREEZER = new BlockItem(ModBlocks.FREEZER, new Properties());
   public static final Item CUSTOM_RECORD = new Item(new Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ModJukeboxSongs.RANDOM_DISC));
   public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bombay_sapphire_gin"), BOMBAY_SAPPHIRE_GIN);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "jack_daniel"), JACK_DANIEL);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "smirnoff_red_vodka"), SMIRNOFF_RED_VODKA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "absolut_vodka"), ABSOLUT_VODKA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "pina_colada"), PINA_COLADA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "maotai"), MAOTAI);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bacardi_carta_blanca"), BACARDI_CARTA_BLANCA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "spiryt_vodka"), SPIRYT_VODKA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "skyy_vodka"), SKYY_VODKA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "johnnie_walker"), JOHNNIE_WALKER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "lafite_1982"), LAFITE_1982);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "strongbow"), STRONGBOW);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "dassai"), DASSAI);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "kwas_chlebowy"), KWAS_CHLEBOWY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bamboo_leaf_green_liquor"), BAMBOO_LEAF_GREEN_LIQUOR);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cool_tea"), COOL_TEA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "sour_plum"), SOUR_PLUM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cola"), COLA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "tonic_water"), TONIC_WATER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "jerk"), JERK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "around_the_world"), AROUND_THE_WORLD);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "long_island_iced_tea"), LONG_ISLAND_ICED_TEA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "shrimp_cocktail"), SHRIMP_COOKTAIL);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "pine_colada"), PINE_COLADA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "gin_tonic"), GIN_TONIC);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_black"), BAR_STOOL_BLACK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_white"), BAR_STOOL_WHITE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_light_gray"), BAR_STOOL_LIGHT_GRAY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_gray"), BAR_STOOL_GRAY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_brown"), BAR_STOOL_BROWN);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_red"), BAR_STOOL_RED);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_orange"), BAR_STOOL_ORANGE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_yellow"), BAR_STOOL_YELLOW);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_lime"), BAR_STOOL_LIME);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_green"), BAR_STOOL_GREEN);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_cyan"), BAR_STOOL_CYAN);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_light_blue"), BAR_STOOL_LIGHT_BLUE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_blue"), BAR_STOOL_BLUE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_purple"), BAR_STOOL_PURPLE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_magenta"), BAR_STOOL_MAGENTA);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_pink"), BAR_STOOL_PINK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "oak_bar_cabinet"), OAK_BAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "oak_glass_bar_cabinet"), OAK_GLASS_BAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "birch_bar_cabinet"), BIRCH_BAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "spruce_bar_cabinet"), SPRUCE_BAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "dark_oak_bar_cabinet"), DARK_OAK_BAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cherry_bar_cabinet"), CHERRY_BAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "oak_cellar_cabinet"), OAK_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "birch_cellar_cabinet"), BIRCH_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "spruce_cellar_cabinet"), SPRUCE_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "dark_oak_cellar_cabinet"), DARK_OAK_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cherry_cellar_cabinet"), CHERRY_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "freezer"), FREEZER);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "custom_record"), CUSTOM_RECORD);
   }
}
