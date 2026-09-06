package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModCompatItems;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TooltipEvents {
   public static void registerClient() {
      ItemTooltipCallback.EVENT.register(TooltipEvents::onItemTooltip);
   }

   private static void onItemTooltip(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.TooltipFlag flag, List<Component> lines) {
      if (stack.is(ModItems.COLA)) {
         Component tooltipLine = Component.empty()
            .append(Component.translatable("item.kaleidoscope_world_liquor.cola.tooltip.front").withStyle(ChatFormatting.GRAY))
            .append(Component.translatable("item.kaleidoscope_world_liquor.cola.tooltip.back").withStyle(ChatFormatting.DARK_RED));
         lines.add(tooltipLine);
      }

      if (stack.is(ModItems.TONIC_WATER)) {
         Component tooltipLine = Component.empty()
            .append(Component.translatable("item.kaleidoscope_world_liquor.tonic_water.tooltip.front").withStyle(ChatFormatting.GRAY))
            .append(Component.translatable("item.kaleidoscope_world_liquor.tonic_water.tooltip.back").withStyle(ChatFormatting.WHITE));
         lines.add(tooltipLine);
      }

      if (stack.is(ModCompatItems.LIANGSHAN_ICE_CONE)) {
         addTooltip(lines, "item.kaleidoscope_twilight.liangshan_ice_cone.tooltip");
      } else if (stack.is(ModCompatItems.KITA_STUFFED_CRISP)) {
         addTooltip(lines, "item.kaleidoscope_twilight.kita_stuffed_crisp.tooltip");
      } else if (stack.is(ModCompatItems.POCHI_PUDDING)) {
         addTooltip(lines, "item.kaleidoscope_twilight.pochi_pudding.tooltip");
      } else if (stack.is(ModCompatItems.MAGIC_CRISPY_CORNER)) {
         addTooltip(lines, "item.kaleidoscope_twilight.magic_crispy_corner.tooltip");
      }
   }

   private static void addTooltip(List<Component> lines, String key) {
      lines.add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
   }
}
