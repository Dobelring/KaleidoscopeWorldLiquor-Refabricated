package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 多段跳客户端判定（1.20.1 ClientPlayerEntityMixinHooks 原样迁移）：
 * 落地/贴水重置跳数；跳跃键按下、下落中、未穿鞘翅且各项条件满足时消耗一次额外跳。
 * <p>
 * 官方 1.1.11：结构改为「先判 canJump 块，尾部统一 jumpedLastTick = input.jumping」；
 * 反重力下「下落」方向取反（velocityY > 0 才算下落）。
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
            // 官方 1.1.11：反重力下角色是「向上飘」，下落判定取反
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
                player.jumpFromGround();
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
        return !this.wearingUsableElytra(player)
                && !player.isInWater()
                && !player.isPassenger()
                && !player.hasEffect(MobEffects.JUMP_BOOST);
    }
}
