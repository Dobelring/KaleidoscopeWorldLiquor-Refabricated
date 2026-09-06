package com.bmt.kaleidoscope_world_liquor.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBlockEntity extends BlockEntity {
   public BaseBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
      super(entityType, pos, state);
   }

   public void refresh() {
      this.setChanged();
      if (this.level != null) {
         BlockState state = this.level.getBlockState(this.worldPosition);
         this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
      }
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
