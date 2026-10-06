package com.bmt.kaleidoscope_world_liquor.client.network;

import com.bmt.kaleidoscope_world_liquor.client.renderer.TreasureSenseRenderer;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePayload;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleTreasureSense(TreasureSensePayload payload) {
        TreasureSenseRenderer.updateLootTargets(payload.blockPositions(), payload.minecartIds());
    }
}
