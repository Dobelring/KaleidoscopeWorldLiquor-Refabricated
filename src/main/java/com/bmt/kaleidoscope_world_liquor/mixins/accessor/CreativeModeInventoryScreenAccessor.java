package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 创造栏筛选器：selectedTab 声明在 CreativeModeInventoryScreen 自身（private static），
 * 用于检测当前选中标签页（原 Forge 有 getSelectedTab 补丁）。
 * leftPos/topPos 与 addRenderableWidget 声明在父类，见 AbstractContainerScreenAccessor / ScreenAddWidgetAccessor。
 */
@Mixin({CreativeModeInventoryScreen.class})
public interface CreativeModeInventoryScreenAccessor {
    @Accessor("selectedTab")
    static CreativeModeTab kwl$getSelectedTab() {
        throw new AssertionError();
    }
}
