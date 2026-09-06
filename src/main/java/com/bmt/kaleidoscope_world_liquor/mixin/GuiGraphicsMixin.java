package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 臻酿附魔书的冷却条过滤：只有带 brew_accelerator 的附魔书才显示冷却条
 * （1.20.1 拦截 renderItemDecorations 的冷却百分比；1.21.11 冷却绘制
 * 已拆到 renderItemCooldown）。
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Inject(method = "renderItemCooldown", at = @At("HEAD"), cancellable = true)
    private void kwl$filterEnchantedBookCooldown(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!stack.is(Items.ENCHANTED_BOOK)) {
            return;
        }
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        boolean hasBrewAccelerator = hasLevel(enchantments) || hasLevel(stored);
        if (!hasBrewAccelerator) {
            // 非臻酿附魔书的全局冷却不显示（原版行为）
            ci.cancel();
        }
    }

    private boolean hasLevel(ItemEnchantments enchantments) {
        if (enchantments == null) {
            return false;
        }
        return containsKey(enchantments);
    }

    private boolean containsKey(ItemEnchantments enchantments) {
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().unwrapKey().map(k -> k.equals(ModEnchantments.BREW_ACCELERATOR)).orElse(false)) {
                return true;
            }
        }
        return false;
    }
}
