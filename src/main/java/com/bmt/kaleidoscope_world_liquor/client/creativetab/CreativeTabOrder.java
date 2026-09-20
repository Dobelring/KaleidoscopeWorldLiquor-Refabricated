package com.bmt.kaleidoscope_world_liquor.client.creativetab;

import com.bmt.kaleidoscope_world_liquor.mixins.accessor.CreativeModeTabAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

/**
 * 让本模组 tab 紧邻在 tavern 两个 tab 之前（官方挂 NeoForge CreativeModeTabRegistry 那份 mixin 的目的）。
 *
 * 原版创造界面是按每个 tab 自己的 row()/column() 摆放按钮、只用 CreativeModeTabs#tabs() 的列表顺序做迭代，
 * 而 Fabric 的 fabric-item-group-api-v1 会按命名空间给所有非原版 tab 重新分配 row/column（分页），
 * 结果固定为 tavern_deco、tavern_main、本模组 tab —— 所以只重排列表改不了视觉位置，必须在位置层落位。
 *
 * 做法：取这三个 tab 当前占用的槽位，按 [本模组, tavern_main, tavern_deco] 的顺序重新填入这些槽位。
 * 槽位集合不变，不会产生重复位置；被别的模组 tab 隔开时也只会退化成"本模组 tab 最靠前"。
 */
@Environment(EnvType.CLIENT)
public final class CreativeTabOrder {
   private CreativeTabOrder() {
   }

   public static void apply() {
      CreativeModeTab ourTab = tab("kaleidoscope_world_liquor", "kaleidoscope_world_liquor_tab");
      CreativeModeTab tavernMain = tab("kaleidoscope_tavern", "tavern_main");
      CreativeModeTab tavernDeco = tab("kaleidoscope_tavern", "tavern_deco");
      if (ourTab == null || tavernMain == null || tavernDeco == null) {
         return;
      }

      List<CreativeModeTab> wanted = List.of(ourTab, tavernMain, tavernDeco);
      List<Slot> slots = new ArrayList<>(wanted.size());
      for (CreativeModeTab tab : wanted) {
         slots.add(new Slot(tab.row(), tab.column()));
      }

      if (slotSharedWithOtherMod(wanted, slots)) {
         // 槽位被别的非原版 tab 占用 → 说明这三个 tab 被分到了不同页（不同页可以共用同一 row/column），换位会重叠
         return;
      }

      slots.sort(Comparator.comparingInt((Slot slot) -> slot.row().ordinal()).thenComparingInt(Slot::column));

      for (int i = 0; i < wanted.size(); i++) {
         CreativeModeTab tab = wanted.get(i);
         Slot slot = slots.get(i);
         if (tab.row() == slot.row() && tab.column() == slot.column()) {
            continue;
         }

         CreativeModeTabAccessor accessor = (CreativeModeTabAccessor)tab;
         accessor.kwl$setRow(slot.row());
         accessor.kwl$setColumn(slot.column());
      }
   }

   /** 原版 tab 由 builder 显式指定 row/column、不参与 Fabric 的分页分配，故只看非 minecraft 命名空间。 */
   private static boolean slotSharedWithOtherMod(List<CreativeModeTab> wanted, List<Slot> slots) {
      for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
         if (wanted.contains(tab)) {
            continue;
         }

         ResourceLocation id = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
         if (id == null || "minecraft".equals(id.getNamespace())) {
            continue;
         }

         if (slots.contains(new Slot(tab.row(), tab.column()))) {
            return true;
         }
      }

      return false;
   }

   private static CreativeModeTab tab(String namespace, String path) {
      return BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
   }

   private record Slot(CreativeModeTab.Row row, int column) {
   }
}
