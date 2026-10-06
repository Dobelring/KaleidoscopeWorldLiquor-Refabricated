package com.bmt.kaleidoscope_world_liquor.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 宝藏感知目标包（官方 Forge SimpleChannel TreasureSensePayload 的 Fabric 等价物）。
 * 载荷：服务端扫描出的「有战利品表的容器坐标」+ 「有战利品表的容器矿车实体 id 列表」。方向仅 S2C。
 */
public record TreasureSensePayload(List<BlockPos> blockPositions, List<Integer> minecartIds)
        implements CustomPacketPayload {

    public static final Type<TreasureSensePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "treasure_sense"));

    public static final StreamCodec<FriendlyByteBuf, TreasureSensePayload> CODEC = new StreamCodec<>() {
        @Override
        public TreasureSensePayload decode(FriendlyByteBuf buf) {
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

            return new TreasureSensePayload(positions, ids);
        }

        @Override
        public void encode(FriendlyByteBuf buf, TreasureSensePayload payload) {
            buf.writeVarInt(payload.blockPositions.size());
            for (BlockPos pos : payload.blockPositions) {
                buf.writeBlockPos(pos);
            }

            buf.writeVarInt(payload.minecartIds.size());
            for (Integer id : payload.minecartIds) {
                buf.writeVarInt(id);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
