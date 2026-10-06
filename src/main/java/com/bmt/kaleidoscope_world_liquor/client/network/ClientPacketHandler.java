package com.bmt.kaleidoscope_world_liquor.client.network;

import com.bmt.kaleidoscope_world_liquor.client.render.TreasureSenseRenderer;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePayload;

/**
 * 服务端 TreasureSensePayload 的客户端落地：把目标列表交给渲染器
 * （渲染器不再自己扫描世界）。
 */
public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleTreasureSense(TreasureSensePayload payload) {
        TreasureSenseRenderer.updateLootTargets(payload.blockPositions(), payload.minecartIds());
    }
}
