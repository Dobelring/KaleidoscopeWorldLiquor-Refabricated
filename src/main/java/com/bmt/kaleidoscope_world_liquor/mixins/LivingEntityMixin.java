package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
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
                  Vec3 motion = self.getDeltaMovement();
                  self.setDeltaMovement(motion.add(0.0, 0.16, 0.0));
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
            Entity self = (Entity)(Object)this;
            Vec3 motion = self.getDeltaMovement();
            self.setDeltaMovement(new Vec3(motion.x, -0.42, motion.z));
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
