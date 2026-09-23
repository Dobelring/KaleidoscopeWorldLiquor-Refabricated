package com.bmt.kaleidoscope_world_liquor.compat.create.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;

/**
 * Create 联动的 Fabric 网络层（官方用 NeoForge 的 {@code PacketHandler}）。
 * <p>
 * 官方两件事：{@code registrar.playToClient(...)} 注册载荷、
 * {@code PacketDistributor.sendToPlayersTrackingEntity(entity, ...)} 发给追踪该装置的玩家。
 * Fabric 对应 {@code PayloadTypeRegistry.clientboundPlay()} 与 {@link PlayerLookup#tracking(Entity)}。
 */
public final class ContraptionNetwork {
    private ContraptionNetwork() {
    }

    /** 服务端/通用侧：注册载荷。由主入口点在装了 create 时调用。 */
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ContraptionChangedPacket.TYPE, ContraptionChangedPacket.STREAM_CODEC);
    }

    /** 服务端：把载荷发给所有追踪该实体的玩家。 */
    public static void sendToTracking(ContraptionChangedPacket packet, Entity entity) {
        for (var player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    /** 客户端侧接收注册。由客户端入口点在装了 create 时调用。 */
    @Environment(EnvType.CLIENT)
    public static final class Clientside {
        private Clientside() {
        }

        public static void register() {
            ClientPlayNetworking.registerGlobalReceiver(ContraptionChangedPacket.TYPE, ContraptionChangedPacket::onHandle);
        }
    }
}
