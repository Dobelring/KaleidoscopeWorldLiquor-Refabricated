package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class ClientForgeEvents {
   private ClientForgeEvents() {
   }

   public static void register() {
      // 官方 1.1.11：宝藏感知改服务端下发——注册客户端 payload 接收
      com.bmt.kaleidoscope_world_liquor.network.NetworkHandler.registerClient();
      WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            if (mc.player.hasEffect(ModEffects.TREASURE_SENSE_EFFECT)) {
               TreasureSenseRenderer.renderTreasures(context.matrixStack());
            }
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TreasureSenseRenderer.freeBuffer());
   }
}
