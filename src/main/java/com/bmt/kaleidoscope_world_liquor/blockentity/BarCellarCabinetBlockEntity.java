package com.bmt.kaleidoscope_world_liquor.blockentity;

import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 酒窖柜 BE：9 槽（继承 tavern StorageBlockEntity，槽限 1、ValueInput/Output 存取）。
 * 原版的 filtered IItemHandler capability 在 Fabric 无对应——
 * 放置过滤在 BE 侧覆写 isItemValid 实现，效果一致。
 * <p>
 * 官方 1.1.11：IItemHandler 漏斗自动化（canPlace 过滤、空槽限 1、允许抽出）+ 比较器输出；
 * Fabric 侧经 {@code compat/transfer/CabinetTransfer} 注册传输 API Storage。
 */
public class BarCellarCabinetBlockEntity extends StorageBlockEntity {
    public BarCellarCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BAR_CELLAR_CABINET_BE, pos, state, 9);
    }

    public boolean acceptsItem(ItemStack stack) {
        return stack.is(ModTags.BAR_CELLAR_CABINET_PLACEABLE);
    }

    public ItemStackHandler items() {
        return this.getItems();
    }

    /** 官方 1.1.11：酒窖柜可放置判定（原生瓶查黑名单 tag，其余查 placeable tag）。 */
    private static boolean canPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean isNativeBottle = stack.getItem() instanceof BottleBlockItem;
        return isNativeBottle
                ? !stack.is(ModTags.BAR_CELLAR_CABINET_NATIVE_BLACKLIST)
                : stack.is(ModTags.BAR_CELLAR_CABINET_PLACEABLE);
    }

    /** 官方 onContentChanged：刷新 + 邻块比较器更新。 */
    private void onContentChanged() {
        this.refresh();
        if (this.level != null) {
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    /** 由 CabinetTransfer 注册（side==null 的判定在注册侧）。 */
    public Storage<ItemVariant> getAutomationStorage() {
        return new CellarStorage();
    }

    private class CellarStorage implements Storage<ItemVariant> {
        private final List<CellarSlot> slots = new ArrayList<>();

        private CellarStorage() {
            for (int i = 0; i < 9; i++) {
                this.slots.add(new CellarSlot(i));
            }
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0L;
            }
            long inserted = 0L;
            for (CellarSlot slot : this.slots) {
                if (inserted >= maxAmount) {
                    break;
                }
                inserted += slot.insert(resource, maxAmount - inserted, transaction);
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0L;
            }
            long extracted = 0L;
            for (CellarSlot slot : this.slots) {
                if (extracted >= maxAmount) {
                    break;
                }
                extracted += slot.extract(resource, maxAmount - extracted, transaction);
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
            return List.<StorageView<ItemVariant>>copyOf(this.slots).iterator();
        }
    }

    private class CellarSlot extends SingleStackStorage {
        private final int index;

        private CellarSlot(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack getStack() {
            return BarCellarCabinetBlockEntity.this.getItems().getStackInSlot(this.index);
        }

        @Override
        protected void setStack(ItemStack stack) {
            BarCellarCabinetBlockEntity.this.getItems().setStackInSlot(this.index, stack);
        }

        @Override
        protected boolean canInsert(ItemVariant variant) {
            return BarCellarCabinetBlockEntity.canPlace(variant.toStack(1));
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
            // 官方 getSlotLimit() = 1（自动化视图每槽单件）
            return 1;
        }

        @Override
        protected void onFinalCommit() {
            BarCellarCabinetBlockEntity.this.onContentChanged();
        }
    }
}
