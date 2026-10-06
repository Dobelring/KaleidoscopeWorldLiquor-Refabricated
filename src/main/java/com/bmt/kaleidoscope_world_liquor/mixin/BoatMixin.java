package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 船长的祝福（boating_master）。
 * <p>
 * 官方 1.1.11：**扁平模型**——{@code multiplier = 1.3 + min(amp*0.2, 1.0)}、
 * 速度上限 1.2、松键刹车 0.85（条件 {@code speedSq > 1.0E-4}），
 * 删除原先按船底方块（水面/冰面/陆地）分档的逻辑。
 * <p>
 * 26.x：1.21.11 船重构为 AbstractBoat 子类，{@code inputUp} 为私有，用 @Shadow 访问
 * ——mixin 目标 AbstractBoat 的 tick 对木船/竹筏统一生效。
 */
@Mixin(AbstractBoat.class)
public abstract class BoatMixin {
    private static final float BASE_MULTIPLIER = 1.3F;
    private static final float PER_LEVEL_BONUS = 0.2F;
    private static final float MAX_BONUS = 1.0F;
    private static final double MAX_SPEED = 1.2;
    private static final float BRAKE_FACTOR = 0.85F;

    @Shadow
    private boolean inputUp;

    @Inject(method = "tick", at = @At("HEAD"))
    private void kwl$applyBoatingMasterSpeed(CallbackInfo ci) {
        AbstractBoat boat = (AbstractBoat) (Object) this;
        if (!(boat.getControllingPassenger() instanceof Player player)) {
            return;
        }
        MobEffectInstance effect = player.getEffect(ModEffects.BOATING_MASTER);
        if (effect == null) {
            return;
        }
        int amplifier = effect.getAmplifier();
        if (this.inputUp) {
            float multiplier = BASE_MULTIPLIER + Math.min(amplifier * PER_LEVEL_BONUS, MAX_BONUS);
            double x = boat.getDeltaMovement().x * multiplier;
            double z = boat.getDeltaMovement().z * multiplier;
            double currentSpeed = Math.sqrt(x * x + z * z);
            if (currentSpeed > MAX_SPEED) {
                double ratio = MAX_SPEED / currentSpeed;
                x *= ratio;
                z *= ratio;
            }
            boat.setDeltaMovement(x, boat.getDeltaMovement().y, z);
        } else {
            double currentSpeedSq = boat.getDeltaMovement().x * boat.getDeltaMovement().x
                    + boat.getDeltaMovement().z * boat.getDeltaMovement().z;
            if (currentSpeedSq > 1.0E-4) {
                boat.setDeltaMovement(boat.getDeltaMovement().x * BRAKE_FACTOR,
                        boat.getDeltaMovement().y, boat.getDeltaMovement().z * BRAKE_FACTOR);
            }
        }
    }
}
