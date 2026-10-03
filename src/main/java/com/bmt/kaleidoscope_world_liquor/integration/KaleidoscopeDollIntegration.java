package com.bmt.kaleidoscope_world_liquor.integration;

import net.fabricmc.loader.api.FabricLoader;

/**
 * 与「森罗物语：玩偶」的联动入口（门控类）。
 * <p>
 * 语义与原 Forge 版一致：只有当玩偶模组存在、且 kaleidoscope_nether（本应提供这些
 * 作者玩偶的联动模组）缺失时，才由本模组注册 {@code kaleidoscope_world_liquor:doll_0..doll_5}。
 * <p>
 * 注意：本类不能引用玩偶模组的任何类（软依赖），真正的注册在
 * {@link KaleidoscopeDollIntegrationImpl} 里，只有守卫通过时才会被类加载。
 * JVM 对被调用指令引用的类是延迟解析的：守卫为 false 时既不会加载 Impl，
 * 也不会触碰 {@code com.github.ysbbbbbb.kaleidoscopedoll.*} 的任何类
 * （防 NoClassDefFoundError）。
 */
public class KaleidoscopeDollIntegration {
    private static final String DOLL_MOD_ID = "kaleidoscope_doll";
    private static final String NETHER_MOD_ID = "kaleidoscope_nether";

    public KaleidoscopeDollIntegration() {
    }

    /**
     * 原 Forge 版为 {@code register(IEventBus)}（把 Impl 挂到 mod 总线上）；
     * Fabric 无 mod 总线，改为主类无参调用本方法。
     */
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("kaleidoscope_doll") && !FabricLoader.getInstance().isModLoaded("kaleidoscope_nether")) {
            KaleidoscopeDollIntegrationImpl.register();
        }
    }
}
