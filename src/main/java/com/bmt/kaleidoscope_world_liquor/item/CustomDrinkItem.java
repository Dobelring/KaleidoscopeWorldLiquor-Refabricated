package com.bmt.kaleidoscope_world_liquor.item;

import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;

/**
 * 特殊饮品：饮用/食用音效直接由物品提供，取代 1.1.8 那套 LivingEntityUseItemEvent 事件驱动实现。
 */
public abstract class CustomDrinkItem extends DrinkBlockItem {
   public CustomDrinkItem(Block block) {
      super(block);
   }

   protected abstract SoundEvent getCustomSound();

   @Override
   public SoundEvent getDrinkingSound() {
      return this.getCustomSound();
   }

   @Override
   public SoundEvent getEatingSound() {
      return this.getCustomSound();
   }

   public static class CoolTea extends CustomDrinkItem {
      public CoolTea(Block block) {
         super(block);
      }

      @Override
      protected SoundEvent getCustomSound() {
         return ModSounds.COOL_ICE_TEA_DRINK;
      }
   }

   public static class IceTea extends CustomDrinkItem {
      public IceTea(Block block) {
         super(block);
      }

      @Override
      protected SoundEvent getCustomSound() {
         return ModSounds.ICE_TEA_EAT;
      }
   }

   public static class SourPlum extends CustomDrinkItem {
      public SourPlum(Block block) {
         super(block);
      }

      @Override
      protected SoundEvent getCustomSound() {
         return ModSounds.SOUR_PLUM_DRINK;
      }
   }
}
