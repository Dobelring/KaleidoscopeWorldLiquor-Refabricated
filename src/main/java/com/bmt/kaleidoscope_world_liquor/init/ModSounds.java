package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent ICE_TEA_EAT = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("ice_tea_eat"));
    public static final SoundEvent COOL_ICE_TEA_DRINK = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("cool_ice_tea_drink"));
    public static final SoundEvent SOUR_PLUM_DRINK = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("sour_plum_drink"));
    public static final SoundEvent CUSTOM_RECORD_PLACEHOLDER = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("custom_record_placeholder"));
    public static final SoundEvent CUSTOM_MUSIC_1 = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("custom_music_1"));
    public static final SoundEvent CUSTOM_MUSIC_2 = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("custom_music_2"));
    public static final SoundEvent POCHI_PUDDING_FEED = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("pochi_pudding_feed"));

    public ModSounds() {
    }

    public static void registerSounds() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("ice_tea_eat"), ICE_TEA_EAT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("cool_ice_tea_drink"), COOL_ICE_TEA_DRINK);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("sour_plum_drink"), SOUR_PLUM_DRINK);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("custom_record_placeholder"), CUSTOM_RECORD_PLACEHOLDER);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("custom_music_1"), CUSTOM_MUSIC_1);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("custom_music_2"), CUSTOM_MUSIC_2);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("pochi_pudding_feed"), POCHI_PUDDING_FEED);
    }
}
