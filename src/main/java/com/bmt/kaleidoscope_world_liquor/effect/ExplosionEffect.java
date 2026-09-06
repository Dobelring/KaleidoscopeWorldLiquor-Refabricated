package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import org.jetbrains.annotations.Nullable;

public class ExplosionEffect extends MobEffect {
   private static final float BASE_POWER = 3.0F;
   private static final float POWER_PER_AMPLIFIER = 1.0F;
   private static final boolean SET_FIRE = false;
   private static final ExplosionInteraction BLOCK_MODE = ExplosionInteraction.TNT;

   public ExplosionEffect(int color) {
      super(MobEffectCategory.HARMFUL, color);
   }

   public boolean isInstantenous() {
      return true;
   }

   public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health) {
      this.doExplosion(livingEntity, amplifier);
   }

   public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
      this.doExplosion(livingEntity, amplifier);
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration == 1;
   }

   private void doExplosion(LivingEntity entity, int amplifier) {
      Level level = entity.level();
      if (!level.isClientSide()) {
         float power = 3.0F + amplifier * 1.0F;
         level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), power, false, BLOCK_MODE);
      }
   }
}
