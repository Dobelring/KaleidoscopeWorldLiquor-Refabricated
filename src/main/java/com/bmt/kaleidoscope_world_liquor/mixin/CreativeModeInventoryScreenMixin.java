package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.client.creativetab.CreativeTabFilter;
import com.bmt.kaleidoscope_world_liquor.client.creativetab.CreativeTabOrder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在创造界面左侧挂「酒水 / 装饰」过滤按钮。
 * 继承 AbstractContainerScreen 是为了能直接访问 protected 的 leftPos/topPos 与 addRenderableWidget
 * （原版没有 NeoForge 的 getGuiLeft/getGuiTop，也没有 ScreenEvent.Init.Post）。
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    public CreativeModeInventoryScreenMixin() {
        super(null, null, null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void kwl$addFilterButtons(CallbackInfo ci) {
        // 此处 Fabric 已完成 tab 位置分配（构造器里跑过 buildAllTabContents），可以安全落位
        CreativeTabOrder.apply();

        for (Button button : CreativeTabFilter.createButtons(this.leftPos, this.topPos)) {
            this.addRenderableWidget(button);
        }
        CreativeTabFilter.onScreenInit((CreativeModeInventoryScreen) (Object) this);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void kwl$syncFilterButtons(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CreativeTabFilter.sync((CreativeModeInventoryScreen) (Object) this);
    }
}
