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
import com.bmt.kaleidoscope_world_liquor.init.ModEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.init.ModPaintings;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.integration.KaleidoscopeDollIntegration;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.fml.config.ModConfig.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaleidoscopeWorldLiquor implements ModInitializer {
   public static final String MODID = "kaleidoscope_world_liquor";
   public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
   public static final ResourceKey<DamageType> LIFE_OR_DEATH_DAMAGE = ResourceKey.create(
      Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "life_or_death")
   );

   @Override
   public void onInitialize() {
      NeoForgeConfigRegistry.INSTANCE.register(MODID, Type.COMMON, ModConfigs.SPEC);
      ModBlocks.registerBlocks();
      ModItems.registerItems();
      ModBlockEntities.registerBlockEntities();
      ModFluids.registerFluids();
      ModEntities.registerEntities();
      ModSounds.registerSounds();
      ModPaintings.registerPaintings();
      ModRecipes.registerRecipes();
      ModCompatItems.register();
      ModCreativeModeTabs.registerTabs();
      ModEffects.registerEffects();
      KaleidoscopeDollIntegration.register();
      FreezerTransfer.register();
      EventHandlers.register();
      BrewCommands.register();
   }

   public static ResourceLocation id(String name) {
      return ResourceLocation.fromNamespaceAndPath(MODID, name);
   }
}
