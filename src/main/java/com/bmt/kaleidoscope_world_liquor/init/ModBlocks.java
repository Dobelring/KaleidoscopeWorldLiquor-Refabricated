package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.ChairBlock;
import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.WallRecordBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;




public class ModBlocks {
   public static final Block BOMBAY_SAPPHIRE_GIN = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0),
               Block.box(1.0, 0.0, 5.0, 15.0, 16.0, 11.0),
               Shapes.or(Block.box(1.0, 0.0, 9.0, 15.0, 16.0, 15.0), Block.box(5.0, 0.0, 1.0, 11.0, 16.0, 15.0)),
               Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
            }
         )
         .build();
   public static final Block JACK_DANIEL = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(4.5, 0.0, 4.5, 11.5, 13.0, 11.5),
               Block.box(0.5, 0.0, 4.5, 15.5, 13.0, 11.5),
               Shapes.or(Block.box(0.5, 0.0, 8.5, 15.5, 13.0, 15.5), Block.box(4.5, 0.0, 0.5, 11.5, 13.0, 15.5)),
               Block.box(0.5, 0.0, 0.5, 15.5, 13.0, 15.5)
            }
         )
         .build();
   public static final Block SMIRNOFF_RED_VODKA = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 16.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 16.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
            }
         )
         .build();
   public static final Block ABSOLUT_VODKA = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(5.0, 0.0, 5.0, 11.0, 14.0, 11.0),
               Block.box(1.0, 0.0, 5.0, 15.0, 14.0, 11.0),
               Shapes.or(Block.box(1.0, 0.0, 9.0, 15.0, 14.0, 15.0), Block.box(5.0, 0.0, 1.0, 11.0, 14.0, 15.0)),
               Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0)
            }
         )
         .build();
   public static final Block PINA_COLADA = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 15.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 15.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 15.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 15.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 15.0, 14.0)
            }
         )
         .build();
   public static final Block MAOTAI = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(5.0, 0.0, 5.0, 11.0, 13.0, 11.0),
               Block.box(1.0, 0.0, 5.0, 15.0, 13.0, 11.0),
               Shapes.or(Block.box(1.0, 0.0, 9.0, 15.0, 13.0, 15.0), Block.box(5.0, 0.0, 1.0, 11.0, 13.0, 15.0)),
               Block.box(1.0, 0.0, 1.0, 15.0, 13.0, 15.0)
            }
         )
         .build();
   public static final Block BACARDI_CARTA_BLANCA = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 16.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 16.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
            }
         )
         .build();
   public static final Block SPIRYT_VODKA = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0),
               Block.box(1.0, 0.0, 5.0, 15.0, 16.0, 11.0),
               Shapes.or(Block.box(1.0, 0.0, 9.0, 15.0, 16.0, 15.0), Block.box(5.0, 0.0, 1.0, 11.0, 16.0, 15.0)),
               Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
            }
         )
         .build();
   public static final Block SKYY_VODKA = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 16.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 16.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
            }
         )
         .build();
   public static final Block JOHNNIE_WALKER = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 14.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 14.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 14.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 14.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0)
            }
         )
         .build();
   public static final Block LAFITE_1982 = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 16.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 16.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
            }
         )
         .build();
   public static final Block STRONGBOW = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 16.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 16.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
            }
         )
         .build();
   public static final Block DASSAI = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
               Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0),
               Shapes.or(Block.box(2.0, 0.0, 10.0, 14.0, 16.0, 14.0), Block.box(6.0, 0.0, 2.0, 10.0, 16.0, 14.0)),
               Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
            }
         )
         .build();
   public static final Block KWAS_CHLEBOWY = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0),
               Block.box(1.0, 0.0, 5.0, 15.0, 16.0, 11.0),
               Shapes.or(Block.box(1.0, 0.0, 9.0, 15.0, 16.0, 15.0), Block.box(5.0, 0.0, 1.0, 11.0, 16.0, 15.0)),
               Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
            }
         )
         .build();
   public static final Block BAMBOO_LEAF_GREEN_LIQUOR = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(5.0, 0.0, 5.0, 11.0, 13.0, 11.0),
               Block.box(1.0, 0.0, 5.0, 15.0, 13.0, 11.0),
               Shapes.or(Block.box(1.0, 0.0, 9.0, 15.0, 13.0, 15.0), Block.box(5.0, 0.0, 1.0, 11.0, 13.0, 15.0)),
               Block.box(1.0, 0.0, 1.0, 15.0, 13.0, 15.0)
            }
         )
         .build();
   public static final Block COOL_TEA = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(4.0, 0.0, 5.0, 12.0, 14.0, 11.0),
               Block.box(0.5, 0.0, 4.0, 15.5, 14.0, 12.0),
               Shapes.or(Block.box(0.5, 0.0, 7.0, 15.5, 14.0, 15.5), Block.box(4.0, 0.0, 0.5, 12.0, 14.0, 7.0)),
               Block.box(0.5, 0.0, 0.5, 15.5, 14.0, 15.5)
            }
         )
         .build();
   public static final Block SOUR_PLUM = DrinkBlock.create()
         .maxCount(4)
         .shapes(
            new VoxelShape[]{
               Block.box(4.0, 0.0, 5.0, 12.0, 14.0, 11.0),
               Block.box(0.5, 0.0, 4.0, 15.5, 14.0, 12.0),
               Shapes.or(Block.box(0.5, 0.0, 7.0, 15.5, 14.0, 15.5), Block.box(4.0, 0.0, 0.5, 12.0, 14.0, 7.0)),
               Block.box(0.5, 0.0, 0.5, 15.5, 14.0, 15.5)
            }
         )
         .build();
   public static final Block JERK = new CocktailBlock();
   public static final Block AROUND_THE_WORLD = new CocktailBlock();
   public static final Block LONG_ISLAND_ICED_TEA = new CocktailBlock();
   public static final Block SHRIMP_COOKTAIL = new CocktailBlock();
   public static final Block PINE_COLADA = new CocktailBlock();
   public static final Block GIN_TONIC = new CocktailBlock();
   public static final Block BAR_STOOL_BLACK = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_WHITE = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_LIGHT_GRAY = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_GRAY = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_BROWN = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_RED = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_ORANGE = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_YELLOW = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_LIME = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_GREEN = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_GREEN).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_CYAN = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_LIGHT_BLUE = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_BLUE = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_BLUE).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_PURPLE = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_MAGENTA = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(1.0F).noOcclusion());
   public static final Block BAR_STOOL_PINK = new ChairBlock(Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.0F).noOcclusion());
   public static final Block FREEZER = new FreezerBlock(Properties.of().strength(5.0F, 1200.0F).mapColor(MapColor.WATER).sound(SoundType.METAL).noOcclusion());
   public static final Block WALL_RECORD = new WallRecordBlock(Properties.of().strength(0.0F).sound(SoundType.METAL).noOcclusion());
   public static final Block OAK_BAR_CABINET = new BarCabinetBlock();
   public static final Block OAK_GLASS_BAR_CABINET = new BarCabinetBlock();
   public static final Block BIRCH_BAR_CABINET = new BarCabinetBlock();
   public static final Block SPRUCE_BAR_CABINET = new BarCabinetBlock();
   public static final Block DARK_OAK_BAR_CABINET = new BarCabinetBlock();
   public static final Block CHERRY_BAR_CABINET = new BarCabinetBlock();
   public static final Block OAK_CELLAR_CABINET = new BarCellarCabinetBlock();
   public static final Block BIRCH_CELLAR_CABINET = new BarCellarCabinetBlock();
   public static final Block SPRUCE_CELLAR_CABINET = new BarCellarCabinetBlock();
   public static final Block DARK_OAK_CELLAR_CABINET = new BarCellarCabinetBlock();
   public static final Block CHERRY_CELLAR_CABINET = new BarCellarCabinetBlock();

   
   public static void registerBlocks() {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bombay_sapphire_gin"), BOMBAY_SAPPHIRE_GIN);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "jack_daniel"), JACK_DANIEL);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "smirnoff_red_vodka"), SMIRNOFF_RED_VODKA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "absolut_vodka"), ABSOLUT_VODKA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "pina_colada"), PINA_COLADA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "maotai"), MAOTAI);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bacardi_carta_blanca"), BACARDI_CARTA_BLANCA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "spiryt_vodka"), SPIRYT_VODKA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "skyy_vodka"), SKYY_VODKA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "johnnie_walker"), JOHNNIE_WALKER);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "lafite_1982"), LAFITE_1982);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "strongbow"), STRONGBOW);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "dassai"), DASSAI);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "kwas_chlebowy"), KWAS_CHLEBOWY);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bamboo_leaf_green_liquor"), BAMBOO_LEAF_GREEN_LIQUOR);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cool_tea"), COOL_TEA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "sour_plum"), SOUR_PLUM);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "jerk"), JERK);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "around_the_world"), AROUND_THE_WORLD);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "long_island_iced_tea"), LONG_ISLAND_ICED_TEA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "shrimp_cocktail"), SHRIMP_COOKTAIL);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "pine_colada"), PINE_COLADA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "gin_tonic"), GIN_TONIC);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_black"), BAR_STOOL_BLACK);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_white"), BAR_STOOL_WHITE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_light_gray"), BAR_STOOL_LIGHT_GRAY);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_gray"), BAR_STOOL_GRAY);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_brown"), BAR_STOOL_BROWN);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_red"), BAR_STOOL_RED);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_orange"), BAR_STOOL_ORANGE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_yellow"), BAR_STOOL_YELLOW);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_lime"), BAR_STOOL_LIME);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_green"), BAR_STOOL_GREEN);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_cyan"), BAR_STOOL_CYAN);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_light_blue"), BAR_STOOL_LIGHT_BLUE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_blue"), BAR_STOOL_BLUE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_purple"), BAR_STOOL_PURPLE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_magenta"), BAR_STOOL_MAGENTA);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_stool_pink"), BAR_STOOL_PINK);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "freezer"), FREEZER);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "wall_record"), WALL_RECORD);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "oak_bar_cabinet"), OAK_BAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "oak_glass_bar_cabinet"), OAK_GLASS_BAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "birch_bar_cabinet"), BIRCH_BAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "spruce_bar_cabinet"), SPRUCE_BAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "dark_oak_bar_cabinet"), DARK_OAK_BAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cherry_bar_cabinet"), CHERRY_BAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "oak_cellar_cabinet"), OAK_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "birch_cellar_cabinet"), BIRCH_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "spruce_cellar_cabinet"), SPRUCE_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "dark_oak_cellar_cabinet"), DARK_OAK_CELLAR_CABINET);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cherry_cellar_cabinet"), CHERRY_CELLAR_CABINET);
   }
}
