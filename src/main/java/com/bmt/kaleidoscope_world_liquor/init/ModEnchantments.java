package com.bmt.kaleidoscope_world_liquor.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
   public static final ResourceKey<Enchantment> BREW_ACCELERATOR = ResourceKey.create(
      Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "brew_accelerator")
   );
}
