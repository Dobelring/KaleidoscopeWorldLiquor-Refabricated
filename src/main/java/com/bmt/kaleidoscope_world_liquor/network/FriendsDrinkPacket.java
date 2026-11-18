package com.bmt.kaleidoscope_world_liquor.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 官方 1.2.0：干杯之歌效果同步包——带 smc:ohh_ye_ye 效果的非玩家实体 id + 效果剩余时长。
 * 方向仅 S2C（1.20.1 旧版 play channel 风格，与 TreasureSensePacket 同构）。
 */
public record FriendsDrinkPacket(int[] entityIds, int[] durations) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityIds.length);
        for (int i = 0; i < this.entityIds.length; i++) {
            buf.writeVarInt(this.entityIds[i]);
            buf.writeVarInt(this.durations[i]);
        }
    }

    public static FriendsDrinkPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        int[] ids = new int[count];
        int[] durations = new int[count];
        for (int i = 0; i < count; i++) {
            ids[i] = buf.readVarInt();
            durations[i] = buf.readVarInt();
        }
        return new FriendsDrinkPacket(ids, durations);
    }
}
