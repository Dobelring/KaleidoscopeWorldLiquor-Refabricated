package com.bmt.kaleidoscope_world_liquor.integration;

import com.github.ysbbbbbb.kaleidoscopedoll.block.DollBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollItem;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 森罗物语：玩偶联动实现类。
 * 持有全部 kaleidoscopedoll 类引用，仅供 {@link KaleidoscopeDollIntegration#register()}
 * 在确认玩偶模组已加载后调用——本类的加载与校验推迟到彼时，玩偶模组缺席时永不触及。
 */
class KaleidoscopeDollIntegrationImpl {
   private static final Map<String, String> AUTHOR_DOLL_DEFINITIONS = Map.of(
      "doll_0", "contributor_0",
      "doll_1", "contributor_1",
      "doll_2", "contributor_2",
      "doll_3", "contributor_3",
      "doll_4", "contributor_4",
      "doll_5", "contributor_5"
   );

   private KaleidoscopeDollIntegrationImpl() {
   }

   static void register(Map<ResourceLocation, Block> dollBlocks, Map<ResourceLocation, Item> dollItems) {
      AUTHOR_DOLL_DEFINITIONS.forEach((dollId, tooltipKey) -> {
         ResourceLocation id = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", dollId);
         DollBlock block = new DollBlock();
         dollBlocks.put(id, block);
         Registry.register(BuiltInRegistries.BLOCK, id, block);
         ModRegisterEvent.SPECIAL_TOOLTIPS.put(id, tooltipKey);
         DollItem item = new DollItem(block, tooltipKey);
         dollItems.put(id, item);
         Registry.register(BuiltInRegistries.ITEM, id, item);
      });
   }
}
