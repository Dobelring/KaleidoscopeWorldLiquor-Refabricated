package com.bmt.kaleidoscope_world_liquor.network;

import com.bmt.kaleidoscope_world_liquor.client.network.ClientPacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 官方 Forge SimpleChannel（kaleidoscope_world_liquor:main）的 Fabric 等价物。
 * 方向仅服务端 → 客户端（宝藏感知），无客户端上行包。
 * 服务端发送走 ServerPlayNetworking；客户端接收在 {@link #registerClient()}（客户端入口调用）。
 */
public final class NetworkHandler {
    public static final ResourceLocation CHANNEL = new ResourceLocation("kaleidoscope_world_liquor", "treasure_sense");

    private NetworkHandler() {
    }

    public static void send(ServerPlayer player, TreasureSensePacket packet) {
        net.minecraft.network.FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            TreasureSensePacket packet = TreasureSensePacket.decode(buf);
            client.execute(() -> ClientPacketHandler.handleTreasureSense(packet));
        });
    }
}
