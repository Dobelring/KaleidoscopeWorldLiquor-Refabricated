package com.bmt.kaleidoscope_world_liquor.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

public class ModConfigs {
    public static final ForgeConfigSpec SPEC;
    public static final BooleanValue ENABLE_MODDED_EFFECTS;

    public ModConfigs() {
    }

    static {
        Builder builder = new Builder();
        builder.push("crazy_effect");
        ENABLE_MODDED_EFFECTS = builder.comment("启用后给目标施加游戏内所有的药水效果，关闭仅原版效果").define("enableModdedEffects", false);
        builder.pop();
        SPEC = builder.build();
    }
}
