package com.bmt.kaleidoscope_world_liquor.compat.kaleidoscope_contraption;

public final class KaleidoscopeContraptionCompat {
    private KaleidoscopeContraptionCompat() {
    }

    public static void register() {
        // kaleidoscope_contraption 无 Fabric 版，此联动未移植；
        // 调用点（CreateCompat.register）已保留 FabricLoader.isModLoaded("kaleidoscope_contraption") 守卫
    }
}
