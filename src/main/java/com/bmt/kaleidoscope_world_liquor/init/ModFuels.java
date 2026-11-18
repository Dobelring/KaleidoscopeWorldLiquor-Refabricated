package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import java.util.List;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;

/**
 * 官方 1.2.0：家具燃料——全部酒柜/酒窖柜可当燃料烧 300t，顺带把酒馆折叠梯一并注册
 * （官方 NeoForge 走 data map，Forge 线走代码；Fabric 无 data map 等价物，代码注册。
 *   26.x Fabric API 为 FuelValueEvents.BUILD + FuelValues.Builder）。
 */
public final class ModFuels {
   private static final int BURN_TIME = 300;

   private ModFuels() {
   }

   public static void register() {
      FuelValueEvents.BUILD.register((builder, context) -> {
         List<String> woods = List.of("oak", "birch", "spruce", "dark_oak", "cherry", "jungle", "acacia", "mangrove", "pale_oak", "bamboo", "crimson", "warped");
         for (String wood : woods) {
            addBlockFuel(builder, wood + "_bar_cabinet");
            addBlockFuel(builder, wood + "_cellar_cabinet");
         }

         addBlockFuel(builder, "oak_glass_bar_cabinet");
         addBlockFuel(builder, "birch_glass_bar_cabinet");
         addBlockFuel(builder, "spruce_glass_bar_cabinet");
         addBlockFuel(builder, "dark_oak_glass_bar_cabinet");
         addBlockFuel(builder, "cherry_glass_bar_cabinet");
         // 官方 1.2.0：酒馆折叠梯也在燃料表里
         builder.add(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("kaleidoscope_tavern", "stepladder")), BURN_TIME);
      });
   }

   private static void addBlockFuel(FuelValues.Builder builder, String path) {
      Block block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, path));
      if (!block.asItem().getDefaultInstance().isEmpty()) {
         builder.add(block.asItem(), BURN_TIME);
      }
   }
}