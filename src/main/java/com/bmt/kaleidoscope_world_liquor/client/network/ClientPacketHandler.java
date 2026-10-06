package com.bmt.kaleidoscope_world_liquor.client.network;

import com.bmt.kaleidoscope_world_liquor.client.renderer.TreasureSenseRender;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePacket;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleTreasureSense(TreasureSensePacket packet) {
        TreasureSenseRender.updateLootTargets(packet.blockPositions(), packet.minecartIds());
    }
}
