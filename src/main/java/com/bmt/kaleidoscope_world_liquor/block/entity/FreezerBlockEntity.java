package com.bmt.kaleidoscope_world_liquor.block.entity;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidTank;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.bmt.kaleidoscope_world_liquor.inventory.SimpleItemHandler;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FreezerBlockEntity extends BlockEntity {
   private RecipeHolder<FreezerRecipe> currentRecipeHolder = null;
   private RecipeHolder<FreezerRecipe> cachedRecipeHolder = null;
   public final FluidTank tank = new FluidTank(1000) {
      @Override
      protected void onContentsChanged() {
         FreezerBlockEntity.this.setChanged();
         if (FreezerBlockEntity.this.level != null && !FreezerBlockEntity.this.level.isClientSide) {
            FreezerBlockEntity.this.level
               .sendBlockUpdated(FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState(), FreezerBlockEntity.this.getBlockState(), 3);
            FreezerBlockEntity.this.checkForMatchingRecipe();
         }
      }
   };
   public final SimpleItemHandler inputInventory = new SimpleItemHandler(4) {
      @Override
      protected void onContentsChanged(int slot) {
         FreezerBlockEntity.this.setChanged();
         if (FreezerBlockEntity.this.level != null && !FreezerBlockEntity.this.level.isClientSide) {
            FreezerBlockEntity.this.level
               .sendBlockUpdated(FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState(), FreezerBlockEntity.this.getBlockState(), 3);
            FreezerBlockEntity.this.checkForMatchingRecipe();
         }
      }

      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
         return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() ? super.isItemValid(slot, stack) : false;
      }

      @NotNull
      @Override
      public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
         return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() ? super.insertItem(slot, stack, simulate) : stack;
      }
   };
   public final SimpleItemHandler outputInventory = new SimpleItemHandler(1) {
      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
         return false;
      }

      @NotNull
      @Override
      public ItemStack getStackInSlot(int slot) {
         if (slot != 0) {
            return ItemStack.EMPTY;
         } else if (FreezerBlockEntity.this.hasOutput()
            && FreezerBlockEntity.this.currentRecipeHolder != null
            && FreezerBlockEntity.this.currentRecipeHolder.value().getExtractCondition().isEmpty()) {
            ItemStack result = FreezerBlockEntity.this.currentRecipeHolder.value()
               .getResultItem(FreezerBlockEntity.this.level.registryAccess())
               .copy();
            result.setCount(1);
            return result;
         } else {
            return ItemStack.EMPTY;
         }
      }

      @NotNull
      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         if (slot != 0 || amount <= 0) {
            return ItemStack.EMPTY;
         } else if (FreezerBlockEntity.this.hasOutput()
            && FreezerBlockEntity.this.currentRecipeHolder != null
            && FreezerBlockEntity.this.currentRecipeHolder.value().getExtractCondition().isEmpty()) {
            int extractCount = Math.min(amount, FreezerBlockEntity.this.outputCount);
            if (extractCount <= 0) {
               return ItemStack.EMPTY;
            } else {
               ItemStack result = FreezerBlockEntity.this.currentRecipeHolder.value()
                  .getResultItem(FreezerBlockEntity.this.level.registryAccess())
                  .copy();
               result.setCount(extractCount);
               if (!simulate) {
                  FreezerBlockEntity.this.outputCount -= extractCount;
                  FreezerBlockEntity.this.setChanged();
                  FreezerBlockEntity.this.level
                     .sendBlockUpdated(
                        FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState(), FreezerBlockEntity.this.getBlockState(), 3
                     );
                  FreezerBlockEntity.this.level.updateNeighborsAt(FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState().getBlock());
                  FreezerBlockEntity.this.checkForMatchingRecipe();
               }

               return result;
            }
         } else {
            return ItemStack.EMPTY;
         }
      }

      @Override
      public int getSlotLimit(int slot) {
         return 1;
      }

      @Override
      protected void onContentsChanged(int slot) {
      }
   };
   private int progress = 0;
   private int maxProgress = 0;
   private int outputCount = 0;
   private ResourceLocation outputTexture = null;
   private boolean redstonePowered = false;
   private ResourceLocation pendingRecipeId = null;
   private boolean needsRecipeRestore = false;

   public FreezerBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.FREEZER_BE, pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, FreezerBlockEntity be) {
      if (!level.isClientSide && be.needsRecipeRestore && be.pendingRecipeId != null) {
         be.restoreRecipe();
         be.needsRecipeRestore = false;
      }

      if (!level.isClientSide) {
         if (state.getValue(FreezerBlock.WORKING)) {
            be.progress++;
            if (be.progress >= be.maxProgress) {
               be.finishCrafting(state, level, pos);
            }

            if (be.progress % 20 == 0) {
               setChanged(level, pos, state);
               level.sendBlockUpdated(pos, state, state, 3);
            }
         }
      }
   }

   private void checkForMatchingRecipe() {
      if (this.level != null && !this.level.isClientSide) {
         if (!this.isWorking() && !this.hasOutput()) {
            FluidStack currentFluid = this.tank.getFluid();
            NonNullList<ItemStack> items = this.inventoryToNonNullList();
            List<RecipeHolder<FreezerRecipe>> allRecipes = this.level.getRecipeManager().getAllRecipesFor(ModRecipes.FREEZER_TYPE);
            RecipeHolder<FreezerRecipe> matchedRecipeHolder = null;

            for (RecipeHolder<FreezerRecipe> holder : allRecipes) {
               if (holder.value().matches(currentFluid, items, this.level)) {
                  matchedRecipeHolder = holder;
                  break;
               }
            }

            RecipeHolder<FreezerRecipe> oldCachedRecipeHolder = this.cachedRecipeHolder;
            this.cachedRecipeHolder = matchedRecipeHolder;
            if (this.cachedRecipeHolder != null && oldCachedRecipeHolder == null && this.canAutoStart()) {
               this.autoStartCrafting();
            }
         }
      }
   }

   private NonNullList<ItemStack> inventoryToNonNullList() {
      NonNullList<ItemStack> list = NonNullList.withSize(4, ItemStack.EMPTY);

      for (int i = 0; i < 4; i++) {
         list.set(i, this.inputInventory.getStackInSlot(i));
      }

      return list;
   }

   private boolean canAutoStart() {
      BlockState state = this.getBlockState();
      return !this.isWorking() && !state.getValue(FreezerBlock.OPEN) && this.outputCount == 0;
   }

   private void autoStartCrafting() {
      if (this.tryStartCrafting()) {
         BlockState state = this.getBlockState();
         this.level.setBlock(this.worldPosition, state.setValue(FreezerBlock.WORKING, true), 3);
      }
   }

   private void restoreRecipe() {
      if (this.level != null && this.pendingRecipeId != null) {
         this.currentRecipeHolder = this.level
            .getRecipeManager()
            .getAllRecipesFor(ModRecipes.FREEZER_TYPE)
            .stream()
            .filter(holder -> holder.id().equals(this.pendingRecipeId))
            .findFirst()
            .orElse(null);
         if (this.currentRecipeHolder == null) {
            this.progress = 0;
            this.maxProgress = 0;
            BlockState state = this.getBlockState();
            if (this.isWorking()) {
               this.level.setBlock(this.worldPosition, state.setValue(FreezerBlock.WORKING, false), 3);
            }
         }

         this.checkForMatchingRecipe();
      }
   }

   public boolean tryStartCrafting() {
      if (this.level != null && this.cachedRecipeHolder != null) {
         FreezerRecipe recipe = this.cachedRecipeHolder.value();
         ResourceLocation recipeId = this.cachedRecipeHolder.id();
         this.maxProgress = recipe.getCraftTime();
         this.progress = 0;
         this.currentRecipeHolder = this.cachedRecipeHolder;
         this.pendingRecipeId = recipeId;
         this.tank.drain(recipe.getInputFluid().getAmount(), false);

         for (int i = 0; i < 4; i++) {
            this.inputInventory.setStackInSlot(i, ItemStack.EMPTY);
         }

         this.setChanged();
         return true;
      } else {
         return false;
      }
   }

   private void finishCrafting(BlockState state, Level level, BlockPos pos) {
      if (this.currentRecipeHolder != null) {
         FreezerRecipe recipe = this.currentRecipeHolder.value();
         this.outputCount = recipe.getResultItem(level.registryAccess()).getCount();
         this.outputTexture = recipe.getResultTexture();
      }

      level.setBlock(pos, state.setValue(FreezerBlock.WORKING, false), 3);
      this.setChanged();
      level.sendBlockUpdated(pos, state, this.getBlockState(), 3);
      level.updateNeighborsAt(pos, state.getBlock());
      this.checkForMatchingRecipe();
   }

   public void insertItem(ItemStack stack, Player player) {
      this.insertItem(stack, player, InteractionHand.MAIN_HAND);
   }

   public boolean insertItem(ItemStack stack, Player player, InteractionHand hand) {
      if (!this.hasOutput() && !this.isWorking()) {
         for (int i = 0; i < 4; i++) {
            if (this.inputInventory.getStackInSlot(i).isEmpty()) {
               ItemStack copy = stack.copy();
               copy.setCount(1);
               this.inputInventory.setStackInSlot(i, copy);
               stack.shrink(1);
               player.setItemInHand(hand, stack);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void extractItem(Player player) {
      for (int i = 3; i >= 0; i--) {
         if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
            ItemStack extracted = this.inputInventory.extractItem(i, 1, false);
            if (!extracted.isEmpty()) {
               player.addItem(extracted);
               player.containerMenu.broadcastChanges();
            }

            return;
         }
      }
   }

   public boolean hasOutput() {
      return this.outputCount > 0;
   }

   public RecipeHolder<FreezerRecipe> getCurrentRecipeHolder() {
      return this.currentRecipeHolder;
   }

   public int getComparatorSignal() {
      int signal = 0;
      if (!this.tank.isEmpty()) {
         signal += 2;
      }

      for (int i = 0; i < 4; i++) {
         if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
            signal++;
         }
      }

      if (this.outputCount > 0) {
         signal += this.outputCount;
      } else if (this.isWorking() && this.currentRecipeHolder != null && this.level != null) {
         signal += this.currentRecipeHolder.value().getResultItem(this.level.registryAccess()).getCount();
      }

      return Math.min(15, signal);
   }

   public boolean isRedstonePowered() {
      return this.redstonePowered;
   }

   public void setRedstonePowered(boolean redstonePowered) {
      this.redstonePowered = redstonePowered;
   }

   @Override
   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
         this.syncToClients();
      }
   }

   /**
    * 向正在追踪此方块实体的玩家广播完整 BE 数据（库存、流体），
    * 使客户端渲染器即时反映最新内容。
    */
   private void syncToClients() {
      if (this.level instanceof ServerLevel serverLevel) {
         ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(this);
         for (Player player : PlayerLookup.tracking(serverLevel, this.worldPosition)) {
            if (player instanceof ServerPlayer serverPlayer) {
               serverPlayer.connection.send(packet);
            }
         }
      }
   }

   public boolean hasMatchingRecipe() {
      return this.cachedRecipeHolder != null;
   }

   public boolean isWorking() {
      return this.getBlockState().getValue(FreezerBlock.WORKING);
   }

   public void extractOutput(Player player, InteractionHand hand) {
      if (this.outputCount > 0 && this.currentRecipeHolder != null) {
         FreezerRecipe recipe = this.currentRecipeHolder.value();
         ItemStack held = player.getItemInHand(hand);
         if (!recipe.canExtract(held)) {
            Ingredient extractCondition = recipe.getExtractCondition();
            ItemStack[] displayItems = extractCondition.getItems();
            Component displayName = displayItems.length > 0 ? displayItems[0].getHoverName() : Component.literal("?");
            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.need_item", displayName), true);
            return;
         }

         ItemStack stack = recipe.getResultItem(player.level().registryAccess()).copy();
         stack.setCount(1);
         if (!recipe.getExtractCondition().isEmpty() && !player.isCreative()) {
            held.shrink(1);
            if (held.isEmpty()) {
               player.setItemInHand(hand, stack);
            } else {
               player.setItemInHand(hand, held);
               if (!player.addItem(stack)) {
                  player.drop(stack, false);
               }
            }
         } else if (!player.addItem(stack)) {
            player.drop(stack, false);
         }

         player.containerMenu.broadcastChanges();
         this.outputCount--;
         this.setChanged();
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         this.checkForMatchingRecipe();
      }
   }

   public void setProgress(int progress) {
      this.progress = progress;
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public void setMaxProgress(int maxProgress) {
      this.maxProgress = maxProgress;
   }

   public void setOutputCount(int outputCount) {
      this.outputCount = outputCount;
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public void setOutputTexture(ResourceLocation outputTexture) {
      this.outputTexture = outputTexture;
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider registries) {
      super.saveAdditional(tag, registries);
      tag.put("Tank", this.tank.writeToNBT(registries, new CompoundTag()));
      tag.put("Inventory", this.serializeInventory(registries));
      tag.putInt("Progress", this.progress);
      tag.putInt("MaxProgress", this.maxProgress);
      tag.putInt("OutputCount", this.outputCount);
      tag.putBoolean("RedstonePowered", this.redstonePowered);
      if (this.outputTexture != null) {
         tag.putString("OutputTexture", this.outputTexture.toString());
      }

      if (this.pendingRecipeId != null) {
         tag.putString("RecipeId", this.pendingRecipeId.toString());
      }
   }

   private CompoundTag serializeInventory(Provider registries) {
      CompoundTag tag = new CompoundTag();
      ListTag list = new ListTag();

      for (int i = 0; i < this.inputInventory.getSlots(); i++) {
         ItemStack stack = this.inputInventory.getStackInSlot(i);
         if (!stack.isEmpty()) {
            // 1.21.1 的 ItemStack.save(registries, prefix) 返回编码结果，不写入 prefix，须使用返回值
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) i);
            if (stack.save(registries, itemTag) instanceof CompoundTag compound && compound.contains("id")) {
               list.add(compound);
            }
         }
      }

      tag.put("Items", list);
      return tag;
   }

   @Override
   public void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider registries) {
      super.loadAdditional(tag, registries);
      this.tank.readFromNBT(registries, tag.getCompound("Tank"));
      this.deserializeInventory(registries, tag.getCompound("Inventory"));
      this.progress = tag.getInt("Progress");
      this.maxProgress = tag.getInt("MaxProgress");
      this.outputCount = tag.getInt("OutputCount");
      this.redstonePowered = tag.getBoolean("RedstonePowered");
      if (tag.contains("OutputTexture")) {
         this.outputTexture = ResourceLocation.parse(tag.getString("OutputTexture"));
      }

      if (tag.contains("RecipeId")) {
         this.pendingRecipeId = ResourceLocation.parse(tag.getString("RecipeId"));
         this.needsRecipeRestore = true;
         this.currentRecipeHolder = null;
      } else {
         this.pendingRecipeId = null;
         this.needsRecipeRestore = false;
         this.currentRecipeHolder = null;
      }

      if (this.level != null && !this.level.isClientSide) {
         this.checkForMatchingRecipe();
      }
   }

   private void deserializeInventory(Provider registries, CompoundTag tag) {
      ListTag list = tag.getList("Items", 10);

      // Items 只含非空槽位——先清空全部槽位再按包恢复，确保与服务器实际内容一致
      for (int i = 0; i < this.inputInventory.getSlots(); i++) {
         this.inputInventory.setStackInSlot(i, ItemStack.EMPTY);
      }

      for (int i = 0; i < list.size(); i++) {
         CompoundTag itemTag = list.getCompound(i);
         if (!itemTag.contains("id")) {
            continue;
         }

         int slot = itemTag.getByte("Slot") & 255;
         if (slot >= 0 && slot < this.inputInventory.getSlots()) {
            this.inputInventory.setStackInSlot(slot, ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY));
         }
      }
   }

   @NotNull
   @Override
   public CompoundTag getUpdateTag(@NotNull Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   @Nullable
   @Override
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public int getProgress() {
      return this.progress;
   }

   public int getMaxProgress() {
      return this.maxProgress;
   }

   public ResourceLocation getOutputTexture() {
      return this.outputTexture;
   }

   public int getOutputCount() {
      return this.outputCount;
   }

   /** 漏斗/管道自动取产物的扣减入口（条件产物在 FreezerTransfer 侧已被拦截，不会走到这里）。 */
   public void consumeOutput(int count) {
      if (this.outputCount > 0 && this.currentRecipeHolder != null
         && this.currentRecipeHolder.value().getExtractCondition().isEmpty()) {
         this.outputCount = Math.max(0, this.outputCount - count);
         this.setChanged();
         if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            this.checkForMatchingRecipe();
         }
      }
   }
}
