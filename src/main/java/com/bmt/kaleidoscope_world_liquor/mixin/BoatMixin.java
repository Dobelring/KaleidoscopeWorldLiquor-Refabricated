package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 划船大师（boating_master）：官方 1.1.11 改扁平模型——
 * 基础倍率 1.3 + 每级 min(0.2, 1.0)、封顶最高速度 1.2、松键刹车系数 0.85（速度平方 &gt; 1e-4 时），
 * <b>删除</b>原先按水下/冰面/陆地的分档加成。
 * <p>照 1.21.1 的 BoatMixin（1.21.11 船重构为 AbstractBoat 子类，inputUp 为私有，
 * 用 @Shadow 访问——mixin 目标 AbstractBoat 的 tick 对木船/竹筏统一生效）。
 */
@Mixin(AbstractBoat.class)
public abstract class BoatMixin {
    @Shadow private boolean inputUp;

    @Inject(method = "tick", at = @At("HEAD"))
    private void kwl$boatingMaster(CallbackInfo ci) {
        AbstractBoat boat = (AbstractBoat) (Object) this;
        if (boat.getControllingPassenger() instanceof Player player) {
            var effect = player.getEffect(ModEffects.BOATING_MASTER);
            if (effect == null) {
                return;
            }
            int amplifier = effect.getAmplifier();
            if (this.inputUp) {
                float multiplier = 1.3F + Math.min(amplifier * 0.2F, 1.0F);
                double x = boat.getDeltaMovement().x * multiplier;
                double z = boat.getDeltaMovement().z * multiplier;
                double currentSpeed = Math.sqrt(x * x + z * z);
                if (currentSpeed > 1.2) {
                    double ratio = 1.2 / currentSpeed;
                    x *= ratio;
                    z *= ratio;
                }
                boat.setDeltaMovement(x, boat.getDeltaMovement().y, z);
            } else {
                double currentSpeedSq = boat.getDeltaMovement().x * boat.getDeltaMovement().x
                        + boat.getDeltaMovement().z * boat.getDeltaMovement().z;
                if (currentSpeedSq > 1.0E-4) {
                    boat.setDeltaMovement(boat.getDeltaMovement().x * 0.85F,
                            boat.getDeltaMovement().y, boat.getDeltaMovement().z * 0.85F);
                }
            }
        }
    }
}
