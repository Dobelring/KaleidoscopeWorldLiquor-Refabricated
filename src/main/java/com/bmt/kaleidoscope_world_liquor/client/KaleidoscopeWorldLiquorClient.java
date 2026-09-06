package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.client.renderer.ClientForgeEvents;
import com.bmt.kaleidoscope_world_liquor.client.renderer.TreasureSenseRenderer;
import com.bmt.kaleidoscope_world_liquor.event.EventHandlers;
import com.bmt.kaleidoscope_world_liquor.event.MusicDiscEvents;
import com.bmt.kaleidoscope_world_liquor.event.TooltipEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class KaleidoscopeWorldLiquorClient implements ClientModInitializer {
   @Override
   public void onInitializeClient() {
      Clients.registerRenderers();
      Clients.registerRenderLayers();
      TreasureSenseRenderer.register();
      ClientForgeEvents.register();
      EventHandlers.registerClient();
      MusicDiscEvents.registerClient();
      TooltipEvents.registerClient();
   }
}
