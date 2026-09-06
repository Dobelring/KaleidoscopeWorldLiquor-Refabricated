package com.bmt.kaleidoscope_world_liquor.effect;

import com.bmt.kaleidoscope_world_liquor.config.ModConfigs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CrazyEffect extends MobEffect {
   public CrazyEffect() {
      super(MobEffectCategory.HARMFUL, 16711935);
   }

   public boolean isInstantenous() {
      return true;
   }

   public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health) {
      this.performEffect(livingEntity, amplifier);
   }

   public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
      this.performEffect(livingEntity, amplifier);
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration == 1;
   }

   private void performEffect(LivingEntity entity, int amplifier) {
      Level level = entity.level();
      if (!level.isClientSide()) {
         BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
            if (effect != null && effect != this) {
               if (!(Boolean)ModConfigs.ENABLE_MODDED_EFFECTS.get()) {
                  ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                  if (effectId == null || !"minecraft".equals(effectId.getNamespace())) {
                     return;
                  }
               }

               MobEffectInstance instance = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), 200, amplifier, false, false);
               entity.addEffect(instance);
            }
         });
         level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
      }
   }
}
