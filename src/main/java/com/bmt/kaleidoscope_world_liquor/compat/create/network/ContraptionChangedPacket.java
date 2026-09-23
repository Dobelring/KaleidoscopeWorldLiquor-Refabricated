package com.bmt.kaleidoscope_world_liquor.compat.create.network;

import com.bmt.kaleidoscope_world_liquor.compat.create.ContraptionDataSync;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 服务端 → 客户端：装置上某个方块的"状态 + 方块实体数据"变了（官方同名包的移植）。
 * <p>
 * 载荷本身是原版 {@code CustomPacketPayload}/{@code StreamCodec}，与官方一致；
 * 差别只在注册与收发走 Fabric 的 {@link ContraptionNetwork}。
 * <p>
 * 客户端刷新渲染数据这一块：官方用 mixin accessor 拿 {@code Contraption.clientContraption}
 * 再调它的实例方法；create-fly 把该字段擦成了 {@code AtomicReference<?>}，但提供了三个静态
 * 便捷方法（{@code getBlockEntityClientSide} / {@code invalidateClientContraptionStructure} /
 * {@code invalidateClientContraptionChildren}），这里改用静态方法，省掉一个 accessor。
 */
public class ContraptionChangedPacket implements CustomPacketPayload {
    public static final Type<ContraptionChangedPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "contraption_changed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionChangedPacket> STREAM_CODEC =
            StreamCodec.of((buffer, packet) -> encode(packet, buffer), ContraptionChangedPacket::decode);

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

    public static void encode(ContraptionChangedPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeBlockPos(packet.localPos);
        buffer.writeNbt(NbtUtils.writeBlockState(packet.newState));
        buffer.writeNbt(packet.newNbt);
    }

    public static ContraptionChangedPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        BlockPos localPos = buffer.readBlockPos();
        CompoundTag stateTag = buffer.readNbt();
        BlockState newState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK,
                Objects.requireNonNull(stateTag, "Missing block state"));
        CompoundTag newNbt = buffer.readNbt();
        return new ContraptionChangedPacket(entityId, localPos, newState, newNbt);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unused")
    @Environment(EnvType.CLIENT)
    public static void onHandle(ContraptionChangedPacket packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> handleClient(packet));
    }

    @Environment(EnvType.CLIENT)
    private static void handleClient(ContraptionChangedPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (!(minecraft.level.getEntity(packet.entityId) instanceof AbstractContraptionEntity contraptionEntity)) {
            return;
        }
        if (contraptionEntity.getContraption() == null) {
            return;
        }
        Contraption contraption = contraptionEntity.getContraption();
        if (packet.newState.isAir()) {
            ContraptionDataSync.removeBlockFromContraption(contraptionEntity, packet.localPos);
        } else {
            StructureBlockInfo newInfo = new StructureBlockInfo(packet.localPos, packet.newState, packet.newNbt);
            ContraptionDataSync.updateContraptionDataLocally(contraptionEntity, packet.localPos, newInfo);
        }
        updateClientRenderData(contraption, packet.localPos, packet.newState, packet.newNbt);
        contraption.invalidateColliders();
    }

    @Environment(EnvType.CLIENT)
    private static void updateClientRenderData(Contraption contraption, BlockPos localPos, BlockState newState, CompoundTag newNbt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (!newState.isAir() && newNbt != null) {
            BlockEntity blockEntity = ClientContraption.getBlockEntityClientSide(contraption, localPos);
            if (blockEntity != null) {
                blockEntity.loadWithComponents(TagValueInput.create(
                        ProblemReporter.DISCARDING, minecraft.level.registryAccess(), newNbt.copy()));
            }
        }
        ClientContraption.invalidateClientContraptionStructure(contraption);
        ClientContraption.invalidateClientContraptionChildren(contraption);
    }
}
