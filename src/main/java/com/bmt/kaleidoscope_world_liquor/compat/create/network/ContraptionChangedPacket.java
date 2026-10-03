package com.bmt.kaleidoscope_world_liquor.compat.create.network;

import com.bmt.kaleidoscope_world_liquor.mixins.create.accessor.ContraptionClientAccessor;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ClientContraption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ContraptionChangedPacket implements FabricPacket {
    public static final PacketType<ContraptionChangedPacket> TYPE = PacketType.create(PacketHandler.CREATE_COMPAT, ContraptionChangedPacket::new);
    private final int entityId;
    private final BlockPos localPos;
    private final BlockState newState;
    private final CompoundTag newNbt;

    public ContraptionChangedPacket(int entityId, BlockPos localPos, BlockState newState, CompoundTag newNbt) {
        this.entityId = entityId;
        this.localPos = localPos;
        this.newState = newState;
        this.newNbt = newNbt;
    }

    public ContraptionChangedPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.localPos = buffer.readBlockPos();
        CompoundTag stateTag = buffer.readNbt();
        this.newState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), Objects.requireNonNull(stateTag, "Missing block state"));
        this.newNbt = buffer.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeBlockPos(this.localPos);
        buffer.writeNbt(NbtUtils.writeBlockState(this.newState));
        buffer.writeNbt(this.newNbt);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    // 原 Forge handle() 的 reception side 判断：包只在客户端被接收（注册见 PacketHandler.Clientside）
    @Environment(EnvType.CLIENT)
    public static void receive(ContraptionChangedPacket packet, LocalPlayer player, PacketSender sender) {
        handleClient(packet);
    }

    @Environment(EnvType.CLIENT)
    private static void handleClient(ContraptionChangedPacket packet) {
        if (Minecraft.getInstance().level != null) {
            if (Minecraft.getInstance().level.getEntity(packet.entityId) instanceof AbstractContraptionEntity contraptionEntity) {
                if (contraptionEntity.getContraption() != null) {
                    Contraption contraption = contraptionEntity.getContraption();
                    boolean isBlockRemoved = packet.newState.isAir();
                    if (isBlockRemoved) {
                        ContraptionInteractionUtil.removeBlockFromContraption(contraptionEntity, packet.localPos);
                    } else {
                        StructureBlockInfo newInfo = new StructureBlockInfo(packet.localPos, packet.newState, packet.newNbt);
                        ContraptionInteractionUtil.updateContraptionDataLocally(contraptionEntity, packet.localPos, newInfo);
                    }

                    updateClientRenderData(contraption, packet.localPos, packet.newState, packet.newNbt);
                    contraption.invalidateColliders();
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private static void updateClientRenderData(Contraption contraption, BlockPos localPos, BlockState newState, CompoundTag newNbt) {
        AtomicReference<ClientContraption> reference = ((ContraptionClientAccessor)contraption).getClientContraptionReference();
        ClientContraption clientContraption = reference.getAcquire();
        if (clientContraption == null) {
            ClientContraption created = ((ContraptionClientAccessor)contraption).invokeCreateClientContraption();
            ClientContraption canonical = reference.compareAndExchangeRelease(null, created);
            clientContraption = canonical == null ? created : canonical;
        }

        if (newState.isAir()) {
            clientContraption.getRenderLevel().removeBlockEntity(localPos);
        } else if (newNbt != null) {
            BlockEntity blockEntity = clientContraption.getBlockEntity(localPos);
            if (blockEntity != null) {
                blockEntity.load(newNbt.copy()); // 原 Forge handleUpdateTag（默认实现即 load）
            }
        }

        clientContraption.invalidateStructure();
        clientContraption.invalidateChildren();
    }
}
