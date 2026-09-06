package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class DamageEvents {
   public static void register() {
      ServerLivingEntityEvents.ALLOW_DAMAGE.register(DamageEvents::onLivingDamage);
   }

   private static boolean onLivingDamage(LivingEntity entity, DamageSource source, float amount) {
      if (entity.level().isClientSide) {
         return true;
      }

      if (entity.hasEffect(ModEffects.TEQUILA_EFFECT)) {
         int amplifier = entity.getEffect(ModEffects.TEQUILA_EFFECT).getAmplifier();
         float maxDamagePercent = 0.4F - amplifier * 0.05F;
         if (maxDamagePercent < 0.05F) {
            maxDamagePercent = 0.05F;
         }

         float maxHealth = entity.getMaxHealth();
         float maxAllowedDamage = maxHealth * maxDamagePercent;
         if (amount > maxAllowedDamage) {
            entity.hurt(source, maxAllowedDamage);
            return false;
         }
      }
      return true;
   }
}
