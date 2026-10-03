package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.enchantment.BrewAcceleratorEnchantment;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
    public static final Enchantment BREW_ACCELERATOR = new BrewAcceleratorEnchantment();

    public ModEnchantments() {
    }

    public static void registerEnchantments() {
        Registry.register(BuiltInRegistries.ENCHANTMENT, KaleidoscopeWorldLiquor.id("brew_accelerator"), BREW_ACCELERATOR);
    }
}
