package com.bmt.kaleidoscope_world_liquor.compat.transfer;

import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 冰柜的漏斗/管道自动化支持，对应原版 NeoForge getCapability 暴露的三类槽：
 * 流体槽（WORKING 时关闭）、物品输入槽（非 DOWN 面；WORKING/有产出时关闭）、产物输出槽（DOWN 面；条件产物关闭）。
 */
public final class FreezerTransfer {
   private FreezerTransfer() {
   }

   public static void register() {
      FluidStorage.SIDED.registerForBlockEntity(FreezerTransfer::getFluidStorage, ModBlockEntities.FREEZER_BE);
      ItemStorage.SIDED.registerForBlockEntity(FreezerTransfer::getItemStorage, ModBlockEntities.FREEZER_BE);
   }

   @Nullable
   private static Storage<FluidVariant> getFluidStorage(FreezerBlockEntity be, Direction side) {
      return be.isWorking() ? null : new FreezerFluidStorage(be);
   }

   @Nullable
   private static Storage<ItemVariant> getItemStorage(FreezerBlockEntity be, Direction side) {
      if (be.isWorking()) {
         return null;
      }

      if (side == Direction.DOWN) {
         return be.hasOutput() ? new FreezerOutputStorage(be) : null;
      }

      return be.hasOutput() ? null : new FreezerInputStorage(be);
   }

   /** 流体槽：直接读写 BE 的 tank（fill/drain 自带 simulate 语义，漏斗交互不依赖事务回滚）。 */
   private static final class FreezerFluidStorage implements Storage<FluidVariant> {
      private final FreezerBlockEntity be;

      private FreezerFluidStorage(FreezerBlockEntity be) {
         this.be = be;
      }

      @Override
      public boolean supportsInsertion() {
         return !this.be.isWorking();
      }

      @Override
      public boolean supportsExtraction() {
         return !this.be.isWorking();
      }

      @Override
      public long insert(FluidVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
         if (!this.supportsInsertion() || maxAmount <= 0 || resource.isBlank()) {
            return 0;
         }

         // 漏斗一次只处理一桶（1000mB），直接限流到剩余容量
         int free = this.be.tank.getCapacity() - this.be.tank.getFluidAmount();
         long amount = Math.min(maxAmount, free);
         if (amount <= 0) {
            return 0;
         }

         if (!this.be.tank.isEmpty() && !this.be.tank.getFluid().getFluid().isSame(resource.getFluid())) {
            return 0;
         }

         transaction.addCloseCallback((ctx, result) -> {
            if (result.wasCommitted()) {
               this.be.tank.fill(new FluidStack(resource.getFluid(), (int) amount), false);
            }
         });

         return amount;
      }

      @Override
      public long extract(FluidVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
         if (!this.supportsExtraction() || maxAmount <= 0 || this.be.tank.isEmpty()) {
            return 0;
         }

         if (resource != null && !resource.isBlank() && !this.be.tank.getFluid().getFluid().isSame(resource.getFluid())) {
            return 0;
         }

         long amount = Math.min(maxAmount, this.be.tank.getFluidAmount());
         if (amount <= 0) {
            return 0;
         }

         transaction.addCloseCallback((ctx, result) -> {
            if (result.wasCommitted()) {
               this.be.tank.drain((int) amount, false);
            }
         });

         return amount;
      }

      @Override
      public Iterator<StorageView<FluidVariant>> iterator() {
         boolean empty = this.be.tank.isEmpty();
         FluidVariant variant = empty ? FluidVariant.blank() : FluidVariant.of(this.be.tank.getFluid().getFluid());
         long amount = empty ? 0 : this.be.tank.getFluidAmount();
         long capacity = this.be.tank.getCapacity();

         List<StorageView<FluidVariant>> views = List.of(new StorageView<>() {
            @Override
            public long extract(FluidVariant res, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext tx) {
               return FreezerFluidStorage.this.extract(res, maxAmount, tx);
            }

            @Override
            public boolean isResourceBlank() {
               return empty;
            }

            @Override
            public FluidVariant getResource() {
               return variant;
            }

            @Override
            public long getAmount() {
               return amount;
            }

            @Override
            public long getCapacity() {
               return capacity;
            }
         });

         return views.iterator();
      }
   }

   /** 输入槽：4 槽合并；insert 先堆叠同种物品槽（每格一组，对齐原版 ItemStackHandler），再落空槽；extract 从最后放入的槽（3→0）取。 */
   private static final class FreezerInputStorage implements Storage<ItemVariant> {
      private final FreezerBlockEntity be;

      private FreezerInputStorage(FreezerBlockEntity be) {
         this.be = be;
      }

      @Override
      public boolean supportsInsertion() {
         return !this.be.isWorking() && !this.be.hasOutput();
      }

      @Override
      public boolean supportsExtraction() {
         return !this.be.isWorking();
      }

      @Override
      public long insert(ItemVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
         if (!this.supportsInsertion() || maxAmount <= 0 || resource.isBlank()) {
            return 0;
         }

         long remaining = maxAmount;
         long total = 0;

         for (int i = 0; i < 4 && remaining > 0; i++) {
            ItemStack current = this.be.inputInventory.getStackInSlot(i);
            if (!current.isEmpty() && resource.matches(current) && current.getCount() < current.getMaxStackSize()) {
               final int slot = i;
               final long count = Math.min(remaining, current.getMaxStackSize() - current.getCount());
               transaction.addCloseCallback((ctx, result) -> {
                  if (result.wasCommitted()) {
                     ItemStack cur = this.be.inputInventory.getStackInSlot(slot).copy();
                     cur.grow((int) count);
                     this.be.inputInventory.setStackInSlot(slot, cur);
                  }
               });

               remaining -= count;
               total += count;
            }
         }

         for (int i = 0; i < 4 && remaining > 0; i++) {
            ItemStack current = this.be.inputInventory.getStackInSlot(i);
            if (current.isEmpty()) {
               final int slot = i;
               final long count = Math.min(remaining, Math.min(resource.toStack().getMaxStackSize(), this.be.inputInventory.getSlotLimit(slot)));
               transaction.addCloseCallback((ctx, result) -> {
                  if (result.wasCommitted()) {
                     this.be.inputInventory.setStackInSlot(slot, resource.toStack((int) count));
                  }
               });

               remaining -= count;
               total += count;
            }
         }

         return total;
      }

      @Override
      public long extract(ItemVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
         if (!this.supportsExtraction() || maxAmount <= 0) {
            return 0;
         }

         for (int i = 3; i >= 0; i--) {
            ItemStack current = this.be.inputInventory.getStackInSlot(i);
            if (!current.isEmpty() && (resource == null || resource.isBlank() || resource.matches(current))) {
               final int slot = i;
               final long count = Math.min(maxAmount, current.getCount());
               transaction.addCloseCallback((ctx, result) -> {
                  if (result.wasCommitted()) {
                     this.be.inputInventory.extractItem(slot, (int) count, false);
                  }
               });

               return count;
            }
         }

         return 0;
      }

      @Override
      public Iterator<StorageView<ItemVariant>> iterator() {
         List<StorageView<ItemVariant>> views = new ArrayList<>();
         for (int i = 0; i < 4; i++) {
            views.add(new SlotView(this.be, i));
         }

         return views.iterator();
      }
   }

   /** 单槽只读视图，extract 委托给所属 Storage 的槽位逻辑。 */
   private record SlotView(FreezerBlockEntity be, int slot) implements StorageView<ItemVariant> {
      @Override
      public long extract(ItemVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext tx) {
         ItemStack current = this.be.inputInventory.getStackInSlot(this.slot);
         if (current.isEmpty() || maxAmount <= 0 || (!resource.isBlank() && !resource.matches(current))) {
            return 0;
         }

         long count = Math.min(maxAmount, current.getCount());
         tx.addCloseCallback((ctx, result) -> {
            if (result.wasCommitted()) {
               this.be.inputInventory.extractItem(this.slot, (int) count, false);
            }
         });

         return count;
      }

      @Override
      public boolean isResourceBlank() {
         return this.be.inputInventory.getStackInSlot(this.slot).isEmpty();
      }

      @Override
      public ItemVariant getResource() {
         return ItemVariant.of(this.be.inputInventory.getStackInSlot(this.slot));
      }

      @Override
      public long getAmount() {
         ItemStack stack = this.be.inputInventory.getStackInSlot(this.slot);
         return stack.isEmpty() ? 0 : stack.getCount();
      }

      @Override
      public long getCapacity() {
         ItemStack stack = this.be.inputInventory.getStackInSlot(this.slot);
         return stack.isEmpty() ? 64 : stack.getMaxStackSize();
      }
   }

   /** 产物输出槽（DOWN 面）：只支持取出，条件产物（波奇布丁）不允许自动化取出。 */
   private static final class FreezerOutputStorage implements Storage<ItemVariant> {
      private final FreezerBlockEntity be;

      private FreezerOutputStorage(FreezerBlockEntity be) {
         this.be = be;
      }

      @Override
      public boolean supportsInsertion() {
         return false;
      }

      @Override
      public boolean supportsExtraction() {
         return this.be.hasOutput() && this.be.getCurrentRecipeHolder() != null
            && this.be.getCurrentRecipeHolder().value().getExtractCondition().isEmpty();
      }

      @Override
      public long insert(ItemVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
         return 0;
      }

      @Override
      public long extract(ItemVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
         if (!this.supportsExtraction() || maxAmount <= 0) {
            return 0;
         }

         ItemStack prototype = this.be.getCurrentRecipeHolder().value().getResultItem(this.be.getLevel().registryAccess()).copy();
         if (prototype.isEmpty() || (!resource.isBlank() && !resource.matches(prototype))) {
            return 0;
         }

         long count = Math.min(maxAmount, this.be.getOutputCount());
         if (count <= 0) {
            return 0;
         }

         transaction.addCloseCallback((ctx, result) -> {
            if (result.wasCommitted()) {
               this.be.consumeOutput((int) count);
            }
         });

         return count;
      }

      @Override
      public Iterator<StorageView<ItemVariant>> iterator() {
         List<StorageView<ItemVariant>> views = new ArrayList<>();
         views.add(new StorageView<>() {
            @Override
            public long extract(ItemVariant resource, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext tx) {
               return FreezerOutputStorage.this.extract(resource, maxAmount, tx);
            }

            @Override
            public boolean isResourceBlank() {
               return !FreezerOutputStorage.this.be.hasOutput();
            }

            @Override
            public ItemVariant getResource() {
               ItemStack prototype = FreezerOutputStorage.this.be.getCurrentRecipeHolder() != null
                  ? FreezerOutputStorage.this.be.getCurrentRecipeHolder().value().getResultItem(FreezerOutputStorage.this.be.getLevel().registryAccess())
                  : ItemStack.EMPTY;
               return ItemVariant.of(prototype);
            }

            @Override
            public long getAmount() {
               return FreezerOutputStorage.this.be.hasOutput() ? FreezerOutputStorage.this.be.getOutputCount() : 0;
            }

            @Override
            public long getCapacity() {
               return 64;
            }
         });

         return views.iterator();
      }
   }
}
