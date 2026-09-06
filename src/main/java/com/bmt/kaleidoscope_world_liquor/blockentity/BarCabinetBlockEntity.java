package com.bmt.kaleidoscope_world_liquor.blockentity;

import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BarCabinetBlockEntity extends BaseBlockEntity {
   private ItemStack leftItem = ItemStack.EMPTY;
   private ItemStack rightItem = ItemStack.EMPTY;
   private boolean isSingle = false;

   public BarCabinetBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.BAR_CABINET_BE, pos, state);
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.leftItem = tag.contains("left_item") ? ItemStack.parseOptional(registries, tag.getCompound("left_item")) : ItemStack.EMPTY;
      this.rightItem = tag.contains("right_item") ? ItemStack.parseOptional(registries, tag.getCompound("right_item")) : ItemStack.EMPTY;
      this.isSingle = tag.getBoolean("is_single");
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      if (!this.leftItem.isEmpty()) {
         tag.put("left_item", this.leftItem.save(registries, new CompoundTag()));
      }

      if (!this.rightItem.isEmpty()) {
         tag.put("right_item", this.rightItem.save(registries, new CompoundTag()));
      }

      tag.putBoolean("is_single", this.isSingle);
   }

   public ItemStack getLeftItem() {
      return this.leftItem;
   }

   public void setLeftItem(ItemStack leftItem) {
      this.leftItem = leftItem;
   }

   public ItemStack getRightItem() {
      return this.rightItem;
   }

   public void setRightItem(ItemStack rightItem) {
      this.rightItem = rightItem;
   }

   public void setSingle(boolean single) {
      this.isSingle = single;
   }

   public boolean isSingle() {
      return this.isSingle;
   }
}
