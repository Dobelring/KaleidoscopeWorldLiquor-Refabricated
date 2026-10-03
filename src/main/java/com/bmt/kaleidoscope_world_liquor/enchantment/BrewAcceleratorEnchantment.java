package com.bmt.kaleidoscope_world_liquor.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

public class BrewAcceleratorEnchantment extends Enchantment {
    public static final int COOLDOWN_TICKS = 2400;

    public BrewAcceleratorEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE, new EquipmentSlot[0]);
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

    // Forge 专有钩子 IForgeEnchantment#canApplyAtEnchantingTable，Fabric 原版无对应物（保留为普通方法）。
    // 官方语义由 mixins/EnchantmentHelperMixin 实现：1.20.1 原版附魔台判定为
    // category.canEnchant(item) || stack.is(Items.BOOK)（直读 category 字段，不经本类覆写），
    // mixin 在该判定上把「臻酿 + 非书」改回 false——书本走 isBook 旁路仍可出（与官方一致），工具被拦截。
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof BookItem;
    }

    // 同上：原版附魔台路径不经本覆写，保留作语义声明。
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof BookItem;
    }

    // TODO(fabric): Forge 专有钩子 IForgeEnchantment#isAllowedOnBooks，Fabric 原版无对应物（保留为普通方法）。
    // 其默认语义为 !isTreasureOnly()，本附魔 isTreasureOnly=false，故保留 true 与原版默认等价，无行为损失。
    public boolean isAllowedOnBooks() {
        return true;
    }

    public boolean isTradeable() {
        return true;
    }

    public boolean isDiscoverable() {
        return true;
    }
}
