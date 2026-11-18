package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import java.util.List;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 官方 1.2.0：家具燃料——全部酒柜/酒窖柜可当燃料烧 300t，顺带把酒馆折叠梯一并注册
 * （官方 NeoForge 走 data map，Forge 线走代码；Fabric 无 data map 等价物，代码注册。
 * Fabric 1.21.1 无 FuelRegistryEvents（0.119+ 才有），用旧版 FuelRegistry.INSTANCE）。
 */
public final class ModFuels {
   private static final int BURN_TIME = 300;

   private ModFuels() {
   }

   public static void register() {
      List<String> woods = List.of("oak", "birch", "spruce", "dark_oak", "cherry", "jungle", "acacia", "mangrove", "bamboo", "crimson", "warped");
      for (String wood : woods) {
         addBlockFuel(wood + "_bar_cabinet");
         addBlockFuel(wood + "_cellar_cabinet");
      }

      addBlockFuel("oak_glass_bar_cabinet");
      addBlockFuel("birch_glass_bar_cabinet");
      addBlockFuel("spruce_glass_bar_cabinet");
      addBlockFuel("dark_oak_glass_bar_cabinet");
      addBlockFuel("cherry_glass_bar_cabinet");
      // 官方 1.2.0：酒馆折叠梯也在燃料表里
      FuelRegistry.INSTANCE.add(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "stepladder")), BURN_TIME);
   }

   private static void addBlockFuel(String path) {
      Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, path));
      Item item = block.asItem();
      if (!item.getDefaultInstance().isEmpty()) {
         FuelRegistry.INSTANCE.add(item, BURN_TIME);
      }
   }
}
