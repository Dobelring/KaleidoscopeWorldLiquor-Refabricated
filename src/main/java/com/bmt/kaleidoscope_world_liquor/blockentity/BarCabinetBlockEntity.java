package com.bmt.kaleidoscope_world_liquor.blockentity;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 酒柜（2 个展示槽）。
 * <p>
 * 官方 1.1.11（Neo/Forge 线）：新增 IItemHandler 漏斗自动化（不规则方块单槽、每槽限 1、
 * 允许抽出）+ 比较器输出；Fabric 侧经 {@code compat/transfer/CabinetTransfer} 注册传输 API Storage
 * （side==null 不暴露，对齐原 getCapability(side!=null) 守卫）。
 */
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

    /** 官方 1.1.11：可放置判定 = 原生瓶/鸡尾酒 或 placeable/irregular tag 方块。 */
    public static boolean canPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            boolean isNativeBottle = stack.getItem() instanceof BottleBlockItem;
            boolean isNativeCocktail = stack.getItem() instanceof CocktailBlockItem;
            boolean isNativeDrink = isNativeBottle || isNativeCocktail;
            boolean isTagNormal = stack.is(BarCabinetBlock.BAR_CABINET_PLACEABLE) && stack.getItem() instanceof BlockItem;
            boolean isTagIrregular = stack.is(BarCabinetBlock.BAR_CABINET_IRREGULAR) && stack.getItem() instanceof BlockItem;
            return isNativeDrink || isTagNormal || isTagIrregular;
        }
    }

    public static boolean isIrregular(ItemStack stack) {
        return stack.is(BarCabinetBlock.BAR_CABINET_IRREGULAR) && stack.getItem() instanceof BlockItem;
    }

    /** 官方 onContentChanged：刷新展示 + 提示邻块比较器更新。 */
    private void onContentChanged() {
        this.refresh();
        if (this.level != null) {
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    /** 由 CabinetTransfer 注册（side==null 的判定在注册侧）。 */
    public Storage<ItemVariant> getAutomationStorage() {
        return new DisplayStorage();
    }

    /** 2 个展示槽的传输 API Storage（官方 AutomationHandler 的 Fabric 等价物）。 */
    private class DisplayStorage implements Storage<ItemVariant> {
        private final CabinetSlot leftSlot = new CabinetSlot(0);
        private final CabinetSlot rightSlot = new CabinetSlot(1);

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0 || !BarCabinetBlockEntity.canPlace(resource.toStack(1))) {
                return 0L;
            }

            if (BarCabinetBlockEntity.isIrregular(resource.toStack(1))) {
                if (!leftSlot.getStack().isEmpty() || !rightSlot.getStack().isEmpty()) {
                    return 0L;
                }
                return leftSlot.insert(resource, Math.min(maxAmount, 1L), transaction);
            }

            long inserted = leftSlot.insert(resource, Math.min(maxAmount, 1L), transaction);
            if (inserted < 1L && maxAmount >= 1L) {
                inserted += rightSlot.insert(resource, 1L, transaction);
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            long extracted = leftSlot.extract(resource, maxAmount, transaction);
            if (extracted < maxAmount) {
                extracted += rightSlot.extract(resource, maxAmount - extracted, transaction);
            }
            return extracted;
        }

        @Override
        public boolean supportsInsertion() {
            return true;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return List.<StorageView<ItemVariant>>of(leftSlot, rightSlot).iterator();
        }
    }

    private class CabinetSlot extends SingleStackStorage {
        private final int slot;

        private CabinetSlot(int slot) {
            this.slot = slot;
        }

        @Override
        protected ItemStack getStack() {
            return this.slot == 0 ? BarCabinetBlockEntity.this.leftItem : BarCabinetBlockEntity.this.rightItem;
        }

        @Override
        protected void setStack(ItemStack stack) {
            if (this.slot == 0) {
                BarCabinetBlockEntity.this.leftItem = stack;
            } else {
                BarCabinetBlockEntity.this.rightItem = stack;
            }
        }

        @Override
        protected boolean canInsert(ItemVariant variant) {
            return BarCabinetBlockEntity.canPlace(variant.toStack(1));
        }

        @Override
        protected boolean canExtract(ItemVariant variant) {
            return true;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        protected int getCapacity(ItemVariant variant) {
            // 官方 getSlotLimit() = 1（每槽单件）
            return 1;
        }

        @Override
        protected void onFinalCommit() {
            if (this.slot == 0) {
                BarCabinetBlockEntity.this.isSingle = BarCabinetBlockEntity.isIrregular(this.getStack());
            } else {
                BarCabinetBlockEntity.this.isSingle = false;
            }
            BarCabinetBlockEntity.this.onContentChanged();
        }
    }
}
