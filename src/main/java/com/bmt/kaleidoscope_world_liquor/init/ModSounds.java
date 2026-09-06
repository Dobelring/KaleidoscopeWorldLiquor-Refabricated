package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
   public static final SoundEvent RANDOM_DISC = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("music_disc.random_disc"));
   public static final SoundEvent RANDOM_DISC_LONG = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("music_disc.random_disc_long"));
   public static final SoundEvent RANDOM_DISC_SHORT = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("music_disc.random_disc_short"));
   public static final SoundEvent ICE_TEA_EAT = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "ice_tea_eat"));
   public static final SoundEvent COOL_ICE_TEA_DRINK = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "cool_ice_tea_drink"));
   public static final SoundEvent SOUR_PLUM_DRINK = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "sour_plum_drink"));
   public static final SoundEvent POCHI_PUDDING_FEED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "pochi_pudding_feed"));

   public static void registerSounds() {
      Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "music_disc.random_disc"), RANDOM_DISC);
      Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "music_disc.random_disc_long"), RANDOM_DISC_LONG);
      Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "music_disc.random_disc_short"), RANDOM_DISC_SHORT);
      Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "ice_tea_eat"), ICE_TEA_EAT);
      Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "cool_ice_tea_drink"), COOL_ICE_TEA_DRINK);
      Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "sour_plum_drink"), SOUR_PLUM_DRINK);
      Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "pochi_pudding_feed"), POCHI_PUDDING_FEED);
   }
}
