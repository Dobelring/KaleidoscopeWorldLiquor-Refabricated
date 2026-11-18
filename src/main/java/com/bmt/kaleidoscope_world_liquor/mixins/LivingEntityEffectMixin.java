package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.event.FriendsDrinkEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 干杯之歌跟踪钩子：效果挂上时通知登记表（移除靠服务端 tick 自清）。 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEffectMixin {
   @Inject(method = "onEffectAdded", at = @At("TAIL"))
   private void kaleidoscope$onEffectAdded(MobEffectInstance instance, Entity source, CallbackInfo ci) {
      FriendsDrinkEvent.onEffectAdded((Entity) (Object) this, instance);
   }
}