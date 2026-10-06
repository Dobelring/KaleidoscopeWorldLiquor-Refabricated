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
 * 反重力（reverse_gravity）：tick 持续上升、跳跃反向、贴天花板判定、摔落免疫；
 * 多段跳（multi_jump）：摔落伤害豁免（原 MultiJumpFallDamageMixin）。
 * <p>
 * 官方 1.1.11：
 * - 上升推进加 LEVITATION 守卫，SLOW_FALLING 时 0.09（否则 0.16）；
 * - 新增 travel TAIL：反重力 × 漂浮 → 强制下压 {@code -0.05*(amp+1)}；
 * - jumpFromGround：{@code 0.42*方块跳跃因子 + getJumpBoostPower()}，疾跑补
 *   {@code (-sin*0.2, 0, cos*0.2)}，置 needsSync 后 cancel。
 *   （原 Forge 的 ForgeHooks.onLivingJump 无订阅方，Fabric 等价空操作，省略。）
 * <p>
 * 26.x API 差异：原版 {@code hasImpulse} 字段已改名 {@code needsSync}；
 * {@code Mth.sin/cos} 收 double；方块跳跃因子用 {@code Block#getJumpFactor()}。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void kwl$applyReverseGravity(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            if (!player.getAbilities().flying && !player.isInWater() && !player.isInLava()) {
                // 官方 1.1.11：漂浮（LEVITATION）时不再叠加上升推力
                if (!player.hasEffect(MobEffects.LEVITATION)) {
                    Vec3 motion = player.getDeltaMovement();
                    double boost = player.hasEffect(MobEffects.SLOW_FALLING) ? 0.09 : 0.16;
                    player.setDeltaMovement(motion.add(0.0, boost, 0.0));
                }
            }
        }
    }

    /** 官方 1.1.11：反重力 × 漂浮 → 按漂浮等级下压，抵消原版漂浮上升。 */
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
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, downSpeed, motion.z);
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void kwl$reverseJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            // 官方 1.1.11：jumpPower = 0.42 * 方块跳跃因子 + 跳跃加成
            // 方块因子取脚下，若为 1.0 则取上方（与原版 getBlockJumpFactor 语义一致）
            float footFactor = player.level().getBlockState(player.blockPosition()).getBlock().getJumpFactor();
            float aboveFactor = player.level().getBlockState(player.blockPosition().above()).getBlock().getJumpFactor();
            float blockJumpFactor = footFactor == 1.0F ? aboveFactor : footFactor;
            float jumpPower = 0.42F * blockJumpFactor + player.getJumpBoostPower();

            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, -jumpPower, motion.z);
            if (player.isSprinting()) {
                float yawRadians = player.getYRot() * (float) (Math.PI / 180.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                        -Mth.sin(yawRadians) * 0.2F, 0.0, Mth.cos(yawRadians) * 0.2F));
            }

            // 26.x：原版 hasImpulse 已改名 needsSync（同语义：把该实体运动状态发给客户端）
            player.needsSync = true;

            // 原 Forge：ForgeHooks.onLivingJump(player)——Fabric 无该事件钩子，
            // 官方侧无订阅方，等价空操作，省略。
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
