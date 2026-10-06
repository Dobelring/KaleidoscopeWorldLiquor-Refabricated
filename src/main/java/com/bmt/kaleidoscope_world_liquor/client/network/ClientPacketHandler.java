package com.bmt.kaleidoscope_world_liquor.client.network;

import com.bmt.kaleidoscope_world_liquor.client.render.TreasureSenseRenderer;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** 服务端 TreasureSensePayload 的客户端入口：转交渲染器更新目标列表。 */
@Environment(EnvType.CLIENT)
public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleTreasureSense(TreasureSensePayload payload) {
        TreasureSenseRenderer.updateLootTargets(payload.blockPositions(), payload.minecartIds());
    }
}
