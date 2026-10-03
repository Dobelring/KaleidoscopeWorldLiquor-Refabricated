package com.bmt.kaleidoscope_world_liquor.block.entity;

import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WallRecordBlockEntity extends BlockEntity {
    private static final String RECORD_KEY = "Record";
    private ItemStack record = ItemStack.EMPTY;

    public WallRecordBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WALL_RECORD, pos, state);
    }

    public void refresh() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        this.record = ItemStack.of(tag.getCompound("Record"));
        // 原 Forge：onDataPacket 收到客户端更新包后（PacketFlow.CLIENTBOUND）调用 refreshChunk 标脏重绘。
        // 1.20.1 原版没有 onDataPacket 钩子——ClientPacketListener.handleBlockEntityData 对更新包直接调用
        // be.load(tag)，区块初次加载的 BE 数据同样走 load。这里把客户端标脏等价并入 load：
        // 更新包到达时 level 已设置、isClientSide 为 true 会执行；区块初次加载时 level 尚为 null 会跳过
        // （区块加载本身会构建网格，无需额外标脏），行为与原 Forge 版一致。
        if (this.level != null && this.level.isClientSide) {
            this.refreshChunk();
        }
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Record", this.record.save(new CompoundTag()));
    }

    public ItemStack getRecord() {
        return this.record;
    }

    public void setRecord(ItemStack stack) {
        this.record = stack.copyWithCount(1);
        this.refresh();
    }

    @Environment(EnvType.CLIENT)
    private void refreshChunk() {
        Minecraft.getInstance()
            .levelRenderer
            .setSectionDirty(
                SectionPos.blockToSectionCoord(this.worldPosition.getX()), SectionPos.blockToSectionCoord(this.worldPosition.getY()), SectionPos.blockToSectionCoord(this.worldPosition.getZ())
            );
    }
}
