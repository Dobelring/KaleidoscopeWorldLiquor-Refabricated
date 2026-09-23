package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

/**
 * 冰柜 Ponder 讲解场景的加载守卫（官方同名类的移植）。
 * <p>
 * 官方判的是独立的 {@code ponder} 库模组 id（NeoForge 上 Ponder 是 Create 拆出去的库）；
 * Fabric 侧由 Create Fly 提供 Create 本体、Ponder 已并入其中，模组 id 是 {@code create}，
 * 因此这里改判 {@code create}（与厨房/chinesefood 26.x 的 PonderCompat 一致）。
 * <p>
 * 本类自身不引用任何 Create 类，未装 Create 时也能安全加载。
 */
@Environment(EnvType.CLIENT)
public final class PonderCompat {
    public static final String CREATE_MOD_ID = "create";
    public static boolean PONDER_LOADED = false;

    private PonderCompat() {
    }

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded(CREATE_MOD_ID)) {
            PONDER_LOADED = true;
            FreezerPonderPlugin.init();
            KaleidoscopeWorldLiquor.LOGGER.info("Create is present, queued the Freezer ponder plugin");
        }
    }
}
