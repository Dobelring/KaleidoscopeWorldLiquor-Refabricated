package com.bmt.kaleidoscope_world_liquor.blockentity;

import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Iterator;
import java.util.List;

/**
 * 酒柜 BE：左右双槽 + 单槽模式（异形酒瓶独占时）。
 * 字段名与 1.20.1 一致（left_item/right_item/is_single）。
 * <p>
 * 官方 1.1.11：新增 IItemHandler 漏斗自动化（异形单槽独占、每槽限 1、允许抽出）
 * + 内容变化时刷新展示并提示邻块比较器；Fabric 侧经
 * {@code compat/transfer/CabinetTransfer} 注册传输 API Storage（side==null 不暴露，
 * 对齐原 {@code getCapability(ITEM_HANDLER) && side != null} 守卫）。
 */
public class BarCabinetBlockEntity extends BaseBlockEntity {
    private static final String LEFT = "left_item";
    private static final String RIGHT = "right_item";
    private static final String SINGLE = "is_single";

    private ItemStack leftItem = ItemStack.EMPTY;
    private ItemStack rightItem = ItemStack.EMPTY;
    private boolean isSingle = false;

    public BarCabinetBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
        super(ModBlockEntities.BAR_CABINET_BE, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.leftItem = valueInput.read(LEFT, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.rightItem = valueInput.read(RIGHT, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.isSingle = valueInput.getBooleanOr(SINGLE, false);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (!this.leftItem.isEmpty()) {
            valueOutput.store(LEFT, ItemStack.CODEC, this.leftItem);
        }
        if (!this.rightItem.isEmpty()) {
            valueOutput.store(RIGHT, ItemStack.CODEC, this.rightItem);
        }
        valueOutput.putBoolean(SINGLE, this.isSingle);
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
        }
        boolean isNativeDrink = stack.getItem() instanceof BottleBlockItem
                || stack.getItem() instanceof CocktailBlockItem;
        boolean isTagNormal = stack.is(ModTags.BAR_CABINET_PLACEABLE) && stack.getItem() instanceof BlockItem;
        boolean isTagIrregular = stack.is(ModTags.BAR_CABINET_IRREGULAR) && stack.getItem() instanceof BlockItem;
        return isNativeDrink || isTagNormal || isTagIrregular;
    }

    /** 官方 1.1.11：异形酒（irregular tag 的方块物品）独占整个柜。 */
    public static boolean isIrregular(ItemStack stack) {
        return stack.is(ModTags.BAR_CABINET_IRREGULAR) && stack.getItem() instanceof BlockItem;
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
                // 异形酒占双槽：任一槽有物则拒收，否则只进左槽
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
            // 官方 1.1.11：允许漏斗抽出
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
            // 左槽放异形酒 → 单槽模式；右槽放任何东西 → 双槽模式
            if (this.slot == 0) {
                BarCabinetBlockEntity.this.isSingle = BarCabinetBlockEntity.isIrregular(this.getStack());
            } else {
                BarCabinetBlockEntity.this.isSingle = false;
            }
            BarCabinetBlockEntity.this.onContentChanged();
        }
    }
}
