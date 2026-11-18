package com.bmt.kaleidoscope_world_liquor;

import com.bmt.kaleidoscope_world_liquor.command.BrewCommands;
import com.bmt.kaleidoscope_world_liquor.compat.transfer.FreezerTransfer;
import com.bmt.kaleidoscope_world_liquor.config.ModConfigs;
import com.bmt.kaleidoscope_world_liquor.event.EventHandlers;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModCompatItems;
import com.bmt.kaleidoscope_world_liquor.init.ModCreativeModeTabs;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.init.ModPaintings;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.integration.KaleidoscopeDollIntegration;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaleidoscopeWorldLiquor implements ModInitializer {
   public static final String MODID = "kaleidoscope_world_liquor";
   public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
   @Override
   public void onInitialize() {
      NeoForgeConfigRegistry.INSTANCE.register(MODID, Type.COMMON, ModConfigs.SPEC);
      ModBlocks.registerBlocks();
      ModItems.registerItems();
      ModBlockEntities.registerBlockEntities();
      ModFluids.registerFluids();
      ModSounds.registerSounds();
      ModPaintings.registerPaintings();
      ModRecipes.registerRecipes();
      ModCompatItems.register();
      ModCreativeModeTabs.registerTabs();
      ModEffects.registerEffects();
      KaleidoscopeDollIntegration.register();
      FreezerTransfer.register();
      // 官方 1.1.11：吧台柜/酒窖柜漏斗自动化 + 宝藏感知 S2C payload 类型
      com.bmt.kaleidoscope_world_liquor.compat.transfer.CabinetTransfer.register();
      com.bmt.kaleidoscope_world_liquor.network.NetworkHandler.register();
      EventHandlers.register();
      BrewCommands.register();
      // 官方 1.2.0：干杯之歌服务端跟踪 + 家具燃料 + CarryOn 搬运黑名单
      com.bmt.kaleidoscope_world_liquor.event.FriendsDrinkEvent.register();
      com.bmt.kaleidoscope_world_liquor.init.ModFuels.register();
      com.bmt.kaleidoscope_world_liquor.compat.carryon.CarryOnCompat.register();
   }

   public static ResourceLocation id(String name) {
      return ResourceLocation.fromNamespaceAndPath(MODID, name);
   }
}
