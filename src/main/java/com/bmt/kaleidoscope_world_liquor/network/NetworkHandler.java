package com.bmt.kaleidoscope_world_liquor.network;

import com.bmt.kaleidoscope_world_liquor.client.network.ClientPacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * 官方 Forge SimpleChannel（kaleidoscope_world_liquor:main）的 Fabric 等价物。
 * 方向仅服务端 → 客户端（宝藏感知）。S2C 载荷类型在服务端入口注册，
 * 客户端接收在 {@link #registerClient()}（客户端入口调用）。
 */
public final class NetworkHandler {
    private NetworkHandler() {
    }

    /** 服务端侧：注册 S2C 载荷类型（由主入口点调用一次）。 */
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TreasureSensePayload.TYPE, TreasureSensePayload.CODEC);
    }

    public static void send(ServerPlayer player, TreasureSensePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(TreasureSensePayload.TYPE,
                (payload, context) -> context.client().execute(() -> ClientPacketHandler.handleTreasureSense(payload)));
    }
}
