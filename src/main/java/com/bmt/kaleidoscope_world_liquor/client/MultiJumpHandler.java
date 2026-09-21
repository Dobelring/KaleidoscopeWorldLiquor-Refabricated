package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

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

        if (this.canJump(player)
                && !player.onGround()
                && !this.jumpedLastTick
                && this.jumpCount > 0
                && player.getDeltaMovement().y < 0.0
                && player.input.keyPresses.jump()
                && !player.getAbilities().flying) {
            this.jumpCount--;
            player.jumpFromGround();
            player.resetFallDistance();
            this.jumpedLastTick = true;
        } else {
            this.jumpedLastTick = player.input.keyPresses.jump();
        }
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
