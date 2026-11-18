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
    // 官方 1.2.0：自定义唱片移除，新增 friends_drink 唱片曲
    public static final SoundEvent MUSIC_DISC_FRIENDS_DRINK = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("music_disc_friends_drink"));
    public static final SoundEvent POCHI_PUDDING_FEED = SoundEvent.createVariableRangeEvent(KaleidoscopeWorldLiquor.id("pochi_pudding_feed"));

    public ModSounds() {
    }

    public static void registerSounds() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("ice_tea_eat"), ICE_TEA_EAT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("cool_ice_tea_drink"), COOL_ICE_TEA_DRINK);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("sour_plum_drink"), SOUR_PLUM_DRINK);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("music_disc_friends_drink"), MUSIC_DISC_FRIENDS_DRINK);
        Registry.register(BuiltInRegistries.SOUND_EVENT, KaleidoscopeWorldLiquor.id("pochi_pudding_feed"), POCHI_PUDDING_FEED);
    }
}
