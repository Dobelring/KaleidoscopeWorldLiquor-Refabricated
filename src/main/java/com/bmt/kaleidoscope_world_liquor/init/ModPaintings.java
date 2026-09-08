package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.github.ysbbbbbb.kaleidoscopetavern.block.deco.PaintingBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;

/**
 * 8 幅作者画：物品=AuthorPaintingItem（背包 tooltip 作者行；放出后 WTHIT 不显示）。
 * 手持渲染=item 模型 parent=kaleidoscope_tavern:item/painting_base（画框 3D+全姿势）。
 */
public class ModPaintings {
   public static final Block BFXM_PAINTING = new PaintingBlock();
   public static final Block BMT_PAINTING = new PaintingBlock();
   public static final Block DREAM_PAINTING = new PaintingBlock();
   public static final Block CHA_PAINTING = new PaintingBlock();
   public static final Block CHEN_PAINTING = new PaintingBlock();
   public static final Block RABBIT_PAINTING = new PaintingBlock();
   public static final Block CH_PAINTING = new PaintingBlock();
   public static final Block QXXY_PAINTING = new PaintingBlock();
   public static final Item BFXM_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(BFXM_PAINTING, new Properties());
   public static final Item BMT_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(BMT_PAINTING, new Properties());
   public static final Item DREAM_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(DREAM_PAINTING, new Properties());
   public static final Item CHA_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(CHA_PAINTING, new Properties());
   public static final Item CHEN_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(CHEN_PAINTING, new Properties());
   public static final Item RABBIT_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(RABBIT_PAINTING, new Properties());
   public static final Item CH_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(CH_PAINTING, new Properties());
   public static final Item QXXY_PAINTING_ITEM = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(QXXY_PAINTING, new Properties());

   public static void registerPaintings() {
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "bfxm_painting"), BFXM_PAINTING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "bmt_painting"), BMT_PAINTING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "dream_painting"), DREAM_PAINTING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "cha_painting"), CHA_PAINTING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "chen_painting"), CHEN_PAINTING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "rabbit_painting"), RABBIT_PAINTING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "ch_painting"), CH_PAINTING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "qxxy_painting"), QXXY_PAINTING);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "bfxm_painting"), BFXM_PAINTING_ITEM);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "bmt_painting"), BMT_PAINTING_ITEM);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "dream_painting"), DREAM_PAINTING_ITEM);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "cha_painting"), CHA_PAINTING_ITEM);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "chen_painting"), CHEN_PAINTING_ITEM);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "rabbit_painting"), RABBIT_PAINTING_ITEM);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "ch_painting"), CH_PAINTING_ITEM);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "qxxy_painting"), QXXY_PAINTING_ITEM);
   }
}
