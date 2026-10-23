package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/**
 * 多段跳客户端判定（1.20.1 ClientPlayerEntityMixinHooks 原样迁移）：
 * 落地/贴水重置跳数；跳跃键按下、下落中、未穿鞘翅且各项条件满足时消耗一次额外跳。
 */
public final class MultiJumpHandler {
    private int jumpCount = 0;
    private boolean jumpedLastTick = false;

    public void tickMovement(LocalPlayer player) {
        if (!player.hasEffect(ModEffects.MULTI_JUMP)) {
            this.jumpCount = 0;
            this.jumpedLastTick = false;
            return;
        }
        MobEffectInstance effect = player.getEffect(ModEffects.MULTI_JUMP);
        if (effect == null) {
            return;
        }
        int maxJumps = effect.getAmplifier() + 1;
        if (player.onGround() || player.isInWater()) {
            this.jumpCount = maxJumps;
        }

        if (this.canJump(player)) {
            // 官方 1.1.11：反重力下「下落」判定翻转——y 速度 > 0 才算下落
            boolean reverseGravity = player.hasEffect(ModEffects.REVERSE_GRAVITY);
            double velocityY = player.getDeltaMovement().y;
            boolean isFalling = reverseGravity ? velocityY > 0.0 : velocityY < 0.0;
            if (!player.onGround()
                    && !this.jumpedLastTick
                    && this.jumpCount > 0
                    && isFalling
                    && player.input.keyPresses.jump()
                    && !player.getAbilities().flying) {
                this.jumpCount--;
                // 官方 1.1.12：不再用原版跳跃（受多因素影响），改为定速起跳并显式吃跳跃提升加成
                double upwardsMotion = 0.5;
                if (player.hasEffect(MobEffects.JUMP_BOOST)) {
                    upwardsMotion += 0.1 * (player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1);
                }

                if (reverseGravity) {
                    upwardsMotion = -upwardsMotion;
                }

                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, upwardsMotion, motion.z);
                player.needsSync = true;
                player.resetFallDistance();
                this.jumpedLastTick = true;
                return;
            }
        }

        this.jumpedLastTick = player.input.keyPresses.jump();
    }

    private boolean wearingUsableElytra(LocalPlayer player) {
        return player.isFallFlying();
    }

    private boolean canJump(LocalPlayer player) {
        // 官方 1.1.12：跳跃提升不再排除多段跳（改为显式加成，见上方定速起跳）
        return !this.wearingUsableElytra(player)
                && !player.isInWater()
                && !player.isPassenger();
    }
}
