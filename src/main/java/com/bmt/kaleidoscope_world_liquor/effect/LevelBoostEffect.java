package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class LevelBoostEffect extends MobEffect {
   private static final int BASE_LEVELS = 3;
   private static final int LEVELS_PER_AMPLIFIER = 3;

   public LevelBoostEffect(int color) {
      super(MobEffectCategory.HARMFUL, color);
   }

   public boolean isInstantenous() {
      return true;
   }

   public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health) {
      this.addExperienceLevels(livingEntity, amplifier);
   }

   public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
      this.addExperienceLevels(livingEntity, amplifier);
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration == 1;
   }

   private void addExperienceLevels(LivingEntity entity, int amplifier) {
      if (!entity.level().isClientSide()) {
         if (entity instanceof Player player) {
            int totalLevels = 3 + amplifier * 3;
            player.giveExperienceLevels(totalLevels);
         }
      }
   }
}
