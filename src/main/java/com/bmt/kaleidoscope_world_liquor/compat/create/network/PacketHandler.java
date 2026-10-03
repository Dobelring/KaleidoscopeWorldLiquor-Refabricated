package com.bmt.kaleidoscope_world_liquor.compat.create.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class PacketHandler {
    // 原 Forge SimpleChannel 频道 id：kaleidoscope_world_liquor:create_compat；
    // Fabric 上频道 id 由 PacketType 持有（ContraptionChangedPacket.TYPE）
    public static final ResourceLocation CREATE_COMPAT = new ResourceLocation("kaleidoscope_world_liquor", "create_compat");

    private PacketHandler() {
    }

    public static void register() {
        // 原 Forge 在此 registerMessage(PLAY_TO_CLIENT)；Fabric 单向 S2C 包只需注册客户端接收端。
        // 接收端是客户端专属逻辑：物理客户端自注册，专用服务器跳过（Clientside 带 @Environment(CLIENT)，
        // 服务端不会加载该嵌套类），因此客户端入口无需再接线。
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Clientside.init();
        }
    }

    public static void sendToTracking(Object packet, ServerPlayer player) {
        if (packet instanceof ContraptionChangedPacket changed && ServerPlayNetworking.canSend(player, ContraptionChangedPacket.TYPE)) {
            ServerPlayNetworking.send(player, changed);
        }
    }

    public static void sendToTracking(Object packet, Entity entity) {
        if (packet instanceof ContraptionChangedPacket changed) {
            for (ServerPlayer tracking : PlayerLookup.tracking(entity)) {
                if (ServerPlayNetworking.canSend(tracking, ContraptionChangedPacket.TYPE)) {
                    ServerPlayNetworking.send(tracking, changed);
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Clientside {
        public static void init() {
            ClientPlayNetworking.registerGlobalReceiver(ContraptionChangedPacket.TYPE, ContraptionChangedPacket::receive);
        }
    }
}
