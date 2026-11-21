package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.enchantment.BrewAcceleratorEnchantment;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
    // 官方 1.2.0 的「仅附魔书」类别在 Fabric 1.20.1 无法用 EnchantmentCategory.create 复刻
    //（构造私有）——等价实现见 BrewAcceleratorEnchantment#canEnchant（铁砧只认附魔书）
    // 与 mixins/EnchantmentHelperMixin（附魔台拦截工具路径）。
    public static final Enchantment BREW_ACCELERATOR = new BrewAcceleratorEnchantment();

    public ModEnchantments() {
    }

    public static void registerEnchantments() {
        Registry.register(BuiltInRegistries.ENCHANTMENT, KaleidoscopeWorldLiquor.id("brew_accelerator"), BREW_ACCELERATOR);
    }
}
