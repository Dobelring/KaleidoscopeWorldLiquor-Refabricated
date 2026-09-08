package com.bmt.kaleidoscope_world_liquor.config;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置（ForgeConfigAPIPort v5，原版只有 enableModdedEffects 一项）。
 */
public final class ModConfigs {
    private ModConfigs() {
    }

    public static ModConfigSpec.BooleanValue ENABLE_MODDED_EFFECTS;

    private static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLE_MODDED_EFFECTS = builder
                .comment("启用后给目标施加游戏内所有的药水效果，关闭仅原版效果")
                .define("enableModdedEffects", false);
        return builder.build();
    }

    public static void register() {
        ConfigRegistry.INSTANCE.register(com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor.MOD_ID,
                ModConfig.Type.COMMON, init());
    }
}
