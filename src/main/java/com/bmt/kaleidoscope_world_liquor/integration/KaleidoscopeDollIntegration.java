package com.bmt.kaleidoscope_world_liquor.integration;

import com.github.ysbbbbbb.kaleidoscopedoll.block.DollBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollItem;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 森罗物语：玩偶（kaleidoscope_doll）联动。
 * 当玩偶模组加载时，注册 doll_0..5 作者玩偶方块/物品到本模组命名空间，
 * 并登记 SPECIAL_TOOLTIPS 供玩偶模组显示贡献者提示。
 */
public class KaleidoscopeDollIntegration {
   private static final Map<ResourceLocation, Block> DOLL_BLOCKS = new LinkedHashMap<>();
   private static final Map<ResourceLocation, Item> DOLL_ITEMS = new LinkedHashMap<>();
   private static final Map<String, String> AUTHOR_DOLL_DEFINITIONS = Map.of(
      "doll_0", "contributor_0",
      "doll_1", "contributor_1",
      "doll_2", "contributor_2",
      "doll_3", "contributor_3",
      "doll_4", "contributor_4",
      "doll_5", "contributor_5"
   );

   private KaleidoscopeDollIntegration() {
   }

   public static void register() {
      if (!FabricLoader.getInstance().isModLoaded("kaleidoscope_doll")) {
         return;
      }

      AUTHOR_DOLL_DEFINITIONS.forEach((dollId, tooltipKey) -> {
         ResourceLocation id = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", dollId);
         DollBlock block = new DollBlock();
         DOLL_BLOCKS.put(id, block);
         Registry.register(BuiltInRegistries.BLOCK, id, block);
         ModRegisterEvent.SPECIAL_TOOLTIPS.put(id, tooltipKey);
         DollItem item = new DollItem(block, tooltipKey);
         DOLL_ITEMS.put(id, item);
         Registry.register(BuiltInRegistries.ITEM, id, item);
      });
   }

   public static Map<ResourceLocation, Item> getDollItems() {
      return DOLL_ITEMS;
   }
}
