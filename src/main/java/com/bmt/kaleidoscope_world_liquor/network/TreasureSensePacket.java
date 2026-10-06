package com.bmt.kaleidoscope_world_liquor.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 宝藏感知目标包（官方 Forge SimpleChannel 的 TreasureSensePacket 等价物）。
 * 载荷：服务端扫描出的有战利品表的容器坐标 + 容器矿车实体 id 列表。
 */
public record TreasureSensePacket(List<BlockPos> blockPositions, List<Integer> minecartIds) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.blockPositions.size());

        for (BlockPos pos : this.blockPositions) {
            buf.writeBlockPos(pos);
        }

        buf.writeVarInt(this.minecartIds.size());

        for (Integer id : this.minecartIds) {
            buf.writeVarInt(id);
        }
    }

    public static TreasureSensePacket decode(FriendlyByteBuf buf) {
        int blockCount = buf.readVarInt();
        List<BlockPos> positions = new ArrayList<>(blockCount);

        for (int i = 0; i < blockCount; i++) {
            positions.add(buf.readBlockPos());
        }

        int minecartCount = buf.readVarInt();
        List<Integer> ids = new ArrayList<>(minecartCount);

        for (int i = 0; i < minecartCount; i++) {
            ids.add(buf.readVarInt());
        }

        return new TreasureSensePacket(positions, ids);
    }
}
