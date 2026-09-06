package com.bmt.kaleidoscope_world_liquor.blockentity;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerInput;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 冰柜 BE：CustomFluidTank(1000mB=FluidConstants.BUCKET) + 输入 4 槽；
 * 输出为虚槽（outputCount/输出贴图）。逻辑与 1.20.1 一致；
 * 配方 id 由 RecipeHolder 携带，BE 另存 holder id 用于跨存档恢复。
 * <p>
 * 漏斗自动化（WorldlyContainer）：上方/侧面投输入槽 0-3；下方抽输出虚槽
 * （槽 4）——需要碗的配方（波奇布丁等 extract_condition 非空）不开放漏斗抽取。
 */
public class FreezerBlockEntity extends BaseBlockEntity implements net.minecraft.world.WorldlyContainer {
    private static final String TANK = "Tank";
    private static final String INVENTORY = "Inventory";
    private static final String PROGRESS = "Progress";
    private static final String MAX_PROGRESS = "MaxProgress";
    private static final String OUTPUT_COUNT = "OutputCount";
    private static final String REDSTONE_POWERED = "RedstonePowered";
    private static final String OUTPUT_TEXTURE = "OutputTexture";
    private static final String RECIPE_ID = "RecipeId";

    public final CustomFluidTank tank;
    public final ItemStackHandler inputInventory;

    private int progress = 0;
    private int maxProgress = 0;
    private int outputCount = 0;
    @Nullable
    private Identifier outputTexture = null;
    private boolean redstonePowered = false;
    @Nullable
    private Identifier pendingRecipeId = null;
    private boolean needsRecipeRestore = false;
    @Nullable
    private RecipeHolder<FreezerRecipe> cachedRecipe = null;
    @Nullable
    private RecipeHolder<FreezerRecipe> recipe = null;

    public FreezerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FREEZER_BE, pos, state);
        this.tank = new CustomFluidTank(FluidConstants.BUCKET, this::onTankChanged) {
            @Override
            public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                // 有产出或工作中不接受注入（1.20.1 tank.fill 守卫同款）
                if (FreezerBlockEntity.this.hasOutput() || FreezerBlockEntity.this.isWorking()) {
                    return 0L;
                }
                return super.insert(resource, maxAmount, transaction);
            }
        };
        this.inputInventory = new ItemStackHandler(4) {
            @Override
            protected void onContentsChanged(int slot) {
                FreezerBlockEntity.this.onContentsChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking() && super.isItemValid(slot, stack);
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return !FreezerBlockEntity.this.hasOutput() && !FreezerBlockEntity.this.isWorking()
                        ? super.insertItem(slot, stack, simulate)
                        : stack;
            }
        };
    }

    private void onTankChanged() {
        this.refresh();
        if (level != null && !level.isClientSide()) {
            this.checkForMatchingRecipe();
        }
    }

    private void onContentsChanged() {
        this.refresh();
        if (level != null && !level.isClientSide()) {
            this.checkForMatchingRecipe();
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FreezerBlockEntity be) {
        if (!level.isClientSide()) {
            if (be.needsRecipeRestore && be.pendingRecipeId != null) {
                be.restoreRecipe();
                be.needsRecipeRestore = false;
            }
            if (state.getValue(FreezerBlock.WORKING)) {
                be.progress++;
                if (be.progress >= be.maxProgress) {
                    be.finishCrafting(state, level, pos);
                }
                if (be.progress % 20 == 0) {
                    be.refresh();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }

    private List<RecipeHolder<FreezerRecipe>> freezerRecipes() {
        return List.copyOf(level.recipeAccess().getSynchronizedRecipes().getAllOfType(ModRecipes.FREEZER_RECIPE_TYPE));
    }

    private void checkForMatchingRecipe() {
        if (level == null || level.isClientSide() || this.isWorking()) {
            return;
        }
        Optional<RecipeHolder<FreezerRecipe>> found = this.freezerRecipes().stream()
                .filter(holder -> holder.value().matches(new FreezerInput(this.tank, this.inputInventory.getStacks()), level))
                .findFirst();
        RecipeHolder<FreezerRecipe> oldCached = this.cachedRecipe;
        this.cachedRecipe = found.orElse(null);
        if (this.cachedRecipe != null && oldCached == null && this.canAutoStart()) {
            this.autoStartCrafting();
        }
    }

    private boolean canAutoStart() {
        BlockState state = this.getBlockState();
        return !this.isWorking() && !state.getValue(FreezerBlock.OPEN) && this.outputCount == 0;
    }

    private void autoStartCrafting() {
        if (this.tryStartCrafting()) {
            BlockState state = this.getBlockState();
            level.setBlockAndUpdate(this.worldPosition, state.setValue(FreezerBlock.WORKING, true));
        }
    }

    private void restoreRecipe() {
        if (level != null && this.pendingRecipeId != null) {
            this.recipe = this.freezerRecipes().stream()
                    .filter(holder -> holder.id().identifier().equals(this.pendingRecipeId))
                    .findFirst()
                    .orElse(null);
            if (this.recipe == null) {
                this.progress = 0;
                this.maxProgress = 0;
                BlockState state = this.getBlockState();
                if (this.isWorking()) {
                    level.setBlockAndUpdate(this.worldPosition, state.setValue(FreezerBlock.WORKING, false));
                }
            }
            this.checkForMatchingRecipe();
        }
    }

    public boolean tryStartCrafting() {
        if (level == null || this.cachedRecipe == null) {
            return false;
        }
        this.maxProgress = this.cachedRecipe.value().craftTime();
        this.progress = 0;
        this.recipe = this.cachedRecipe;
        this.pendingRecipeId = null;
        // fluidAmount 以 mB 记，转 Fabric transfer 单位（droplets）
        this.tank.drain(this.cachedRecipe.value().fluidAmount() * (FluidConstants.BUCKET / CustomFluidTank.MB_PER_BUCKET),
                CustomFluidTank.FluidAction.EXECUTE);
        for (int i = 0; i < 4; i++) {
            this.inputInventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        this.setChanged();
        return true;
    }

    private void finishCrafting(BlockState state, Level level, BlockPos pos) {
        if (this.recipe != null) {
            this.outputCount = this.recipe.value().result().getCount();
            this.outputTexture = this.recipe.value().resultTexture();
        }
        level.setBlock(pos, state.setValue(FreezerBlock.WORKING, false), 3);
        this.setChanged();
        level.sendBlockUpdated(pos, state, this.getBlockState(), 3);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
        this.checkForMatchingRecipe();
    }

    public void insertItem(ItemStack stack, Player player) {
        if (!this.hasOutput() && !this.isWorking()) {
            for (int i = 0; i < 4; i++) {
                if (this.inputInventory.getStackInSlot(i).isEmpty()) {
                    this.inputInventory.setStackInSlot(i, stack.copyWithCount(1));
                    stack.shrink(1);
                    return;
                }
            }
        }
    }

    public void extractItem(Player player) {
        for (int i = 3; i >= 0; i--) {
            if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
                player.getInventory().add(this.inputInventory.extractItem(i, 1, false));
                return;
            }
        }
    }

    public boolean hasOutput() {
        return this.outputCount > 0;
    }

    public int getComparatorSignal() {
        int signal = 0;
        if (!this.tank.isResourceBlank() && this.tank.getFluidAmountMb() > 0) {
            signal += 2;
        }
        for (int i = 0; i < 4; i++) {
            if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
                signal++;
            }
        }
        if (this.outputCount > 0) {
            signal += this.outputCount;
        } else if (this.isWorking() && this.recipe != null) {
            signal += this.recipe.value().result().getCount();
        }
        return Math.min(15, signal);
    }

    public boolean isRedstonePowered() {
        return this.redstonePowered;
    }

    public void setRedstonePowered(boolean redstonePowered) {
        if (this.redstonePowered != redstonePowered) {
            this.redstonePowered = redstonePowered;
            this.setChanged();
        }
    }

    public boolean hasMatchingRecipe() {
        return this.cachedRecipe != null;
    }

    public boolean isWorking() {
        return this.getBlockState().getValue(FreezerBlock.WORKING);
    }

    public boolean extractOutput(Player player, InteractionHand hand) {
        if (this.outputCount > 0 && this.recipe != null) {
            ItemStack held = player.getItemInHand(hand);
            FreezerRecipe r = this.recipe.value();
            if (!r.canExtract(held)) {
                // 照 1.21.1 范式：提示需要手持的物品名（波奇布丁 → 碗）
                net.minecraft.world.item.crafting.Ingredient condition = r.extractIngredient().orElse(null);
                ItemStack displayStack = condition != null
                        ? condition.items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY)
                        : ItemStack.EMPTY;
                net.minecraft.network.chat.Component displayName = !displayStack.isEmpty()
                        ? displayStack.getHoverName()
                        : net.minecraft.network.chat.Component.literal("?");
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "message.kaleidoscope_world_liquor.freezer.need_item", displayName), true);
                return false;
            }
            ItemStack result = r.result().copy();
            result.setCount(1);
            // extract_condition 非空（波奇布丁）：消耗手持碗；创造不消耗（清单 36）
            if (r.extractIngredient().isPresent() && !player.isCreative()) {
                held.shrink(1);
                if (held.isEmpty()) {
                    player.setItemInHand(hand, result);
                } else {
                    player.setItemInHand(hand, held);
                    if (!player.getInventory().add(result)) {
                        player.drop(result, false);
                    }
                }
            } else if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
            this.outputCount--;
            this.setChanged();
            level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            this.checkForMatchingRecipe();
            return true;
        }
        return false;
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

    @Nullable
    public Identifier getOutputTexture() {
        return this.outputTexture;
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        this.tank.writeToNBT(valueOutput.child(TANK));
        this.inputInventory.serialize(valueOutput.child(INVENTORY));
        valueOutput.putInt(PROGRESS, this.progress);
        valueOutput.putInt(MAX_PROGRESS, this.maxProgress);
        valueOutput.putInt(OUTPUT_COUNT, this.outputCount);
        valueOutput.putBoolean(REDSTONE_POWERED, this.redstonePowered);
        if (this.outputTexture != null) {
            valueOutput.putString(OUTPUT_TEXTURE, this.outputTexture.toString());
        }
        if (this.recipe != null) {
            valueOutput.putString(RECIPE_ID, this.recipe.id().identifier().toString());
        } else if (this.pendingRecipeId != null) {
            valueOutput.putString(RECIPE_ID, this.pendingRecipeId.toString());
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        valueInput.child(TANK).ifPresent(this.tank::readFromNBT);
        valueInput.child(INVENTORY).ifPresent(this.inputInventory::deserializeNBT);
        this.progress = valueInput.getIntOr(PROGRESS, 0);
        this.maxProgress = valueInput.getIntOr(MAX_PROGRESS, 0);
        this.outputCount = valueInput.getIntOr(OUTPUT_COUNT, 0);
        this.redstonePowered = valueInput.getBooleanOr(REDSTONE_POWERED, false);
        if (valueInput.contains(OUTPUT_TEXTURE)) {
            this.outputTexture = Identifier.parse(valueInput.getString(OUTPUT_TEXTURE).orElse(""));
        } else {
            this.outputTexture = null;
        }
        if (valueInput.contains(RECIPE_ID)) {
            this.pendingRecipeId = Identifier.parse(valueInput.getString(RECIPE_ID).orElse(""));
            this.needsRecipeRestore = true;
            this.recipe = null;
        } else {
            this.pendingRecipeId = null;
            this.needsRecipeRestore = false;
            this.recipe = null;
        }
        if (level != null && !level.isClientSide()) {
            this.checkForMatchingRecipe();
        }
    }

    // ================= 漏斗自动化（WorldlyContainer） =================

public static final int OUTPUT_SLOT = 4;
    private static final int[] INPUT_SLOTS = {0, 1, 2, 3};
    private static final int[] OUTPUT_SLOT_ARR = {4};

    private FreezerRecipe currentOutputRecipe() {
        return this.recipe != null ? this.recipe.value() : null;
    }

    private ItemStack viewOutput() {
        FreezerRecipe r = this.currentOutputRecipe();
        if (this.outputCount > 0 && r != null) {
            ItemStack result = r.result().copy();
            result.setCount(1);
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int[] getSlotsForFace(@NotNull Direction side) {
        // 下方 = 输出虚槽；其余 = 输入槽
        return side == Direction.DOWN ? OUTPUT_SLOT_ARR : INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction face) {
        if (slot == OUTPUT_SLOT || face == Direction.DOWN) {
            return false;
        }
        return !this.hasOutput() && !this.isWorking() && this.inputInventory.isItemValid(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction face) {
        if (slot != OUTPUT_SLOT || face != Direction.DOWN) {
            return false;
        }
        FreezerRecipe r = this.currentOutputRecipe();
        // 需要碗的配方（extract_condition 非空）不开放漏斗抽取
        return r != null && r.extractIngredient().isEmpty();
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < 4; i++) {
            if (!this.inputInventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return !(this.outputCount > 0 && this.currentOutputRecipe() != null);
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot >= 0 && slot < 4) {
            return this.inputInventory.getStackInSlot(slot);
        }
        return this.viewOutput();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int count) {
        if (slot == OUTPUT_SLOT) {
            ItemStack output = this.viewOutput();
            if (!output.isEmpty() && count > 0) {
                ItemStack taken = output.split(Math.min(count, output.getCount()));
                this.outputCount -= taken.getCount();
                this.setChanged();
                if (level != null) {
                    level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                    level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
                }
                this.checkForMatchingRecipe();
                return taken;
            }
            return ItemStack.EMPTY;
        }
        return this.inputInventory.extractItem(slot, count, false);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (slot == OUTPUT_SLOT) {
            ItemStack output = this.viewOutput();
            this.outputCount = 0;
            this.setChanged();
            return output;
        }
        return this.inputInventory.extractItem(slot, this.inputInventory.getStackInSlot(slot).getCount(), false);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot >= 0 && slot < 4) {
            this.inputInventory.setStackInSlot(slot, stack);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return slot < 4 && this.canPlaceItemThroughFace(slot, stack, null);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < 4; i++) {
            this.inputInventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        this.outputCount = 0;
    }

    // ================= Fabric Transfer API 流体槽（清单 35：原版经 capability 暴露给管道） =================

    /** 在主初始化中调用一次：任意侧面暴露 tank（工作中不阻断外部抽取，与 1.20.1 外部访问一致） */
    public static void registerFluidStorage() {
        net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                (FreezerBlockEntity be, Direction side) -> be.tank,
                ModBlockEntities.FREEZER_BE);
    }
}
