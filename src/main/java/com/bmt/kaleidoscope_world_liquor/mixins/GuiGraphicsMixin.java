package com.bmt.kaleidoscope_world_liquor.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 官方 1.2.0：附魔书冷却条改读每本书自身 NBT（kaleidoscope_brew_cooldown_until），
 * 没有冷却数据的书直接不显示冷却覆盖层。
 */
@Mixin({GuiGraphics.class})
public abstract class GuiGraphicsMixin {
   @ModifyVariable(
      method = {"renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"},
      at = {@At("STORE")},
      ordinal = 0
   )
   private float kaleidoscope_world_liquor$stackCooldown(float cooldownPercent, Font font, ItemStack stack, int x, int y, String text) {
      if (!(stack.getItem() instanceof EnchantedBookItem)) {
         return cooldownPercent;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.level == null) {
            return 0.0F;
         } else {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            long cooldownUntil = tag.getLong("kaleidoscope_brew_cooldown_until");
            long remaining = cooldownUntil - minecraft.level.getGameTime();
            return remaining <= 0L ? 0.0F : Mth.clamp((float) remaining / 2400.0F, 0.0F, 1.0F);
         }
      }
   }
}
