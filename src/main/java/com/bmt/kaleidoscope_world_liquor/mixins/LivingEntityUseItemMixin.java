package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.event.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityUseItemMixin {
   @Inject(
      method = {"startUsingItem"},
      at = {@At("TAIL")}
   )
   private void kaleidoscope$onStartDrinking(InteractionHand hand, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      ItemStack stack = entity.getUseItem();
      SoundEvents.onStartDrinking(entity, stack);
   }

   @Inject(
      method = {"updatingUsingItem"},
      at = {@At("TAIL")}
   )
   private void kaleidoscope$onTickDrinking(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      ItemStack stack = entity.getUseItem();
      SoundEvents.onTickDrinking(entity, stack);
   }

   @Inject(
      method = {"completeUsingItem"},
      at = {@At("HEAD")}
   )
   private void kaleidoscope$onFinishDrinking(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      ItemStack stack = entity.getUseItem();
      SoundEvents.onFinishDrinking(entity, stack);
   }

   @Inject(
      method = {"releaseUsingItem"},
      at = {@At("HEAD")}
   )
   private void kaleidoscope$onStopDrinking(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      ItemStack stack = entity.getUseItem();
      SoundEvents.onStopDrinking(entity, stack);
   }
}
