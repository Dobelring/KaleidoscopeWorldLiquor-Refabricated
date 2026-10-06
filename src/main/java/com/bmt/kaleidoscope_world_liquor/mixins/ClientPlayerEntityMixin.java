package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.hooks.ClientPlayerEntityMixinHooks;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public abstract class ClientPlayerEntityMixin {
   @Unique
   private final ClientPlayerEntityMixinHooks kaleidoscope_world_liquor$hooks = new ClientPlayerEntityMixinHooks();

   @Inject(
      method = {"aiStep"},
      at = {@At("HEAD")},
      remap = false
   )
   private void multiJump$tickMovement(CallbackInfo info) {
      this.kaleidoscope_world_liquor$hooks.tickMovement((LocalPlayer)(Object)this);
   }

   // 官方 1.1.11：反重力下创造飞行上下键反转修复——aiStep 内 LocalPlayer#setDeltaMovement 纵向分量取反
   @WrapOperation(
      method = {"aiStep"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
      )}
   )
   private void kaleidoscope$reverseCreativeFlyingThrust(LocalPlayer player, Vec3 desired, Operation<Void> original) {
      if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
         Vec3 current = player.getDeltaMovement();
         double dy = desired.y - current.y;
         desired = desired.add(0.0, -2.0 * dy, 0.0);
      }

      original.call(player, desired);
   }

   @Inject(
      method = {"serverAiStep"},
      at = {@At("RETURN")}
   )
   private void kaleidoscope$invertStrafe(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
         LivingEntity living = (LivingEntity)(Object)this;
         living.xxa = -living.xxa;
      }
   }
}
