package com.bmt.kaleidoscope_world_liquor.effect;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import java.util.Random;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DoubleDamageEffect extends MobEffect {
   private static final Random RANDOM = new Random();
   private static final float BASE_CHANCE = 0.2F;
   private static final float CHANCE_PER_LEVEL = 0.2F;
   private static final float DAMAGE_MULTIPLIER = 2.0F;

   public DoubleDamageEffect() {
      super(MobEffectCategory.BENEFICIAL, 16729344);
   }

   public boolean isInstantenous() {
      return false;
   }

   public boolean applyEffectTick(LivingEntity entity, int amplifier) {
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return true;
   }

   /**
    * Applies the double-damage chance logic and returns the (possibly doubled) damage amount.
    * Called by damage event handlers; no event subscription is done here.
    */
   public static float applyDoubleDamage(LivingEntity attacker, DamageSource source, float amount) {
      if (attacker == null || attacker.level().isClientSide()) {
         return amount;
      }

      MobEffectInstance effectInstance = attacker.getEffect(ModEffects.DOUBLE_DAMAGE_EFFECT);
      if (effectInstance == null) {
         return amount;
      }

      int amplifier = effectInstance.getAmplifier();
      float chance = BASE_CHANCE + amplifier * CHANCE_PER_LEVEL;
      if (RANDOM.nextFloat() >= chance) {
         return amount;
      }

      float finalDamage = amount * DAMAGE_MULTIPLIER;
      Level level = attacker.level();
      level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.5F);
      Entity target = source.getEntity() != null ? source.getEntity() : attacker;
      if (attacker instanceof Player player) {
         player.crit(target);
      } else {
         for (int i = 0; i < 5; i++) {
            double x = target.getX() + RANDOM.nextDouble() * 2.0 - 1.0;
            double y = target.getY() + target.getBbHeight() / 2.0F;
            double z = target.getZ() + RANDOM.nextDouble() * 2.0 - 1.0;
            level.addParticle(ParticleTypes.CRIT, x, y, z, 0.0, -0.1, 0.0);
         }
      }

      return finalDamage;
   }
}
