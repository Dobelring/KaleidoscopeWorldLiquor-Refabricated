package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RespawnEffect extends MobEffect {
   public RespawnEffect() {
      super(MobEffectCategory.NEUTRAL, 8900331);
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
         if (entity instanceof ServerPlayer serverPlayer) {
            level.playSound(
               null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F
            );
            boolean keepInventory = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            DimensionTransition transition = serverPlayer.findRespawnPositionAndUseSpawnBlock(keepInventory, DimensionTransition.DO_NOTHING);
            ServerLevel targetLevel = transition.newLevel();
            Vec3 targetPos = transition.pos();
            float yRot = transition.yRot();
            float xRot = transition.xRot();
            ResourceKey targetDim = targetLevel.dimension();
            if (level.dimension() == targetDim) {
               serverPlayer.teleportTo(targetPos.x, targetPos.y, targetPos.z);
               serverPlayer.setYRot(yRot);
               serverPlayer.setXRot(xRot);
            } else {
               serverPlayer.changeDimension(transition);
            }

            serverPlayer.fallDistance = 0.0F;
            serverPlayer.level().playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
         }
      }
   }
}
