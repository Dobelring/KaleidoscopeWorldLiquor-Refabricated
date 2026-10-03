package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 创造栏筛选器 accessor（按 Mixin 要求拆分：@Accessor/@Invoker 只在**目标类自身声明**里查找，
 * leftPos/topPos 声明在父类 AbstractContainerScreen，不能挂在 CreativeModeInventoryScreen 上——
 * 实测 InvalidAccessorException: No candidates were found matching field_2776 in class_481）。
 */
@Mixin({AbstractContainerScreen.class})
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int kwl$getLeftPos();

    @Accessor("topPos")
    int kwl$getTopPos();
}
