package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 反重力（reverse_gravity）：tick 持续上升、跳跃反向、贴天花板判定、摔落免疫；
 * 多段跳（multi_jump）：摔落伤害豁免（原 MultiJumpFallDamageMixin）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void kwl$applyReverseGravity(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            if (!player.getAbilities().flying && !player.isInWater() && !player.isInLava()) {
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.add(0.0, 0.16, 0.0));
            }
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void kwl$reverseJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(new Vec3(motion.x, -0.42, motion.z));
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kwl$updateCeilingOnGround(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            boolean isOnCeiling = !entity.level().noCollision(entity.getBoundingBox().move(0.0, 0.1, 0.0));
            entity.setOnGround(isOnCeiling);
        }
    }

    @Inject(method = "calculateFallDamage", at = @At("HEAD"), cancellable = true)
    private void kwl$cancelFallDamage(double fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player && entity.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            cir.setReturnValue(0);
        }
        if (entity.hasEffect(ModEffects.MULTI_JUMP)) {
            cir.setReturnValue(0);
        }
    }
}
