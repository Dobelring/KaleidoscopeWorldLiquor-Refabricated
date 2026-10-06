package com.bmt.kaleidoscope_world_liquor.compat.transfer;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import org.jetbrains.annotations.Nullable;

/**
 * 吧台柜/酒窖柜的漏斗自动化（官方 1.1.11 IItemHandler 的 Fabric 等价物）。
 * side==null 不暴露——对齐官方 {@code getCapability(ITEM_HANDLER) && side != null} 守卫。
 */
public final class CabinetTransfer {
    private CabinetTransfer() {
    }

    public static void register() {
        ItemStorage.SIDED.registerForBlockEntity(
            (@Nullable BarCabinetBlockEntity be, net.minecraft.core.Direction side) -> side == null ? null : be.getAutomationStorage(),
            ModBlockEntities.BAR_CABINET_BE
        );
        ItemStorage.SIDED.registerForBlockEntity(
            (@Nullable BarCellarCabinetBlockEntity be, net.minecraft.core.Direction side) -> side == null ? null : be.getAutomationStorage(),
            ModBlockEntities.BAR_CELLAR_CABINET_BE
        );
    }
}
