package com.bmt.kaleidoscope_world_liquor.block.entity;

import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WallRecordBlockEntity extends BlockEntity {
   private static final String RECORD_KEY = "Record";
   private ItemStack record = ItemStack.EMPTY;

   public WallRecordBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.WALL_RECORD_BE, pos, state);
   }

   public void refresh() {
      this.setChanged();
      if (this.level != null) {
         BlockState state = this.level.getBlockState(this.worldPosition);
         this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
      }
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.record = ItemStack.parseOptional(registries, tag.getCompound("Record"));
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.put("Record", this.record.saveOptional(registries));
   }

   public ItemStack getRecord() {
      return this.record;
   }

   public void setRecord(ItemStack stack) {
      this.record = stack.copyWithCount(1);
      this.refresh();
   }

   private void refreshChunk() {
      Minecraft.getInstance()
         .levelRenderer
         .setSectionDirty(
            SectionPos.blockToSectionCoord(this.worldPosition.getX()),
            SectionPos.blockToSectionCoord(this.worldPosition.getY()),
            SectionPos.blockToSectionCoord(this.worldPosition.getZ())
         );
   }
}
