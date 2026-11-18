package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.client.FriendsDrinkClientState;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 官方 1.2.0：干杯之歌舞蹈渲染的 26.x 近似——官方 1.21.1 走 EntityRenderDispatcher
 * pose 包裹，26.x 渲染改 render-state/submit 管线，改为在 extractRenderState 末尾
 * 调 bodyRot 旋转 + scale 缩放（无 XZ/不对称挤压，视觉近似）。
 */
@Mixin(LivingEntityRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class FriendsDrinkPoseMixin {
   @Inject(
      method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
      at = @At("TAIL")
   )
   private void kaleidoscopeWorldLiquor$applyFriendsDrinkPose(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
      if (ModEffects.OHH_YE_YE != null && FriendsDrinkClientState.isAffected(entity)) {
         double time = entity.level().getGameTime() + partialTicks;
         double progress = time / 20.0 * (Math.PI * 2);
         float sin = (float) Math.sin(2.0 * (progress % Math.PI));
         float yOffset = (float) (Math.tanh(2.0 * sin) / Math.tanh(2.0));
         state.scale *= 1.0F + yOffset * 0.25F;
         state.bodyRot += (float) (progress * 90.0 % 360.0);
      }
   }
}
