package com.bmt.kaleidoscope_world_liquor.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 官方 1.2.0：干杯之歌效果同步包——带 smc:ohh_ye_ye 效果的非玩家实体 id + 效果剩余时长。
 * 方向仅 S2C。
 */
public record FriendsDrinkPayload(int[] entityIds, int[] durations) implements CustomPacketPayload {
    public static final Type<FriendsDrinkPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "friends_drink"));

    public static final StreamCodec<FriendlyByteBuf, FriendsDrinkPayload> CODEC =
            new StreamCodec<FriendlyByteBuf, FriendsDrinkPayload>() {
                @Override
                public FriendsDrinkPayload decode(FriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    int[] ids = new int[count];
                    int[] durations = new int[count];
                    for (int i = 0; i < count; i++) {
                        ids[i] = buf.readVarInt();
                        durations[i] = buf.readVarInt();
                    }
                    return new FriendsDrinkPayload(ids, durations);
                }

                @Override
                public void encode(FriendlyByteBuf buf, FriendsDrinkPayload payload) {
                    buf.writeVarInt(payload.entityIds.length);
                    for (int i = 0; i < payload.entityIds.length; i++) {
                        buf.writeVarInt(payload.entityIds[i]);
                        buf.writeVarInt(payload.durations[i]);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}