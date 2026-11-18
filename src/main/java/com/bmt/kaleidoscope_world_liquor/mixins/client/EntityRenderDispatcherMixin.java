package com.bmt.kaleidoscope_world_liquor.mixins.client;

import com.bmt.kaleidoscope_world_liquor.client.FriendsDrinkClientState;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 官方 1.2.0：干杯之歌舞蹈渲染——受影响实体渲染时包一层旋转+压扁/拉伸姿态。
 */
@Mixin(EntityRenderDispatcher.class)
@Environment(EnvType.CLIENT)
public abstract class EntityRenderDispatcherMixin {

   @Inject(
      method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
         shift = At.Shift.BEFORE
      )
   )
   private void kaleidoscopeWorldLiquor$pushFriendsDrinkPose(
      Entity entity, double x, double y, double z, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci
   ) {
      poseStack.pushPose();
      applyFriendsDrinkPose(entity, partialTicks, poseStack);
   }

   @Inject(
      method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
         shift = At.Shift.AFTER
      )
   )
   private void kaleidoscopeWorldLiquor$popFriendsDrinkPose(
      Entity entity, double x, double y, double z, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci
   ) {
      poseStack.popPose();
   }

   private static void applyFriendsDrinkPose(Entity entity, float partialTicks, PoseStack poseStack) {
      if (ModEffects.OHH_YE_YE != null && FriendsDrinkClientState.isAffected(entity)) {
         double time = entity.level().getGameTime() + partialTicks;
         double progress = time / 20.0 * (Math.PI * 2);
         float sin = (float) Math.sin(2.0 * (progress % Math.PI));
         float yOffset = (float) (Math.tanh(2.0 * sin) / Math.tanh(2.0));
         float scaleY = 1.0F + yOffset * 0.25F;
         float scaleXZ = 1.0F - yOffset * 0.25F;
         float height = (float) Math.max(entity.getBbHeight(), entity.getBoundingBoxForCulling().getYsize());
         float halfHeight = height * 0.5F * scaleY;
         poseStack.translate(0.0F, halfHeight, 0.0F);
         poseStack.mulPose(Axis.YP.rotationDegrees((float) (progress * 90.0 % 360.0)));
         poseStack.translate(0.0F, -halfHeight, 0.0F);
         poseStack.scale(scaleXZ, scaleY, scaleXZ);
      }
   }
}
