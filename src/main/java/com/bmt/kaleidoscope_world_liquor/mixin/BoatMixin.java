package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 划船大师（boating_master）：玩家驾驶船时按船底方块类型加速，
 * 水面/冰面/陆地三档基础加成 + 每级 0.15，并封顶最高速度；松键时刹车。
 * 照 1.21.1 的 BoatMixin（1.21.11 船重构为 AbstractBoat 子类，inputUp 为私有，
 * 用 @Shadow 访问——mixin 目标 AbstractBoat 的 tick 对木船/竹筏统一生效）。
 */
@Mixin(AbstractBoat.class)
public abstract class BoatMixin {
    @Shadow private boolean inputUp;

    @Inject(method = "tick", at = @At("HEAD"))
    private void kwl$boatingMaster(CallbackInfo ci) {
        AbstractBoat boat = (AbstractBoat) (Object) this;
        if (boat.getControllingPassenger() instanceof Player player
                && player.hasEffect(ModEffects.BOATING_MASTER)) {
            int amplifier = player.getEffect(ModEffects.BOATING_MASTER).getAmplifier();
            BlockPos belowPos = boat.blockPosition().below();
            BlockState belowState = boat.level().getBlockState(belowPos);
            float baseBonus;
            double maxSpeed;
            float brakeFactor;
            if (belowState.is(Blocks.WATER)) {
                baseBonus = 0.12F;
                maxSpeed = 0.9;
                brakeFactor = 0.9F;
            } else if (!belowState.is(Blocks.ICE) && !belowState.is(Blocks.PACKED_ICE) && !belowState.is(Blocks.BLUE_ICE)) {
                baseBonus = 0.3F;
                maxSpeed = 1.2;
                brakeFactor = 0.85F;
            } else {
                baseBonus = 0.05F;
                maxSpeed = 0.7;
                brakeFactor = 0.95F;
            }

            float speedMultiplier = 1.0F + baseBonus + amplifier * 0.15F;
            if (this.inputUp) {
                double x = boat.getDeltaMovement().x * speedMultiplier;
                double z = boat.getDeltaMovement().z * speedMultiplier;
                double currentSpeed = Math.sqrt(x * x + z * z);
                if (currentSpeed > maxSpeed) {
                    double ratio = maxSpeed / currentSpeed;
                    x *= ratio;
                    z *= ratio;
                }
                boat.setDeltaMovement(x, boat.getDeltaMovement().y, z);
            } else {
                double currentSpeed = Math.sqrt(boat.getDeltaMovement().x * boat.getDeltaMovement().x
                        + boat.getDeltaMovement().z * boat.getDeltaMovement().z);
                if (currentSpeed > 0.01) {
                    boat.setDeltaMovement(boat.getDeltaMovement().x * brakeFactor,
                            boat.getDeltaMovement().y, boat.getDeltaMovement().z * brakeFactor);
                }
            }
        }
    }
}
