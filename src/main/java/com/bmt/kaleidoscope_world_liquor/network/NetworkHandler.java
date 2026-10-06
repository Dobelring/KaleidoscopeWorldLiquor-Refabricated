package com.bmt.kaleidoscope_world_liquor.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * 官方 Forge SimpleChannel（kaleidoscope_world_liquor:main）的 Fabric 等价物。
 * 方向仅服务端 → 客户端（宝藏感知）。
 * <p>
 * 26.x Fabric API：S2C 载荷类型用 {@code PayloadTypeRegistry.clientboundPlay()}
 * （1.21.1 上叫 {@code playS2C()}，26.x 已改名），类型注册在主入口；
 * 客户端接收在 {@link #registerClient()}（客户端入口调用）。
 */
public final class NetworkHandler {
    private NetworkHandler() {
    }

    /** 服务端/通用侧：注册 S2C 载荷类型（由主入口 onInitialize 调用一次）。 */
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TreasureSensePayload.TYPE, TreasureSensePayload.CODEC);
    }

    /** 服务端：发宝藏感知目标给单个玩家。 */
    public static void send(ServerPlayer player, TreasureSensePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(TreasureSensePayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> com.bmt.kaleidoscope_world_liquor.client.network.ClientPacketHandler.handleTreasureSense(payload)));
    }
}
