package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 附魔台工具拦截（对齐官方 Forge IForgeEnchantment#canApplyAtEnchantingTable）。
 * <p>
 * 1.20.1 原版 EnchantmentHelper.getAvailableEnchantmentResults 直接读 {@code enchantment.category}
 * 调 {@code EnchantmentCategory.canEnchant(Item)}（字节码实锤，不经 Enchantment#canEnchant 覆写），
 * 臻酿的 BREAKABLE 类别放行一切耐久物品。此处把「臻酿 + 非书物品」的 category 判定改回 false：
 * 书本仍走 isBook 旁路可出臻酿（与官方一致），工具/武器/盔甲不再进附魔台候选。
 * 铁砧路径由 AnvilMenuMixin 单独拦截；随机战利品附魔书也走 isBook 旁路，不受影响。
 * 需登记进 kaleidoscope_world_liquor.mixins.json 的 mixins 数组。
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @ModifyExpressionValue(
        method = "getAvailableEnchantmentResults(ILnet/minecraft/world/item/ItemStack;Z)Ljava/util/List;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"
        )
    )
    private static boolean kaleidoscope_world_liquor$blockBrewAcceleratorOnTools(boolean original, @Local Enchantment enchantment, @Local ItemStack stack) {
        if (original && enchantment == ModEnchantments.BREW_ACCELERATOR && !stack.is(Items.BOOK)) {
            return false;
        }
        return original;
    }
}
