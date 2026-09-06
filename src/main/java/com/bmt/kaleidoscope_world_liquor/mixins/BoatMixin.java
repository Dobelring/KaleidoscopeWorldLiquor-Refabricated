package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Boat.class})
public abstract class BoatMixin extends Entity {
   @Shadow
   protected boolean inputUp;
   private static final float LAND_BASE_BONUS = 0.3F;
   private static final float WATER_BASE_BONUS = 0.12F;
   private static final float ICE_BASE_BONUS = 0.05F;
   private static final float PER_LEVEL_SPEED_BONUS = 0.15F;
   private static final double LAND_MAX_SPEED = 1.2;
   private static final double WATER_MAX_SPEED = 0.9;
   private static final double ICE_MAX_SPEED = 0.7;
   private static final float LAND_BRAKE_FACTOR = 0.85F;
   private static final float WATER_BRAKE_FACTOR = 0.9F;
   private static final float ICE_BRAKE_FACTOR = 0.95F;

   public BoatMixin(EntityType<?> entityType, Level level) {
      super(entityType, level);
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onBoatTick(CallbackInfo ci) {
      if (this.getControllingPassenger() instanceof Player player) {
         if (player.hasEffect(ModEffects.BOATING_MASTER_EFFECT)) {
            int amplifier = player.getEffect(ModEffects.BOATING_MASTER_EFFECT).getAmplifier();
            BlockPos belowPos = this.blockPosition().below();
            BlockState belowState = this.level().getBlockState(belowPos);
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
               double x = this.getDeltaMovement().x * speedMultiplier;
               double z = this.getDeltaMovement().z * speedMultiplier;
               double currentSpeed = Math.sqrt(x * x + z * z);
               if (currentSpeed > maxSpeed) {
                  double ratio = maxSpeed / currentSpeed;
                  x *= ratio;
                  z *= ratio;
               }

               this.setDeltaMovement(x, this.getDeltaMovement().y, z);
            } else {
               double currentSpeed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().z * this.getDeltaMovement().z);
               if (currentSpeed > 0.01) {
                  double x = this.getDeltaMovement().x * brakeFactor;
                  double z = this.getDeltaMovement().z * brakeFactor;
                  this.setDeltaMovement(x, this.getDeltaMovement().y, z);
               }
            }
         }
      }
   }
}
