package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ContinuousHealEffect extends MobEffect {
   public ContinuousHealEffect() {
      super(MobEffectCategory.BENEFICIAL, 16711680);
   }

   public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
      if (!livingEntity.level().isClientSide() && !(livingEntity.getHealth() >= livingEntity.getMaxHealth())) {
         livingEntity.heal(amplifier + 1);
         return true;
      } else {
         return true;
      }
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return true;
   }
}
