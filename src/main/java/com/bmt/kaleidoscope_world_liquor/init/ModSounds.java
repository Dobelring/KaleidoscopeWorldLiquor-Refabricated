package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private ModSounds() {
    }

    public static final SoundEvent ICE_TEA_EAT = register("ice_tea_eat");
    public static final SoundEvent COOL_ICE_TEA_DRINK = register("cool_ice_tea_drink");
    public static final SoundEvent SOUR_PLUM_DRINK = register("sour_plum_drink");
    public static final SoundEvent CUSTOM_RECORD_PLACEHOLDER = register("custom_record_placeholder");
    public static final SoundEvent CUSTOM_MUSIC_1 = register("custom_music_1");
    public static final SoundEvent CUSTOM_MUSIC_2 = register("custom_music_2");
    public static final SoundEvent POCHI_PUDDING_FEED = register("pochi_pudding_feed");
    /** 唱片播放用的音效事件：sounds.json 中以相同权重随机播放两首曲子 */
    public static final SoundEvent MUSIC_DISC_RANDOM_DISC = register("music_disc.random_disc");

    /** 玩偶右键音效（原版 kaleidoscope_doll:block.duck_toy，资产随 liquor 携带） */
    public static final SoundEvent DUCK_TOY = register("duck_toy");

    private static SoundEvent register(String name) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, PortHelper.id(name), SoundEvent.createVariableRangeEvent(PortHelper.id(name)));
    }
    /** 静态字段随类加载完成注册，此方法仅用于在 onInitialize 中固定初始化顺序 */
    public static void register() {
    }
}
