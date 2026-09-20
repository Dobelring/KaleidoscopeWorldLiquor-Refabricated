package com.bmt.kaleidoscope_world_liquor.item;

import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BottledDrinkItem extends Item implements IHasContainer {
   public BottledDrinkItem(@NotNull Properties properties) {
      super(properties);
   }

   public Item getContainerItem() {
      return Items.GLASS_BOTTLE;
   }

   public UseAnim getUseAnimation(@NotNull ItemStack stack) {
      return UseAnim.DRINK;
   }

   public int getUseDuration(@NotNull ItemStack stack, LivingEntity entity) {
      return 32;
   }

   /** cola / tonic_water 的颜色说明行（1.1.9 起由物品提供，取代原来的 ItemTooltipEvent 实现）。 */
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      if (this == ModItems.COLA) {
         tooltip.add(
            Component.empty()
               .append(Component.translatable("item.kaleidoscope_world_liquor.cola.tooltip.front").withStyle(ChatFormatting.GRAY))
               .append(Component.translatable("item.kaleidoscope_world_liquor.cola.tooltip.back").withStyle(ChatFormatting.DARK_RED))
         );
      }

      if (this == ModItems.TONIC_WATER) {
         tooltip.add(
            Component.empty()
               .append(Component.translatable("item.kaleidoscope_world_liquor.tonic_water.tooltip.front").withStyle(ChatFormatting.GRAY))
               .append(Component.translatable("item.kaleidoscope_world_liquor.tonic_water.tooltip.back").withStyle(ChatFormatting.WHITE))
         );
      }
   }

   @NotNull
   public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
      ItemStack result = super.finishUsingItem(stack, level, entity);
      if (entity instanceof Player player && !player.getAbilities().instabuild) {
         ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
         if (!player.getInventory().add(bottle)) {
            player.drop(bottle, false);
         }
      }

      return result;
   }
}
