package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.hooks.CaptainGiftMixinHooks;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;

@Mixin({Entity.class})
public abstract class CaptainGiftMixin {
   @ModifyVariable(
      method = {"move"},
      ordinal = 1,
      at = @At(
         value = "MIXINEXTRAS:EXPRESSION",
         shift = Shift.AFTER
      )
   )
   @Definition(
      id = "collide",
      method = {"Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"}
   )
   @Expression({"? = ?.collide(?)"})
   public Vec3 captainGift$enableWaterWalking(Vec3 original) {
      return CaptainGiftMixinHooks.enableWaterWalking((Entity)(Object)this, original);
   }
}
