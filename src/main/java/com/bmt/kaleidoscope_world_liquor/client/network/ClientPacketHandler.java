package com.bmt.kaleidoscope_world_liquor.client.network;

import com.bmt.kaleidoscope_world_liquor.client.render.TreasureSenseRenderer;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * 客户端 S2C 载荷接收（宝藏感知）：payload → TreasureSenseRenderer.updateLootTargets。
 * 由客户端入口点（KaleidoscopeWorldLiquorClient）调用 {@link #register()}。
 */
@Environment(EnvType.CLIENT)
public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    /** 客户端侧：注册全局接收器（客户端入口调用一次）。 */
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(TreasureSensePayload.TYPE,
                (payload, context) -> context.client().execute(() -> handleTreasureSense(payload)));
    }

    public static void handleTreasureSense(TreasureSensePayload payload) {
        TreasureSenseRenderer.updateLootTargets(payload.blockPositions(), payload.minecartIds());
    }
}
