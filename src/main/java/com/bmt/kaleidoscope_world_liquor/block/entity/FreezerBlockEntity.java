package com.bmt.kaleidoscope_world_liquor.block.entity;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidTank;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.bmt.kaleidoscope_world_liquor.inventory.SimpleItemHandler;
import com.bmt.kaleidoscope_world_liquor.util.ItemUtils;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
   // 官方 1.2.0：禁止塞入冰箱的物品黑名单（工具/盔甲/桶/潜影盒/碗/花盆/玻璃瓶等）
   public static final TagKey<Item> FREEZER_BLACKLIST = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "freezer_blacklist"));
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

      // 官方 1.2.0：有产出或工作中时不允许注入流体
      @Override
      public int fill(FluidStack resource, boolean simulate) {
         return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() ? super.fill(resource, simulate) : 0;
      }
   };
   private boolean suppressInputUpdates = false;
   public final SimpleItemHandler inputInventory = new SimpleItemHandler(4) {
      @Override
      protected void onContentsChanged(int slot) {
         FreezerBlockEntity.this.onInputChanged(slot);
      }

      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
         return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking()
            ? !stack.is(FreezerBlockEntity.FREEZER_BLACKLIST) && ItemUtils.getContainerItem(stack) == Items.AIR
            : false;
      }

      @NotNull
      @Override
      public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
         return !FreezerBlockEntity.this.hasOutput()
               && !FreezerBlockEntity.this.isWorking()
               && !stack.isEmpty()
               && !stack.is(FreezerBlockEntity.FREEZER_BLACKLIST)
               && ItemUtils.getContainerItem(stack) == Items.AIR
            ? super.insertItem(slot, stack, simulate)
            : stack;
      }

      // 官方 1.2.0：自动化只能取出不带容器物的输入（如碗装物不可自动取出）
      @NotNull
      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         if (!FreezerBlockEntity.this.isWorking() && amount > 0) {
            ItemStack stack = this.getStackInSlot(slot);
            if (!stack.isEmpty() && ItemUtils.getContainerItem(stack) == Items.AIR) {
               int extractCount = Math.min(amount, stack.getCount());
               if (extractCount <= 0) {
                  return ItemStack.EMPTY;
               } else {
                  ItemStack extracted = stack.copyWithCount(extractCount);
                  if (!simulate) {
                     ItemStack remaining = stack.copy();
                     remaining.shrink(extractCount);
                     this.setStackInSlot(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                  }

                  return extracted;
               }
            } else {
               return ItemStack.EMPTY;
            }
         } else {
            return ItemStack.EMPTY;
         }
      }

      @Override
      public int getSlotLimit(int slot) {
         return 1;
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
         } else {
            RecipeHolder<FreezerRecipe> rh = FreezerBlockEntity.this.currentRecipeHolder;
            if (rh != null && FreezerBlockEntity.this.hasOutput() && rh.value().getCarrier().isEmpty()) {
               ItemStack result = rh.value().getResultItem(FreezerBlockEntity.this.level.registryAccess()).copy();
               result.setCount(1);
               return result;
            } else {
               return ItemStack.EMPTY;
            }
         }
      }

      @NotNull
      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         if (slot != 0 || amount <= 0) {
            return ItemStack.EMPTY;
         } else {
            RecipeHolder<FreezerRecipe> rh = FreezerBlockEntity.this.currentRecipeHolder;
            if (rh != null && FreezerBlockEntity.this.hasOutput() && rh.value().getCarrier().isEmpty()) {
               int extractCount = Math.min(amount, FreezerBlockEntity.this.outputCount);
               if (extractCount <= 0) {
                  return ItemStack.EMPTY;
               } else {
                  ItemStack result = rh.value().getResultItem(FreezerBlockEntity.this.level.registryAccess()).copy();
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
   private int maxOutputCount = 0;
   private ResourceLocation outputTexture = null;
   private boolean redstonePowered = false;
   private ResourceLocation pendingRecipeId = null;
   private boolean needsRecipeRestore = false;

   public FreezerBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.FREEZER_BE, pos, state);
   }

   // 官方 1.2.0：配方匹配启动期间的静默清槽（避免触发重复配方检查）
   protected void onInputChanged(int slot) {
      if (!this.suppressInputUpdates) {
         this.setChanged();
         if (this.level != null && !this.level.isClientSide) {
            this.level
               .sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.checkForMatchingRecipe();
         }
      }
   }

   private void clearInputsSilently() {
      this.suppressInputUpdates = true;

      try {
         for (int i = 0; i < this.inputInventory.getSlots(); i++) {
            this.inputInventory.setStackInSlot(i, ItemStack.EMPTY);
         }
      } finally {
         this.suppressInputUpdates = false;
      }
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
               this.startCraftingAndUpdateState();
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
         this.clearInputsSilently();
         this.tank.drain(recipe.getInputFluid().getAmount(), false);
         this.setChanged();
         return true;
      } else {
         return false;
      }
   }

   // 官方 1.2.0：启动成功时一并关闭盖子并进入工作态，供方块交互与红石统一调用
   public boolean startCraftingAndUpdateState() {
      if (!this.tryStartCrafting()) {
         return false;
      } else {
         if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            this.level
               .setBlock(this.worldPosition, (BlockState)((BlockState)state.setValue(FreezerBlock.OPEN, false)).setValue(FreezerBlock.WORKING, true), 3);
         }

         return true;
      }
   }

   private void finishCrafting(BlockState state, Level level, BlockPos pos) {
      if (this.currentRecipeHolder != null) {
         FreezerRecipe recipe = this.currentRecipeHolder.value();
         this.outputCount = recipe.getResultItem(level.registryAccess()).getCount();
         this.maxOutputCount = this.outputCount;
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

   // 官方 1.2.0：黑名单拦截 + 创造不消耗 + 带容器物塞入时把容器退还玩家
   public boolean insertItem(ItemStack stack, Player player, InteractionHand hand) {
      if (!this.hasOutput() && !this.isWorking() && !stack.isEmpty() && !stack.is(FREEZER_BLACKLIST)) {
         Item container = ItemUtils.getContainerItem(stack);

         for (int i = 0; i < 4; i++) {
            if (this.inputInventory.getStackInSlot(i).isEmpty()) {
               this.inputInventory.setStackInSlot(i, stack.copyWithCount(1));
               if (!player.isCreative()) {
                  stack.shrink(1);
                  player.setItemInHand(hand, stack);
               }

               if (container != Items.AIR) {
                  ItemUtils.giveItemToPlayer(player, new ItemStack(container));
               }

               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   // 官方 1.2.0：空手取最上层输入；持对应容器物时按容器数量取出（need_item 提示）
   public boolean extractItem(Player player, ItemStack held) {
      if (this.isWorking()) {
         return false;
      } else {
         int index = -1;

         for (int i = 3; i >= 0; i--) {
            if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
               index = i;
               break;
            }
         }

         if (index < 0) {
            return false;
         } else {
            ItemStack stack = this.inputInventory.getStackInSlot(index);
            Item container = ItemUtils.getContainerItem(stack);
            int extractCount;
            if (container != Items.AIR) {
               if (!held.is(container)) {
                  if (held.isEmpty()) {
                     player.displayClientMessage(
                        Component.translatable("message.kaleidoscope_world_liquor.freezer.need_item", new Object[]{new ItemStack(container).getHoverName()}),
                        true
                     );
                  }

                  return false;
               }

               extractCount = player.isCreative() ? stack.getCount() : Math.min(stack.getCount(), held.getCount());
            } else {
               if (!held.isEmpty()) {
                  return false;
               }

               extractCount = stack.getCount();
            }

            if (extractCount <= 0) {
               return false;
            } else {
               ItemStack extracted = stack.copyWithCount(extractCount);
               if (!ItemUtils.fitsInInventory(player, extracted)) {
                  return false;
               } else {
                  if (container != Items.AIR && !player.isCreative()) {
                     held.shrink(extractCount);
                  }

                  ItemStack remaining = stack.copy();
                  remaining.shrink(extractCount);
                  this.inputInventory.setStackInSlot(index, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                  ItemUtils.giveItemToPlayer(player, extracted);
                  return true;
               }
            }
         }
      }
   }

   public void extractItem(Player player) {
      this.extractItem(player, ItemStack.EMPTY);
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

   // 官方 1.2.0：取产物改为返回布尔（成功才播音效）；carrier 按需消耗，创造不消耗
   public boolean extractOutput(Player player, InteractionHand hand) {
      if (this.outputCount > 0 && this.currentRecipeHolder != null) {
         FreezerRecipe recipe = this.currentRecipeHolder.value();
         ItemStack held = player.getItemInHand(hand);
         if (!recipe.canExtract(held)) {
            Ingredient carrier = recipe.getCarrier();
            ItemStack[] displayItems = carrier.getItems();
            Component displayName = displayItems.length > 0 ? displayItems[0].getHoverName() : Component.literal("?");
            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.need_item", new Object[]{displayName}), true);
            return false;
         }

         ItemStack stack = recipe.getResultItem(player.level().registryAccess()).copy();
         stack.setCount(1);
         if (!recipe.getCarrier().isEmpty() && !player.isCreative()) {
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

         this.outputCount--;
         this.setChanged();
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         this.checkForMatchingRecipe();
         return true;
      } else {
         return false;
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
      tag.putInt("MaxOutputCount", this.maxOutputCount);
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
      this.maxOutputCount = tag.getInt("MaxOutputCount");
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

   public int getMaxOutputCount() {
      return this.maxOutputCount;
   }

   /** 漏斗/管道自动取产物的扣减入口（条件产物在 FreezerTransfer 侧已被拦截，不会走到这里）。 */
   public void consumeOutput(int count) {
      if (this.outputCount > 0 && this.currentRecipeHolder != null
         && this.currentRecipeHolder.value().getCarrier().isEmpty()) {
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
