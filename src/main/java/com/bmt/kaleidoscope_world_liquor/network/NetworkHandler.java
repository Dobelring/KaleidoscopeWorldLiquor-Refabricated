package com.bmt.kaleidoscope_world_liquor.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * 官方 Forge SimpleChannel（kaleidoscope_world_liquor:main）的 Fabric 等价物。
 * 方向仅服务端 → 客户端（宝藏感知）。
 * <p>
 * S2C payload 类型在服务端启动入口（主类 onInitialize）注册；
 * 客户端接收在 {@code client/network/ClientPacketHandler#register}（客户端入口调用）。
 * 26.x fabric-api 用 {@code PayloadTypeRegistry.clientboundPlay()}（playS2C 的现名）。
 */
public final class NetworkHandler {
    private NetworkHandler() {
    }

    /** 服务端侧：注册 S2C payload 类型（只调用一次）。 */
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TreasureSensePayload.TYPE, TreasureSensePayload.CODEC);
    }

    public static void send(ServerPlayer player, TreasureSensePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
