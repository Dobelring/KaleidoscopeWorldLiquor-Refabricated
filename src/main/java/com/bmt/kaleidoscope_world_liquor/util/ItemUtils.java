package com.bmt.kaleidoscope_world_liquor.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 官方 1.2.0：冰箱输入规则用到的容器物/背包工具。
 */
public final class ItemUtils {
   private ItemUtils() {
   }

   /** 带合成剩余物的物品（桶、蛋糕等）返回剩余物，药水/蜂蜜瓶视为玻璃瓶，其余返回 AIR。 */
   public static Item getContainerItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return Items.AIR;
      } else {
         net.minecraft.world.item.ItemStackTemplate template = stack.getItem().getCraftingRemainder();
         // 26.x：无剩余物的物品返回 null 模板（实测空手/无剩余物物品右键冰箱直接 NPE）
         ItemStack remainder = template == null ? ItemStack.EMPTY : template.create();
         if (!remainder.isEmpty()) {
            return remainder.getItem();
         } else {
            return !stack.is(Items.POTION) && !stack.is(Items.HONEY_BOTTLE) ? Items.AIR : Items.GLASS_BOTTLE;
         }
      }
   }

   public static boolean fitsInInventory(Player player, ItemStack stack) {
      return moveToInventory(player.getInventory(), stack, true) <= 0;
   }

   public static void giveItemToPlayer(Player player, ItemStack stack) {
      int remaining = moveToInventory(player.getInventory(), stack, false);
      if (remaining > 0) {
         player.drop(stack.copyWithCount(remaining), false);
      }
   }

   private static int moveToInventory(Inventory inventory, ItemStack stack, boolean simulate) {
      int count = stack.getCount();
      int maxStackSize = stack.getMaxStackSize();

      for (int i = 0; i < 36 && count > 0; i++) {
         ItemStack slot = inventory.getItem(i);
         if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
            int move = Math.min(count, slot.getMaxStackSize() - slot.getCount());
            count -= move;
            if (!simulate) {
               slot.grow(move);
            }
         }
      }

      for (int i = 0; i < 36 && count > 0; i++) {
         if (inventory.getItem(i).isEmpty()) {
            int move = Math.min(count, maxStackSize);
            count -= move;
            if (!simulate) {
               inventory.setItem(i, stack.copyWithCount(move));
            }
         }
      }

      return count;
   }
}
