package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * 官方 1.2.0：家具燃料——全部酒柜/酒窖柜可当燃料烧 300t，顺带把酒馆折叠梯一并注册
 * （官方 NeoForge 走 data map，Forge 线走代码）。
 * <p>
 * 26.3 起原版删了 FuelValues 注册表（fabric-api 的 FuelValueEvents 一并移除），
 * 燃料改为物品 {@code minecraft:cooking_fuel} 组件；组件只能在建物品时给，
 * 覆盖不了外部模组物品，故统一改为 {@link #burnTimeOrZero(Item)} 查表 +
 * {@code AbstractFurnaceBlockEntityMixin} 在 {@code getBurnDuration} 返 0 时回填。
 */
public final class ModFuels {
   private static final int BURN_TIME = 300;
   private static final Map<Item, Integer> FUELS = new HashMap<>();
   private static boolean initialized;

   private ModFuels() {
   }

   public static void register() {
      // 延迟到首次查询再填表：26.3 在 onInitialize 阶段物品组件尚未绑定，
      // 此处立刻 getDefaultInstance 会 NPE（26.1.2/26.2 走 FuelValueEvents 回调是晚绑定，无此问题）
   }

   private static synchronized void init() {
      if (initialized) {
         return;
      }
      initialized = true;
      List<String> woods = List.of("oak", "birch", "spruce", "dark_oak", "cherry", "jungle", "acacia", "mangrove",
            "pale_oak", "poplar", "bamboo", "crimson", "warped");
      for (String wood : woods) {
         addBlockFuel(wood + "_bar_cabinet");
         addBlockFuel(wood + "_cellar_cabinet");
         addBlockFuel(wood + "_glass_bar_cabinet");
      }

      // 官方 1.2.0：酒馆折叠梯也在燃料表里
      Item stepladder = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("kaleidoscope_tavern", "stepladder"));
      if (stepladder != null && stepladder != Items.AIR) {
         FUELS.put(stepladder, BURN_TIME);
      }
   }

   private static void addBlockFuel(String path) {
      Block block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, path));
      if (!block.asItem().getDefaultInstance().isEmpty()) {
         FUELS.put(block.asItem(), BURN_TIME);
      }
   }

   /** mixin 查询口：非燃料返回 -1（与原版「无组件即 0」区分）。 */
   public static int burnTimeOrZero(Item item) {
      if (!initialized) {
         init();
      }
      return FUELS.getOrDefault(item, -1);
   }
}
