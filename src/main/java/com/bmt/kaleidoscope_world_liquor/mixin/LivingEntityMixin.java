package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 反重力（reverse_gravity）：tick 持续上升（漂浮禁用、缓降减速）、跳跃反向、贴天花板判定、
 * 摔落免疫、反重力×漂浮 → 下落；多段跳（multi_jump）：摔落伤害豁免。
 * <p>官方 1.1.11 语义；26.3 的 {@code Entity#hasImpulse} 字段已不存在（原版 jumpFromGround
 * 已重写为 {@code max(jumpPower, y)} + 疾跑分量），故该行省略。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void kwl$applyReverseGravity(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            if (!player.getAbilities().flying && !player.isInWater() && !player.isInLava()
                    && !player.hasEffect(MobEffects.LEVITATION)) {
                Vec3 motion = player.getDeltaMovement();
                // 官方 1.1.11：带缓降时上升推进 0.09（否则 0.16）
                double boost = player.hasEffect(MobEffects.SLOW_FALLING) ? 0.09 : 0.16;
                player.setDeltaMovement(motion.add(0.0, boost, 0.0));
            }
        }
    }

    /** 官方 1.1.11：反重力下再吃漂浮 → 按漂浮等级向下推进（原本漂浮是上升的）。 */
    @Inject(method = "travel", at = @At("TAIL"))
    private void kwl$reverseLevitationTravel(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player
                && player.hasEffect(ModEffects.REVERSE_GRAVITY)
                && player.hasEffect(MobEffects.LEVITATION)
                && !player.getAbilities().flying
                && !player.isInWater() && !player.isInLava()) {
            int amplifier = player.getEffect(MobEffects.LEVITATION).getAmplifier();
            double downSpeed = -0.05 * (amplifier + 1);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, downSpeed, motion.z);
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void kwl$reverseJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            float footFactor = player.level().getBlockState(player.blockPosition()).getBlock().getJumpFactor();
            float aboveFactor = player.level().getBlockState(player.blockPosition().above()).getBlock().getJumpFactor();
            float blockJumpFactor = footFactor == 1.0F ? aboveFactor : footFactor;
            float jumpPower = 0.42F * blockJumpFactor + player.getJumpBoostPower();
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, -jumpPower, motion.z);
            if (player.isSprinting()) {
                float yaw = player.getYRot() * (float) (Math.PI / 180.0);
                player.setDeltaMovement(player.getDeltaMovement().add(
                        -Mth.sin(yaw) * 0.2F, 0.0, Mth.cos(yaw) * 0.2F));
            }
            // 原 Forge：ForgeHooks.onLivingJump(player)——Fabric 无该事件钩子，无订阅方等价空操作，省略。
            // 原 hasImpulse = true 在 26.3 无对应字段，省略。
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
