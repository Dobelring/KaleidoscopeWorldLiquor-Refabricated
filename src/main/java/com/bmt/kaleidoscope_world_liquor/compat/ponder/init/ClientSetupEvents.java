package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Fabric 上不再使用 Forge 的 @EventBusSubscriber + FMLClientSetupEvent，
// 改由客户端入口点（client.KaleidoscopeWorldLiquorClient）在初始化时调用 register()
@Environment(EnvType.CLIENT)
public class ClientSetupEvents {
    public ClientSetupEvents() {
    }

    // 原 FMLClientSetupEvent#onClientSetup → Fabric 静态注册入口
    public static void register() {
        PonderCompat.init();
    }
}
