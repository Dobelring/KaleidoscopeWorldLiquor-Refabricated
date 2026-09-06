package com.bmt.kaleidoscope_world_liquor.blockentity;

import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 酒窖柜 BE：9 槽（继承 tavern StorageBlockEntity，槽限 1、ValueInput/Output 存取）。
 * 原版的 filtered IItemHandler capability 在 Fabric 无对应——
 * 放置过滤在 BE 侧覆写 isItemValid 实现，效果一致（Java 版酒窖柜的插入
 * 全部经由玩家交互 / 漏斗 Container，第 7 步给 BE 挂 Container 接口时同样走此过滤）。
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
}
