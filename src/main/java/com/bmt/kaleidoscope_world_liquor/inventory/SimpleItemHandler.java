package com.bmt.kaleidoscope_world_liquor.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal item handler replacing NeoForge ItemStackHandler for the Fabric port.
 */
public abstract class SimpleItemHandler {
   protected final NonNullList<ItemStack> stacks;

   protected SimpleItemHandler(int size) {
      this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
   }

   public int getSlots() {
      return this.stacks.size();
   }

   @NotNull
   public ItemStack getStackInSlot(int slot) {
      return this.stacks.get(slot);
   }

   public void setStackInSlot(int slot, @NotNull ItemStack stack) {
      this.stacks.set(slot, stack);
      this.onContentsChanged(slot);
   }

   public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      return true;
   }

   @NotNull
   public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      if (stack.isEmpty() || !this.isItemValid(slot, stack)) {
         return stack;
      }

      ItemStack current = this.stacks.get(slot);
      if (current.isEmpty()) {
         if (!simulate) {
            this.stacks.set(slot, stack.copy());
            this.onContentsChanged(slot);
         }

         return ItemStack.EMPTY;
      }

      return stack;
   }

   @NotNull
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (amount <= 0 || slot < 0 || slot >= this.stacks.size()) {
         return ItemStack.EMPTY;
      }

      ItemStack current = this.stacks.get(slot);
      if (current.isEmpty()) {
         return ItemStack.EMPTY;
      }

      int extracted = Math.min(amount, current.getCount());
      ItemStack result = current.copy();
      result.setCount(extracted);
      if (!simulate) {
         current.shrink(extracted);
         if (current.isEmpty()) {
            this.stacks.set(slot, ItemStack.EMPTY);
         }

         this.onContentsChanged(slot);
      }

      return result;
   }

   public int getSlotLimit(int slot) {
      return 64;
   }

   public boolean isEmpty() {
      return this.stacks.stream().allMatch(ItemStack::isEmpty);
   }

   protected abstract void onContentsChanged(int slot);
}
