package com.bmt.kaleidoscope_world_liquor.compat.carryon;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.fabricmc.loader.api.FabricLoader;

/**
 * 官方 1.2.0：Carry On 联动——把冰箱加入其禁搬列表（ListHandler.addForbiddenTiles）。
 * Fabric 端 Carry On 同 API，走反射避免硬依赖（未装 Carry On 时零开销）。
 */
public final class CarryOnCompat {
   private static final String FORBIDDEN_TILE = KaleidoscopeWorldLiquor.MODID + ":freezer";

   private CarryOnCompat() {
   }

   public static void register() {
      if (!FabricLoader.getInstance().isModLoaded("carryon")) {
         return;
      }

      try {
         Class<?> handler = Class.forName("tschipp.carryon.common.config.ListHandler");
         handler.getMethod("addForbiddenTiles", String.class).invoke(null, FORBIDDEN_TILE);
      } catch (Throwable t) {
         KaleidoscopeWorldLiquor.LOGGER.warn("[carryon] failed to blacklist freezer: {}", t.toString());
      }
   }
}