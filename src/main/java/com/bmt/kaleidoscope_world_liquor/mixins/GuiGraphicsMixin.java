package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({GuiGraphics.class})
public abstract class GuiGraphicsMixin {
    public GuiGraphicsMixin() {
    }

    @ModifyVariable(
        method = {"renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"},
        at = @At("STORE"),
        ordinal = 0
    )
    private float kaleidoscope_world_liquor$filterEnchantedBookCooldown(float cooldownPercent, Font font, ItemStack stack, int x, int y, String text) {
        return stack.getItem() instanceof EnchantedBookItem && !EnchantmentHelper.getEnchantments(stack).containsKey(ModEnchantments.BREW_ACCELERATOR)
            ? 0.0F
            : cooldownPercent;
    }
}
