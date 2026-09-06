package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({GuiGraphics.class})
public abstract class GuiGraphicsMixin {
   @ModifyVariable(
      method = {"renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"},
      at = @At("STORE"),
      ordinal = 0
   )
   private float kaleidoscope_world_liquor$filterEnchantedBookCooldown(float cooldownPercent, Font font, ItemStack stack, int x, int y, String text) {
      if (!(stack.getItem() instanceof EnchantedBookItem)) {
         return cooldownPercent;
      } else {
         ItemEnchantments storedEnchants = (ItemEnchantments)stack.get(DataComponents.STORED_ENCHANTMENTS);
         if (storedEnchants != null && !storedEnchants.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
               return cooldownPercent;
            } else {
               RegistryLookup<Enchantment> enchantLookup = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
               boolean hasBrewAccelerator = enchantLookup.get(ModEnchantments.BREW_ACCELERATOR).<Integer>map(storedEnchants::getLevel).orElse(0) > 0;
               return hasBrewAccelerator ? cooldownPercent : 0.0F;
            }
         } else {
            return 0.0F;
         }
      }
   }
}
