package com.bmt.kaleidoscope_world_liquor.item;

import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
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
