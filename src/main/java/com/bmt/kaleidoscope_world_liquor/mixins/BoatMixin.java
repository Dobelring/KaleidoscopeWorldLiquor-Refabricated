package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Boat.class})
public abstract class BoatMixin extends Entity {
    @Shadow
    private boolean inputUp;
    // 官方 1.1.12：船长的祝福重构——地形分档模型 → 扁平模型（基准 1.3 + 每级 0.2 封顶 +1.0，速度上限 1.2，刹车 0.85）
    private static final float BASE_MULTIPLIER = 1.3F;
    private static final float PER_LEVEL_BONUS = 0.2F;
    private static final float MAX_BONUS = 1.0F;
    private static final double MAX_SPEED = 1.2;
    private static final float BRAKE_FACTOR = 0.85F;

    public BoatMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = {"tick"},
        at = {@At("HEAD")}
    )
    private void kwl$applyBoatingMasterSpeed(CallbackInfo ci) {
        if (this.getControllingPassenger() instanceof Player player) {
            MobEffectInstance effect = player.getEffect(ModEffects.BOATING_MASTER_EFFECT);
            if (effect != null) {
                int amplifier = effect.getAmplifier();
                if (this.inputUp) {
                    float multiplier = BASE_MULTIPLIER + Math.min(amplifier * PER_LEVEL_BONUS, MAX_BONUS);
                    double x = this.getDeltaMovement().x * multiplier;
                    double z = this.getDeltaMovement().z * multiplier;
                    double currentSpeed = Math.sqrt(x * x + z * z);
                    if (currentSpeed > MAX_SPEED) {
                        double ratio = MAX_SPEED / currentSpeed;
                        x *= ratio;
                        z *= ratio;
                    }

                    this.setDeltaMovement(x, this.getDeltaMovement().y, z);
                } else {
                    double currentSpeedSq = this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z;
                    if (currentSpeedSq > 1.0E-4) {
                        this.setDeltaMovement(this.getDeltaMovement().x * BRAKE_FACTOR, this.getDeltaMovement().y, this.getDeltaMovement().z * BRAKE_FACTOR);
                    }
                }
            }
        }
    }
}
