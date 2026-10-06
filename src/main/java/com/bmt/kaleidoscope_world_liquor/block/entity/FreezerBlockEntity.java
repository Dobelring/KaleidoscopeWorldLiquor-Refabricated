package com.bmt.kaleidoscope_world_liquor.block.entity;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import com.github.ysbbbbbb.kaleidoscopetavern.util.forge.IItemHandler;
import com.github.ysbbbbbb.kaleidoscopetavern.util.forge.ItemStackHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FreezerBlockEntity extends BlockEntity {
    // Fabric：无 Forge 能力系统，改用传输 API 暴露物品槽与流体槽（原 getCapability/invalidateCaps +
    // 三个 LazyOptional）。init 类（ModBlockEntities）归其它批次维护，故在 BE 类加载时注册
    //（首个 BE 实例化发生在区块加载，早于世界内任何 lookup——lookup 先取 BE 实例、必然触发类加载）。
    static {
        ItemStorage.SIDED.registerForBlockEntity((be, side) -> be.getItemStorage(side), ModBlockEntities.FREEZER_BE);
        FluidStorage.SIDED.registerForBlockEntity((be, side) -> be.getFluidStorage(side), ModBlockEntities.FREEZER_BE);
    }

    public FreezerRecipe recipe = FreezerRecipe.EMPTY;
    private FreezerRecipe cachedRecipe = FreezerRecipe.EMPTY;
    // 原 Forge：public final FluidTank tank = new FluidTank(1000) { onContentsChanged 同步 / fill 守卫 }。
    // Fabric 用 tavern CustomFluidTank（transfer API）；容量 FluidConstants.BUCKET = 81000 droplet = 1000mB，
    // 与原 new FluidTank(1000)（1000mB）一致；变更回调保持原同步语义：setChanged + sendBlockUpdated + 检查配方。
    public final CustomFluidTank tank = new Tank();
    public final ItemStackHandler inputInventory = new ItemStackHandler(4) {
        protected void onContentsChanged(int slot) {
            FreezerBlockEntity.this.setChanged();
            if (FreezerBlockEntity.this.level != null && !FreezerBlockEntity.this.level.isClientSide) {
                FreezerBlockEntity.this.level
                    .sendBlockUpdated(FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState(), FreezerBlockEntity.this.getBlockState(), 3);
                FreezerBlockEntity.this.checkForMatchingRecipe();
            }
        }

        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() ? super.isItemValid(slot, stack) : false;
        }

        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    public final ItemStackHandler outputInventory = new ItemStackHandler(1) {
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @NotNull
        public ItemStack getStackInSlot(int slot) {
            if (slot != 0) {
                return ItemStack.EMPTY;
            } else if (FreezerBlockEntity.this.hasOutput()
                && FreezerBlockEntity.this.recipe != FreezerRecipe.EMPTY
                && FreezerBlockEntity.this.recipe.getExtractIngredient().isEmpty()) {
                ItemStack result = FreezerBlockEntity.this.recipe.getResultItem(FreezerBlockEntity.this.level.registryAccess()).copy();
                result.setCount(1);
                return result;
            } else {
                return ItemStack.EMPTY;
            }
        }

        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0) {
                return ItemStack.EMPTY;
            } else if (FreezerBlockEntity.this.hasOutput()
                && FreezerBlockEntity.this.recipe != FreezerRecipe.EMPTY
                && FreezerBlockEntity.this.recipe.getExtractIngredient().isEmpty()) {
                int extractCount = Math.min(amount, FreezerBlockEntity.this.outputCount);
                if (extractCount <= 0) {
                    return ItemStack.EMPTY;
                } else {
                    ItemStack result = FreezerBlockEntity.this.recipe.getResultItem(FreezerBlockEntity.this.level.registryAccess()).copy();
                    result.setCount(extractCount);
                    if (!simulate) {
                        FreezerBlockEntity.this.outputCount -= extractCount;
                        FreezerBlockEntity.this.setChanged();
                        FreezerBlockEntity.this.level
                            .sendBlockUpdated(FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState(), FreezerBlockEntity.this.getBlockState(), 3);
                        FreezerBlockEntity.this.level.updateNeighborsAt(FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState().getBlock());
                        FreezerBlockEntity.this.checkForMatchingRecipe();
                    }

                    return result;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        public int getSlotLimit(int slot) {
            return 1;
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

    public static void tick(Level level, BlockPos pos, BlockState state, FreezerBlockEntity be) {
        if (!level.isClientSide && be.needsRecipeRestore && be.pendingRecipeId != null) {
            be.restoreRecipe();
            be.needsRecipeRestore = false;
        }

        if (!level.isClientSide) {
            if ((Boolean)state.getValue(FreezerBlock.WORKING)) {
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
            if (!this.isWorking()) {
                Optional<FreezerRecipe> recipe = this.level
                    .getRecipeManager()
                    .getAllRecipesFor(ModRecipes.FREEZER_TYPE)
                    .stream()
                    // 按跨批次契约 FreezerRecipe.matches(FluidVariant, long amount, items, level)，
                    // amount 为 mB（原 fluid.getAmount() 单位），故传 tank.getFluidAmountMb()。
                    // tank 为空时 FluidVariant 为 blank（getFluid() 为 null），直接不匹配——
                    // 等价原版空 FluidStack 的 Fluids.EMPTY.isSame(输入流体) 恒为 false。
                    .filter(r -> !this.tank.getResource().isBlank()
                        && r.matches(this.tank.getResource(), this.tank.getFluidAmountMb(), this.inventoryToNonNullList(), this.level))
                    .findFirst();
                FreezerRecipe oldCachedRecipe = this.cachedRecipe;
                this.cachedRecipe = recipe.orElse(FreezerRecipe.EMPTY);
                if (this.cachedRecipe != FreezerRecipe.EMPTY && oldCachedRecipe == FreezerRecipe.EMPTY && this.canAutoStart()) {
                    this.autoStartCrafting();
                }
            }
        }
    }

    private boolean canAutoStart() {
        BlockState state = this.getBlockState();
        return !this.isWorking() && !(Boolean)state.getValue(FreezerBlock.OPEN) && this.outputCount == 0;
    }

    private void autoStartCrafting() {
        if (this.tryStartCrafting()) {
            BlockState state = this.getBlockState();
            this.level.setBlock(this.worldPosition, (BlockState)state.setValue(FreezerBlock.WORKING, true), 3);
        }
    }

    private void restoreRecipe() {
        if (this.level != null && this.pendingRecipeId != null) {
            this.recipe = this.level
                .getRecipeManager()
                .getAllRecipesFor(ModRecipes.FREEZER_TYPE)
                .stream()
                .filter(r -> r.getId().equals(this.pendingRecipeId))
                .findFirst()
                .orElse(FreezerRecipe.EMPTY);
            if (this.recipe == FreezerRecipe.EMPTY) {
                this.progress = 0;
                this.maxProgress = 0;
                BlockState state = this.getBlockState();
                if (this.isWorking()) {
                    this.level.setBlock(this.worldPosition, (BlockState)state.setValue(FreezerBlock.WORKING, false), 3);
                }
            }

            this.checkForMatchingRecipe();
        }
    }

    public boolean tryStartCrafting() {
        if (this.level == null) {
            return false;
        } else if (this.cachedRecipe == FreezerRecipe.EMPTY) {
            return false;
        } else {
            this.maxProgress = this.cachedRecipe.getCraftTime();
            this.progress = 0;
            this.recipe = this.cachedRecipe;
            this.pendingRecipeId = null;
            // 原 Forge：tank.drain(cachedRecipe.getInputFluid().getAmount(), EXECUTE)，数量为 mB；
            // CustomFluidTank.drain 以 droplet 计，这里做 mB→droplet 换算（1000mB = FluidConstants.BUCKET）
            this.tank.drain(
                (long)this.cachedRecipe.getFluidAmount() * FluidConstants.BUCKET / CustomFluidTank.MB_PER_BUCKET,
                CustomFluidTank.FluidAction.EXECUTE
            );

            for (int i = 0; i < 4; i++) {
                this.inputInventory.setStackInSlot(i, ItemStack.EMPTY);
            }

            this.setChanged();
            return true;
        }
    }

    private void finishCrafting(BlockState state, Level level, BlockPos pos) {
        if (this.recipe != FreezerRecipe.EMPTY) {
            this.outputCount = this.recipe.getResultItem(level.registryAccess()).getCount();
            this.maxOutputCount = this.outputCount;
            this.outputTexture = this.recipe.getResultTexture();
        }

        level.setBlock(pos, (BlockState)state.setValue(FreezerBlock.WORKING, false), 3);
        this.setChanged();
        level.sendBlockUpdated(pos, state, this.getBlockState(), 3);
        level.updateNeighborsAt(pos, state.getBlock());
        this.checkForMatchingRecipe();
    }

    public void insertItem(ItemStack stack, Player player) {
        if (!this.hasOutput() && !this.isWorking()) {
            for (int i = 0; i < 4; i++) {
                if (this.inputInventory.getStackInSlot(i).isEmpty()) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    this.inputInventory.setStackInSlot(i, copy);
                    stack.shrink(1);
                    return;
                }
            }
        }
    }

    public void extractItem(Player player) {
        for (int i = 3; i >= 0; i--) {
            if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
                player.addItem(this.inputInventory.extractItem(i, 1, false));
                return;
            }
        }
    }

    public boolean hasOutput() {
        return this.outputCount > 0;
    }

    public int getComparatorSignal() {
        int signal = 0;
        if (!this.tank.isResourceBlank()) {
            signal += 2;
        }

        for (int i = 0; i < 4; i++) {
            if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
                signal++;
            }
        }

        if (this.outputCount > 0) {
            signal += this.outputCount;
        } else if (this.isWorking() && this.recipe != FreezerRecipe.EMPTY && this.level != null) {
            signal += this.recipe.getResultItem(this.level.registryAccess()).getCount();
        }

        return Math.min(15, signal);
    }

    public boolean isRedstonePowered() {
        return this.redstonePowered;
    }

    public void setRedstonePowered(boolean redstonePowered) {
        this.redstonePowered = redstonePowered;
    }

    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    public boolean hasMatchingRecipe() {
        return this.cachedRecipe != FreezerRecipe.EMPTY;
    }

    public boolean isWorking() {
        return (Boolean)this.getBlockState().getValue(FreezerBlock.WORKING);
    }

    public boolean extractOutput(Player player) {
        if (this.outputCount > 0 && this.recipe != FreezerRecipe.EMPTY) {
            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!this.recipe.canExtract(held)) {
                // 需碗配方空手取出：actionbar 提示（带物品悬停名），对齐已验收 1.21.1 行为
                Ingredient extractIngredient = this.recipe.getExtractIngredient();
                ItemStack[] displayItems = extractIngredient.getItems();
                Component displayName = displayItems.length > 0 ? displayItems[0].getHoverName() : Component.literal("?");
                player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.need_item", displayName), true);
                return false;
            } else {
                ItemStack result = this.recipe.getResultItem(player.level().registryAccess()).copy();
                result.setCount(1);
                // 创造模式不消耗碗（规格：碗消耗创造不耗）
                if (!this.recipe.getExtractIngredient().isEmpty() && !player.isCreative()) {
                    held.shrink(1);
                    if (held.isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, result);
                    } else if (!player.addItem(result)) {
                        player.drop(result, false);
                    }
                } else if (!player.addItem(result)) {
                    player.drop(result, false);
                }

                this.outputCount--;
                this.setChanged();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
                this.checkForMatchingRecipe();
                return true;
            }
        } else {
            return false;
        }
    }

    private NonNullList<ItemStack> inventoryToNonNullList() {
        NonNullList<ItemStack> list = NonNullList.withSize(4, ItemStack.EMPTY);

        for (int i = 0; i < 4; i++) {
            list.set(i, this.inputInventory.getStackInSlot(i));
        }

        return list;
    }

    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        // 原 Forge：tag.put("Tank", this.tank.writeToNBT(new CompoundTag()))，
        // Forge FluidTank 的 NBT 结构为 FluidName/Amount(/Tag)。tavern CustomFluidTank 的
        // writeToNBT 用的是自己的键（fluid/amount/nbt），与官方存档不互通；按「以官方为准」裁定，
        // 这里按 Forge 结构手写序列化，保证与官方 Forge 1.20.1 存档兼容。
        CompoundTag tankTag = new CompoundTag();
        Fluid fluid = this.tank.getFluid();
        int fluidAmountMb = this.tank.getFluidAmountMb();
        if (fluid == null || fluid == Fluids.EMPTY || fluidAmountMb <= 0) {
            tankTag.putString("FluidName", "minecraft:empty");
            tankTag.putInt("Amount", 0);
        } else {
            tankTag.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid).toString());
            tankTag.putInt("Amount", fluidAmountMb);
            if (this.tank.getFluidVariant().getNbt() != null) {
                tankTag.put("Tag", this.tank.getFluidVariant().getNbt().copy());
            }
        }
        tag.put("Tank", tankTag);
        tag.put("Inventory", this.inputInventory.serializeNBT());
        tag.putInt("Progress", this.progress);
        tag.putInt("MaxProgress", this.maxProgress);
        tag.putInt("OutputCount", this.outputCount);
        tag.putInt("MaxOutputCount", this.maxOutputCount);
        tag.putBoolean("RedstonePowered", this.redstonePowered);
        if (this.outputTexture != null) {
            tag.putString("OutputTexture", this.outputTexture.toString());
        }

        if (this.recipe != FreezerRecipe.EMPTY) {
            tag.putString("RecipeId", this.recipe.getId().toString());
        } else if (this.pendingRecipeId != null) {
            tag.putString("RecipeId", this.pendingRecipeId.toString());
        }
    }

    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        // 原 Forge：this.tank.readFromNBT(tag.getCompound("Tank"))（直接写字段、不经过 fill 守卫）。
        // 按 Forge 的 FluidName/Amount 结构解析后经 loadContents 直写 tank（同样绕过注入守卫），
        // 保证官方 Forge 存档中的流体能原样恢复。
        CompoundTag tankTag = tag.getCompound("Tank");
        String fluidName = tankTag.getString("FluidName");
        int amountMb = tankTag.getInt("Amount");
        // 空罐分支必须写回 blank/0：客户端收到「取空」更新包时若跳过，tank 会保留旧值
        // （实测 bug：桶抽出成功但客户端液面不消失——load 原来只在 Amount>0 时才动 tank）。
        FluidVariant restoreVariant = FluidVariant.blank();
        long restoreDroplets = 0L;
        if (amountMb > 0 && !fluidName.isEmpty() && !"minecraft:empty".equals(fluidName)) {
            ResourceLocation fluidId = new ResourceLocation(fluidName);
            if (BuiltInRegistries.FLUID.containsKey(fluidId)) {
                Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
                if (fluid != null && fluid != Fluids.EMPTY) {
                    restoreVariant = tankTag.contains("Tag", Tag.TAG_COMPOUND)
                        ? FluidVariant.of(fluid, tankTag.getCompound("Tag"))
                        : FluidVariant.of(fluid);
                    restoreDroplets = (long) amountMb * FluidConstants.BUCKET / CustomFluidTank.MB_PER_BUCKET;
                }
            }
        }
        ((Tank) this.tank).loadContents(restoreVariant, restoreDroplets);
        this.inputInventory.deserializeNBT(tag.getCompound("Inventory"));
        this.progress = tag.getInt("Progress");
        this.maxProgress = tag.getInt("MaxProgress");
        this.outputCount = tag.getInt("OutputCount");
        this.maxOutputCount = tag.getInt("MaxOutputCount");
        this.redstonePowered = tag.getBoolean("RedstonePowered");
        if (tag.contains("OutputTexture")) {
            this.outputTexture = new ResourceLocation(tag.getString("OutputTexture"));
        }

        if (tag.contains("RecipeId")) {
            this.pendingRecipeId = new ResourceLocation(tag.getString("RecipeId"));
            this.needsRecipeRestore = true;
            this.recipe = FreezerRecipe.EMPTY;
        } else {
            this.pendingRecipeId = null;
            this.needsRecipeRestore = false;
            this.recipe = FreezerRecipe.EMPTY;
        }

        if (this.level != null && !this.level.isClientSide) {
            this.checkForMatchingRecipe();
        }
    }

    @NotNull
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // 原 Forge 的 onDataPacket(Connection, pkt) { this.load(pkt.getTag()); } 与
    // handleUpdateTag(tag) { this.load(tag); } 已删除：1.20.1 原版客户端收到更新包时由
    // ClientPacketListener.handleBlockEntityData 直接调用 be.load(tag)，区块加载也走 load——
    // 与这两个 Forge 钩子现有实现完全等价，行为无损。

    // Fabric：原 Forge getCapability(ITEM_HANDLER) 的按面分流逻辑
    //（工作中→空；DOWN 面有产出→产出槽；否则→4 格输入槽），side 为 null 时等价 Forge 的非 DOWN 分支。
    public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
        if (this.isWorking()) {
            return null;
        }
        if (side == Direction.DOWN && this.hasOutput()) {
            return new OutputSlotStorage();
        }
        return new HandlerStorage(this.inputInventory, true);
    }

    // Fabric：原 Forge getCapability(FLUID_HANDLER)：side==null 且工作中时为空，其余暴露 tank；
    // 「工作中/有产出拒绝注入」的守卫在 Tank.fill/Tank.insert 上（见 Tank 内部类）。
    public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        return side == null && this.isWorking() ? null : this.tank;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getOutputCount() {
        return this.outputCount;
    }

    public int getMaxOutputCount() {
        return this.maxOutputCount;
    }

    public ResourceLocation getOutputTexture() {
        return this.outputTexture;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.setChanged();
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setOutputCount(int outputCount) {
        this.outputCount = outputCount;
        this.setChanged();
    }

    public void setOutputTexture(ResourceLocation outputTexture) {
        this.outputTexture = outputTexture;
        this.setChanged();
    }

    /**
     * 替代原 Forge 匿名 FluidTank：容量为一桶（1000mB），变更回调保持原 onContentsChanged 语义；
     * fill 守卫等价原 fill 覆写（有产出/工作中拒绝注入），insert 守卫覆盖传输 API（漏斗/流体管道）路径；
     * loadContents 仅供 NBT 恢复使用，绕过守卫（等价原 tank.readFromNBT 的直接赋值）。
     */
    private class Tank extends CustomFluidTank {
        Tank() {
            super(FluidConstants.BUCKET, () -> {
                FreezerBlockEntity.this.setChanged();
                if (FreezerBlockEntity.this.level != null && !FreezerBlockEntity.this.level.isClientSide) {
                    FreezerBlockEntity.this.level
                        .sendBlockUpdated(FreezerBlockEntity.this.worldPosition, FreezerBlockEntity.this.getBlockState(), FreezerBlockEntity.this.getBlockState(), 3);
                    FreezerBlockEntity.this.checkForMatchingRecipe();
                }
            });
        }

        @Override
        public long fill(FluidVariant resource, long maxAmount, FluidAction action) {
            return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() ? super.fill(resource, maxAmount, action) : 0;
        }

        @Override
        public long insert(FluidVariant variant, long maxAmount, TransactionContext transaction) {
            return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() ? super.insert(variant, maxAmount, transaction) : 0;
        }

        public void loadContents(FluidVariant variant, long amount) {
            this.variant = variant;
            this.amount = Math.min(Math.max(amount, 0L), this.getCapacityTransfer());
        }
    }

    /**
     * Fabric：把输入槽 ItemStackHandler 适配成传输 API 的 Storage
     *（CF 工程 ModBlockEntities.ItemHandlerStorage 先例；此处嵌套在本类内，批次文件清单不含独立文件）。
     * 插入/抽出守卫走 handler.isItemValid / SingleStackStorage 事务语义。
     * insert 两段式（先堆已有同种槽、再落空槽）与 extract 3→0 对齐已验收的 1.21.1 FreezerInputStorage 语义
     *（对齐原版 ItemStackHandler.insertItem 的堆叠优先 + 手取顺序）。
     */
    private class HandlerStorage implements Storage<ItemVariant> {
        private final List<HandlerSlotStorage> slots = new ArrayList<>();

        private HandlerStorage(IItemHandler handler, boolean extractable) {
            for (int i = 0; i < handler.getSlots(); i++) {
                this.slots.add(new HandlerSlotStorage(handler, i, extractable));
            }
        }

        @Override
        public boolean supportsInsertion() {
            return !FreezerBlockEntity.this.isWorking() && !FreezerBlockEntity.this.hasOutput();
        }

        @Override
        public boolean supportsExtraction() {
            return !FreezerBlockEntity.this.isWorking();
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            long inserted = 0L;
            // 第一段：堆进已有同种物品的槽（不落新格）
            for (HandlerSlotStorage slot : this.slots) {
                if (inserted >= maxAmount) {
                    break;
                }
                if (!slot.isResourceBlank() && slot.getResource().equals(resource)) {
                    inserted += slot.insert(resource, maxAmount - inserted, transaction);
                }
            }
            // 第二段：剩余量落空槽
            for (HandlerSlotStorage slot : this.slots) {
                if (inserted >= maxAmount) {
                    break;
                }
                if (slot.isResourceBlank()) {
                    inserted += slot.insert(resource, maxAmount - inserted, transaction);
                }
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            long extracted = 0L;
            // 3→0：与手取一致（取出最后放入的）
            for (int i = this.slots.size() - 1; i >= 0; i--) {
                if (extracted >= maxAmount) {
                    break;
                }
                extracted += this.slots.get(i).extract(resource, maxAmount - extracted, transaction);
            }
            return extracted;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return Collections.<StorageView<ItemVariant>>unmodifiableList(this.slots).iterator();
        }
    }

    private static class HandlerSlotStorage extends SingleStackStorage {
        private final IItemHandler handler;
        private final int slot;
        private final boolean extractable;

        private HandlerSlotStorage(IItemHandler handler, int slot, boolean extractable) {
            this.handler = handler;
            this.slot = slot;
            this.extractable = extractable;
        }

        @Override
        protected ItemStack getStack() {
            return this.handler.getStackInSlot(this.slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            this.handler.setStackInSlot(this.slot, stack);
        }

        @Override
        protected boolean canInsert(ItemVariant variant) {
            // 输入槽的 isItemValid 自带「有产出/工作中拒绝」守卫（原 insertItem 覆写）
            return this.handler.isItemValid(this.slot, variant.toStack(1));
        }

        @Override
        protected boolean canExtract(ItemVariant variant) {
            return this.extractable;
        }

        @Override
        public boolean supportsExtraction() {
            return this.extractable;
        }

        @Override
        protected int getCapacity(ItemVariant variant) {
            int limit = this.handler.getSlotLimit(this.slot);
            return variant.isBlank() ? limit : Math.min(limit, variant.getItem().getMaxStackSize());
        }
    }

    /**
     * Fabric：产出槽是虚拟槽（栈由 recipe+outputCount 推导），不走 SingleStackStorage——
     * 抽取必须经 outputInventory.extractItem 以保留 outputCount 扣减、发包、邻块与配方检查语义；
     * 用 SnapshotParticipant 对 outputCount 做事务快照，事务回滚时恢复。
     */
    private class OutputSlotStorage extends SnapshotParticipant<Long> implements SingleSlotStorage<ItemVariant> {
        private boolean available() {
            return FreezerBlockEntity.this.hasOutput()
                && FreezerBlockEntity.this.recipe != FreezerRecipe.EMPTY
                && FreezerBlockEntity.this.recipe.getExtractIngredient().isEmpty();
        }

        private ItemStack resultStack(int count) {
            ItemStack result = FreezerBlockEntity.this.recipe.getResultItem(FreezerBlockEntity.this.level.registryAccess()).copy();
            result.setCount(count);
            return result;
        }

        @Override
        public ItemVariant getResource() {
            return this.available() ? ItemVariant.of(this.resultStack(1)) : ItemVariant.blank();
        }

        @Override
        public long getAmount() {
            return this.available() ? FreezerBlockEntity.this.outputCount : 0L;
        }

        @Override
        public long getCapacity() {
            return this.getAmount();
        }

        @Override
        public boolean isResourceBlank() {
            return !this.available();
        }

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long insert(ItemVariant variant, long maxAmount, TransactionContext transaction) {
            // 原 Forge：outputInventory.isItemValid 恒 false → insertItem 原样退回
            return 0L;
        }

        @Override
        public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
            if (!this.available() || maxAmount <= 0 || variant.isBlank() || !this.getResource().equals(variant)) {
                return 0L;
            }
            int extractCount = (int)Math.min(maxAmount, FreezerBlockEntity.this.outputCount);
            if (extractCount <= 0) {
                return 0L;
            }
            // 先做事务快照（记录 outputCount），回滚时 readSnapshot 恢复
            this.updateSnapshots(transaction);
            // 走原 outputInventory.extractItem：扣减 outputCount、发包、更新邻块、检查配方
            FreezerBlockEntity.this.outputInventory.extractItem(0, extractCount, false);
            return extractCount;
        }

        @Override
        protected Long createSnapshot() {
            return (long) FreezerBlockEntity.this.outputCount;
        }

        @Override
        protected void readSnapshot(Long snapshot) {
            FreezerBlockEntity.this.outputCount = snapshot.intValue();
            FreezerBlockEntity.this.setChanged();
        }
    }
}
