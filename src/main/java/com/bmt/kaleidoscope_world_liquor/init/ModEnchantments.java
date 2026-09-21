package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * brew_accelerator 附魔：1.21+ 数据驱动（data/kaleidoscope_world_liquor/enchantment/...json
 * 已在第 3 步迁移），代码里只保留 ResourceKey 供事件层查询。
 */
public final class ModEnchantments {
    private ModEnchantments() {
    }

    public static final ResourceKey<Enchantment> BREW_ACCELERATOR = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "brew_accelerator"));

    public static void register() {
    }
}
