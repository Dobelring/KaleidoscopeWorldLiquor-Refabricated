package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class SoundEvents {
   private static final Map<UUID, Integer> drinkingTicksMap = new HashMap<>();
   private static final ResourceLocation COOL_ICE_TEA_ITEM = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "cool_tea");
   private static final ResourceLocation SOUR_PLUM_ITEM = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "sour_plum");

   public static void onStartDrinking(LivingEntity entity, ItemStack itemStack) {
      if (entity instanceof Player player) {
         if (isIceTeaItem(itemStack)) {
            drinkingTicksMap.put(player.getUUID(), 0);
         }

         if (isSourPlum(itemStack)) {
            drinkingTicksMap.put(player.getUUID(), 0);
         }
      }
   }

   public static void onTickDrinking(LivingEntity entity, ItemStack stack) {
      if (entity.level().isClientSide && isCoolIceTea(stack) && stack.getUseAnimation() == UseAnim.DRINK) {
         int usedTicks = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
         if (usedTicks % 4 == 0) {
            playCoolIceTeaSound(entity);
         }
      }

      if (entity.level().isClientSide && isSourPlum(stack) && stack.getUseAnimation() == UseAnim.DRINK) {
         int usedTicks = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
         if (usedTicks % 4 == 0) {
            playSourPlumSound(entity);
         }
      }

      if (entity instanceof Player player) {
         if (isIceTeaItem(stack)) {
            UUID playerId = player.getUUID();
            if (drinkingTicksMap.containsKey(playerId)) {
               int ticks = drinkingTicksMap.get(playerId);
               drinkingTicksMap.put(playerId, ticks + 1);
               if (ticks % 10 == 0 && !player.level().isClientSide()) {
                  player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ICE_TEA_EAT, SoundSource.PLAYERS, 0.5F, 1.0F);
               }
            }
         }

         if (isSourPlum(stack)) {
            UUID playerId = player.getUUID();
            if (drinkingTicksMap.containsKey(playerId)) {
               int ticks = drinkingTicksMap.get(playerId);
               drinkingTicksMap.put(playerId, ticks + 1);
               if (ticks % 10 == 0 && !player.level().isClientSide()) {
                  player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SOUR_PLUM_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);
               }
            }
         }
      }
   }

   public static void onFinishDrinking(LivingEntity entity, ItemStack itemStack) {
      if (entity instanceof Player player) {
         if (isIceTeaItem(itemStack)) {
            UUID playerId = player.getUUID();
            drinkingTicksMap.remove(playerId);
            if (!player.level().isClientSide()) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ICE_TEA_EAT, SoundSource.PLAYERS, 1.3F, 0.7F);
            }
         }

         if (isSourPlum(itemStack)) {
            UUID playerId = player.getUUID();
            drinkingTicksMap.remove(playerId);
            if (!player.level().isClientSide()) {
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SOUR_PLUM_DRINK, SoundSource.PLAYERS, 1.3F, 0.7F);
            }
         }
      }
   }

   public static void onStopDrinking(LivingEntity entity, ItemStack itemStack) {
      if (entity instanceof Player player) {
         if (isIceTeaItem(itemStack)) {
            drinkingTicksMap.remove(player.getUUID());
         }

         if (isSourPlum(itemStack)) {
            drinkingTicksMap.remove(player.getUUID());
         }
      }
   }

   private static boolean isCoolIceTea(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
         return COOL_ICE_TEA_ITEM.equals(itemId);
      }
   }

   private static void playCoolIceTeaSound(LivingEntity entity) {
      entity.level()
         .playSound((Player)entity, entity.getX(), entity.getY(), entity.getZ(), ModSounds.COOL_ICE_TEA_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);
   }

   private static boolean isIceTeaItem(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return false;
      } else {
         String className = itemStack.getItem().getClass().getName();
         return className.equals("com.starmeow.smc.items.IceTea") || itemStack.getItem().getDescriptionId().toLowerCase().contains("ice_tea");
      }
   }

   private static boolean isSourPlum(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
         return SOUR_PLUM_ITEM.equals(itemId);
      }
   }

   private static void playSourPlumSound(LivingEntity entity) {
      entity.level().playSound((Player)entity, entity.getX(), entity.getY(), entity.getZ(), ModSounds.SOUR_PLUM_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);
   }
}
