package com.bmt.kaleidoscope_world_liquor.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

/**
 * 官方 1.2.0：类别收窄为「仅附魔书」+ 槽位 MAINHAND。
 * Fabric 1.20.1 原版 EnchantmentCategory 无自定义入口（构造私有，Forge 的
 * EnchantmentCategory.create 不可用），故保留 BREAKABLE 类别 + EnchantmentHelperMixin
 * 拦附魔台工具路径；铁砧路径走本类覆写的 canEnchant（仅附魔书），与官方类别收窄等价。
 * 原 isAllowedOnBooks/isTradeable/isDiscoverable 覆写删除（1.20.1 原版默认即 true，无行为差异）。
 */
public class BrewAcceleratorEnchantment extends Enchantment {
    public static final int COOLDOWN_TICKS = 2400;

    public BrewAcceleratorEnchantment() {
        super(Rarity.UNCOMMON, net.minecraft.world.item.enchantment.EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 官方 1.2.0 BREW_ACCELERATOR_BOOK 类别的等价实现：铁砧只认附魔书
        return stack.getItem() instanceof EnchantedBookItem;
    }

    public int getMinCost(int level) {
        return 15;
    }

    public int getMaxCost(int level) {
        return this.getMinCost(level) + 40;
    }

    public int getMaxLevel() {
        return 1;
    }
}
