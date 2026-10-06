package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 多段跳客户端判定（官方 1.1.11 同款结构）：
 * 落地/贴水重置跳数；{@code canJump} 通过后再判「是否在下落」——
 * 反重力下「下落」= 纵向速度为正（朝天花板掉），普通时仍为负。
 * 尾部统一同步 {@code jumpedLastTick = 跳跃键}（官方把 else 分支挪到 if 外）。
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

        boolean jumping = player.input.keyPresses.jump();
        if (this.canJump(player)) {
            boolean reverseGravity = player.hasEffect(ModEffects.REVERSE_GRAVITY);
            double velocityY = player.getDeltaMovement().y;
            boolean isFalling = reverseGravity ? velocityY > 0.0 : velocityY < 0.0;
            if (!player.onGround()
                    && !this.jumpedLastTick
                    && this.jumpCount > 0
                    && isFalling
                    && jumping
                    && !player.getAbilities().flying) {
                this.jumpCount--;
                player.jumpFromGround();
                player.resetFallDistance();
                this.jumpedLastTick = true;
                return;
            }
        }

        this.jumpedLastTick = jumping;
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
