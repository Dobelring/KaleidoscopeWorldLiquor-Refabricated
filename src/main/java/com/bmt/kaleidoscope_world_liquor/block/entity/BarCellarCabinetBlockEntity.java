package com.bmt.kaleidoscope_world_liquor.block.entity;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.forge.IItemHandler;
import com.github.ysbbbbbb.kaleidoscopetavern.util.forge.ItemStackHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class BarCellarCabinetBlockEntity extends StorageBlockEntity {
    // Fabric：无 Forge 能力系统，改用传输 API 暴露过滤后的物品槽（原 getCapability/invalidateCaps +
    // LazyOptional<IItemHandler> itemHandlerCap）。init 类归其它批次维护，故在 BE 类加载时注册
    //（首个 BE 实例化发生在区块加载，早于世界内任何 ItemStorage 查询）。
    static {
        ItemStorage.SIDED.registerForBlockEntity((be, side) -> be.getItemStorage(side), ModBlockEntities.BAR_CELLAR_CABINET_BE);
    }

    public BarCellarCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BAR_CELLAR_CABINET_BE, pos, state, 9);
    }

    /** 官方 1.1.12-fix：酒窖柜可放置判定（原生瓶查黑名单 tag，其余查 placeable tag）。 */
    private boolean canPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            boolean isNativeBottle = stack.getItem() instanceof BottleBlockItem;
            return isNativeBottle
                ? !stack.is(BarCellarCabinetBlock.BAR_CELLAR_CABINET_NATIVE_BLACKLIST)
                : stack.is(BarCellarCabinetBlock.BAR_CELLAR_CABINET_PLACEABLE);
        }
    }

    /** 官方 onContentChanged：刷新 + 邻块比较器更新（比较器交互新增项）。 */
    private void onContentChanged() {
        this.refresh();
        if (this.level != null) {
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    private IItemHandler createFilteredHandler() {
        final ItemStackHandler original = this.getItems();
        return new IItemHandler() {
            public int getSlots() {
                return original.getSlots();
            }

            @NotNull
            public ItemStack getStackInSlot(int slot) {
                return original.getStackInSlot(slot);
            }

            // 官方 1.1.12-fix：仅空槽可插入且按 canPlace 过滤；每槽限 1；允许漏斗抽出
            @NotNull
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (stack.isEmpty() || !BarCellarCabinetBlockEntity.this.canPlace(stack)) {
                    return stack;
                }

                ItemStack existing = original.getStackInSlot(slot);
                if (!existing.isEmpty()) {
                    return stack;
                }

                if (!simulate) {
                    original.setStackInSlot(slot, stack.copyWithCount(1));
                    BarCellarCabinetBlockEntity.this.onContentChanged();
                }

                return stack.copyWithCount(stack.getCount() - 1);
            }

            @NotNull
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (amount <= 0) {
                    return ItemStack.EMPTY;
                }

                ItemStack existing = original.getStackInSlot(slot);
                if (existing.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                int extract = Math.min(amount, existing.getCount());
                ItemStack result = existing.copyWithCount(extract);
                if (!simulate) {
                    if (extract >= existing.getCount()) {
                        original.setStackInSlot(slot, ItemStack.EMPTY);
                    } else {
                        original.setStackInSlot(slot, existing.copyWithCount(existing.getCount() - extract));
                    }

                    BarCellarCabinetBlockEntity.this.onContentChanged();
                }

                return result;
            }

            public int getSlotLimit(int slot) {
                return 1;
            }

            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return BarCellarCabinetBlockEntity.this.canPlace(stack);
            }

            // tavern 的 IItemHandler 比 Forge 版多一个 setStackInSlot（NeoForge 接口形状）；
            // 传输 API 槽位提交/回滚经此写回
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                original.setStackInSlot(slot, stack);
            }
        };
    }

    // Fabric：原 Forge 对各面返回同一个自动化 IItemHandler；side==null 不暴露（原 getCapability 守卫）
    public Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
        if (side == null) {
            return null;
        }

        return new FilteredStorage(this.createFilteredHandler(), this::onContentChanged);
    }

    private static class FilteredStorage implements Storage<ItemVariant> {
        private final List<FilteredSlotStorage> slots = new ArrayList<>();

        private FilteredStorage(IItemHandler handler, Runnable onCommit) {
            for (int i = 0; i < handler.getSlots(); i++) {
                this.slots.add(new FilteredSlotStorage(handler, i, onCommit));
            }
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            long inserted = 0L;
            for (FilteredSlotStorage slot : this.slots) {
                if (inserted >= maxAmount) {
                    break;
                }
                inserted += slot.insert(resource, maxAmount - inserted, transaction);
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            // 原 Forge：过滤 handler 的 extractItem 恒返回 ItemStack.EMPTY，漏斗取不走展示品
            return 0L;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return Collections.<StorageView<ItemVariant>>unmodifiableList(this.slots).iterator();
        }
    }

    private static class FilteredSlotStorage extends SingleStackStorage {
        private final IItemHandler handler;
        private final int slot;
        private final Runnable onCommit;

        private FilteredSlotStorage(IItemHandler handler, int slot, Runnable onCommit) {
            this.handler = handler;
            this.slot = slot;
            this.onCommit = onCommit;
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
            return this.handler.isItemValid(this.slot, variant.toStack(1));
        }

        @Override
        protected boolean canExtract(ItemVariant variant) {
            return false;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        protected int getCapacity(ItemVariant variant) {
            int limit = this.handler.getSlotLimit(this.slot);
            return variant.isBlank() ? limit : Math.min(limit, variant.getItem().getMaxStackSize());
        }

        @Override
        protected void onFinalCommit() {
            this.onCommit.run();
        }
    }
}
