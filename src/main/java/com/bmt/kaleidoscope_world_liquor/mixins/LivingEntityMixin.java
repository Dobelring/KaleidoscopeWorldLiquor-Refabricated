package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {
   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void kaleidoscope$applyReverseGravity(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (entity instanceof Player player) {
         if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            if (!player.getAbilities().flying) {
               if (!player.isInWater() && !player.isInLava()) {
                  Entity self = (Entity)(Object)this;
                  if (!player.hasEffect(MobEffects.LEVITATION)) {
                     Vec3 motion = self.getDeltaMovement();
                     double boost = player.hasEffect(MobEffects.SLOW_FALLING) ? 0.09 : 0.16;
                     self.setDeltaMovement(motion.add(0.0, boost, 0.0));
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = {"travel"},
      at = {@At("TAIL")}
   )
   private void kaleidoscope$reverseLevitationTravel(Vec3 travelVector, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (entity instanceof Player player) {
         if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            if (player.hasEffect(MobEffects.LEVITATION)) {
               if (!player.getAbilities().flying) {
                  if (!player.isInWater() && !player.isInLava()) {
                     int amplifier = player.getEffect(MobEffects.LEVITATION).getAmplifier();
                     double downSpeed = -0.05 * (amplifier + 1);
                     Entity self = (Entity)(Object)this;
                     Vec3 motion = self.getDeltaMovement();
                     self.setDeltaMovement(motion.x, downSpeed, motion.z);
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = {"jumpFromGround"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kaleidoscope$reverseJump(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (entity instanceof Player player) {
         if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            float footFactor = player.level().getBlockState(player.blockPosition()).getBlock().getJumpFactor();
            float aboveFactor = player.level().getBlockState(player.blockPosition().above()).getBlock().getJumpFactor();
            float blockJumpFactor = footFactor == 1.0F ? aboveFactor : footFactor;
            float jumpPower = 0.42F * blockJumpFactor + player.getJumpBoostPower();
            Entity self = (Entity)(Object)this;
            Vec3 motion = self.getDeltaMovement();
            self.setDeltaMovement(motion.x, -jumpPower, motion.z);
            if (player.isSprinting()) {
               float f = player.getYRot() * (float) (Math.PI / 180.0);
               self.setDeltaMovement(self.getDeltaMovement().add(-Mth.sin(f) * 0.2F, 0.0, Mth.cos(f) * 0.2F));
            }

            player.hasImpulse = true;

            // 原 Forge：ForgeHooks.onLivingJump(player)——Fabric 无该事件钩子，无订阅方等价空操作，省略
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void kaleidoscope$updateCeilingOnGround(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (entity instanceof Player player) {
         if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            Entity self = (Entity)(Object)this;
            boolean isOnCeiling = !self.level().noCollision(self.getBoundingBox().move(0.0, 0.1, 0.0));
            entity.setOnGround(isOnCeiling);
         }
      }
   }

   @Inject(
      method = {"calculateFallDamage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kaleidoscope$cancelInvertedFallDamage(float pFallDistance, float pDamageMultiplier, CallbackInfoReturnable<Integer> cir) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (entity instanceof Player) {
         if (entity.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            cir.setReturnValue(0);
         }
      }
   }
}
