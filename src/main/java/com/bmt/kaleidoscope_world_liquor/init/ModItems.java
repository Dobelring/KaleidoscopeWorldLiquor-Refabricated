package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.item.BottledDrinkItem;
import com.bmt.kaleidoscope_world_liquor.item.CustomDrinkItem;
import com.bmt.kaleidoscope_world_liquor.item.CustomRecordItem;
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
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;

public class ModItems {
    public static final Item BOMBAY_SAPPHIRE_GIN = new DrinkBlockItem(ModBlocks.BOMBAY_SAPPHIRE_GIN)
   ;
    public static final Item JACK_DANIEL = new DrinkBlockItem(ModBlocks.JACK_DANIEL);
    public static final Item SMIRNOFF_RED_VODKA = new DrinkBlockItem(ModBlocks.SMIRNOFF_RED_VODKA)
   ;
    public static final Item ABSOLUT_VODKA = new DrinkBlockItem(ModBlocks.ABSOLUT_VODKA);
    public static final Item PINA_COLADA = new DrinkBlockItem(ModBlocks.PINA_COLADA);
    public static final Item MAOTAI = new DrinkBlockItem(ModBlocks.MAOTAI);
    public static final Item BACARDI_CARTA_BLANCA = new DrinkBlockItem(ModBlocks.BACARDI_CARTA_BLANCA)
   ;
    public static final Item SPIRYT_VODKA = new DrinkBlockItem(ModBlocks.SPIRYT_VODKA);
    public static final Item SKYY_VODKA = new DrinkBlockItem(ModBlocks.SKYY_VODKA);
    public static final Item JOHNNIE_WALKER = new DrinkBlockItem(ModBlocks.JOHNNIE_WALKER);
    public static final Item LAFITE_1982 = new DrinkBlockItem(ModBlocks.LAFITE_1982);
    public static final Item STRONGBOW = new DrinkBlockItem(ModBlocks.STRONGBOW);
    public static final Item DASSAI = new DrinkBlockItem(ModBlocks.DASSAI);
    public static final Item KWAS_CHLEBOWY = new DrinkBlockItem(ModBlocks.KWAS_CHLEBOWY);
    public static final Item BAMBOO_LEAF_GREEN_LIQUOR = new DrinkBlockItem(ModBlocks.BAMBOO_LEAF_GREEN_LIQUOR)
   ;
    public static final Item COOL_TEA = new CustomDrinkItem.CoolTea(ModBlocks.COOL_TEA);
    public static final Item SOUR_PLUM = new CustomDrinkItem.SourPlum(ModBlocks.SOUR_PLUM);
    public static final Item COLA = new BottledDrinkItem(
         new Properties()
            .stacksTo(16)
            .food(
               new Builder()
                  .alwaysEat()
                  .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 0), 1.0F)
                  .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0), 1.0F)
                  .build()
            )
      )
   ;
    public static final Item TONIC_WATER = new BottledDrinkItem(
         new Properties().stacksTo(16).food(new Builder().alwaysEat().effect(new MobEffectInstance(MobEffects.REGENERATION, 300, 0), 1.0F).build())
      )
   ;
    public static final Item JERK = new CocktailBlockItem(ModBlocks.JERK);
    public static final Item AROUND_THE_WORLD = new CocktailBlockItem(ModBlocks.AROUND_THE_WORLD)
   ;
    public static final Item LONG_ISLAND_ICED_TEA = new CocktailBlockItem(ModBlocks.LONG_ISLAND_ICED_TEA)
   ;
    public static final Item SHRIMP_COOKTAIL = new CocktailBlockItem(ModBlocks.SHRIMP_COOKTAIL)
   ;
    public static final Item PINE_COLADA = new CocktailBlockItem(ModBlocks.PINE_COLADA);
    public static final Item GIN_TONIC = new CocktailBlockItem(ModBlocks.GIN_TONIC);
    public static final Item HIGHBALL = new CocktailBlockItem(ModBlocks.HIGHBALL);
    public static final Item CUSTOM_RECORD = new CustomRecordItem();

    public ModItems() {
    }

    /** 由 ModBlocks.registerBlocks() 先行创建方块后调用；与官方 registerBlock 辅助函数的自动 BlockItem 注册等价。 */
    private static void blockItem(String id, Block block) {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, id), new BlockItem(block, new Item.Properties()));
    }

    public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "bombay_sapphire_gin"), BOMBAY_SAPPHIRE_GIN);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "jack_daniel"), JACK_DANIEL);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "smirnoff_red_vodka"), SMIRNOFF_RED_VODKA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "absolut_vodka"), ABSOLUT_VODKA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "pina_colada"), PINA_COLADA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "maotai"), MAOTAI);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "bacardi_carta_blanca"), BACARDI_CARTA_BLANCA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "spiryt_vodka"), SPIRYT_VODKA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "skyy_vodka"), SKYY_VODKA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "johnnie_walker"), JOHNNIE_WALKER);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "lafite_1982"), LAFITE_1982);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "strongbow"), STRONGBOW);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "dassai"), DASSAI);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "kwas_chlebowy"), KWAS_CHLEBOWY);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "bamboo_leaf_green_liquor"), BAMBOO_LEAF_GREEN_LIQUOR);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "cool_tea"), COOL_TEA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "sour_plum"), SOUR_PLUM);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "cola"), COLA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "tonic_water"), TONIC_WATER);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "jerk"), JERK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "around_the_world"), AROUND_THE_WORLD);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "long_island_iced_tea"), LONG_ISLAND_ICED_TEA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "shrimp_cocktail"), SHRIMP_COOKTAIL);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "pine_colada"), PINE_COLADA);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "gin_tonic"), GIN_TONIC);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "highball"), HIGHBALL);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KaleidoscopeWorldLiquor.MODID, "custom_record"), CUSTOM_RECORD);
        blockItem("bar_stool_black", ModBlocks.BAR_STOOL_BLACK);
        blockItem("bar_stool_white", ModBlocks.BAR_STOOL_WHITE);
        blockItem("bar_stool_light_gray", ModBlocks.BAR_STOOL_LIGHT_GRAY);
        blockItem("bar_stool_gray", ModBlocks.BAR_STOOL_GRAY);
        blockItem("bar_stool_brown", ModBlocks.BAR_STOOL_BROWN);
        blockItem("bar_stool_red", ModBlocks.BAR_STOOL_RED);
        blockItem("bar_stool_orange", ModBlocks.BAR_STOOL_ORANGE);
        blockItem("bar_stool_yellow", ModBlocks.BAR_STOOL_YELLOW);
        blockItem("bar_stool_lime", ModBlocks.BAR_STOOL_LIME);
        blockItem("bar_stool_green", ModBlocks.BAR_STOOL_GREEN);
        blockItem("bar_stool_cyan", ModBlocks.BAR_STOOL_CYAN);
        blockItem("bar_stool_light_blue", ModBlocks.BAR_STOOL_LIGHT_BLUE);
        blockItem("bar_stool_blue", ModBlocks.BAR_STOOL_BLUE);
        blockItem("bar_stool_purple", ModBlocks.BAR_STOOL_PURPLE);
        blockItem("bar_stool_magenta", ModBlocks.BAR_STOOL_MAGENTA);
        blockItem("bar_stool_pink", ModBlocks.BAR_STOOL_PINK);
        blockItem("freezer", ModBlocks.FREEZER);
        blockItem("oak_bar_cabinet", ModBlocks.OAK_BAR_CABINET);
        blockItem("oak_glass_bar_cabinet", ModBlocks.OAK_GLASS_BAR_CABINET);
        blockItem("birch_bar_cabinet", ModBlocks.BIRCH_BAR_CABINET);
        blockItem("spruce_bar_cabinet", ModBlocks.SPRUCE_BAR_CABINET);
        blockItem("dark_oak_bar_cabinet", ModBlocks.DARK_OAK_BAR_CABINET);
        blockItem("cherry_bar_cabinet", ModBlocks.CHERRY_BAR_CABINET);
        blockItem("oak_cellar_cabinet", ModBlocks.OAK_CELLAR_CABINET);
        blockItem("birch_cellar_cabinet", ModBlocks.BIRCH_CELLAR_CABINET);
        blockItem("spruce_cellar_cabinet", ModBlocks.SPRUCE_CELLAR_CABINET);
        blockItem("dark_oak_cellar_cabinet", ModBlocks.DARK_OAK_CELLAR_CABINET);
        blockItem("cherry_cellar_cabinet", ModBlocks.CHERRY_CELLAR_CABINET);
        blockItem("jungle_bar_cabinet", ModBlocks.JUNGLE_BAR_CABINET);
        blockItem("jungle_cellar_cabinet", ModBlocks.JUNGLE_CELLAR_CABINET);
        blockItem("acacia_bar_cabinet", ModBlocks.ACACIA_BAR_CABINET);
        blockItem("acacia_cellar_cabinet", ModBlocks.ACACIA_CELLAR_CABINET);
        blockItem("mangrove_bar_cabinet", ModBlocks.MANGROVE_BAR_CABINET);
        blockItem("mangrove_cellar_cabinet", ModBlocks.MANGROVE_CELLAR_CABINET);
        blockItem("bamboo_bar_cabinet", ModBlocks.BAMBOO_BAR_CABINET);
        blockItem("bamboo_cellar_cabinet", ModBlocks.BAMBOO_CELLAR_CABINET);
        blockItem("crimson_bar_cabinet", ModBlocks.CRIMSON_BAR_CABINET);
        blockItem("crimson_cellar_cabinet", ModBlocks.CRIMSON_CELLAR_CABINET);
        blockItem("warped_bar_cabinet", ModBlocks.WARPED_BAR_CABINET);
        blockItem("warped_cellar_cabinet", ModBlocks.WARPED_CELLAR_CABINET);
    }
}