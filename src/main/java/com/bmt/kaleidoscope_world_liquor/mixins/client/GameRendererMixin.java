package com.bmt.kaleidoscope_world_liquor.mixins.client;

import com.bmt.kaleidoscope_world_liquor.client.event.CameraAnglesEvent;
import com.github.ysbbbbbb.kaleidoscopetavern.api.event.ViewportEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 反重力视角倒置（行为规格 §84）：tavern 的 CameraMixin 每帧 post 一次
 * {@link ViewportEvent.ComputeCameraAngles}，但只回写 yaw/pitch、丢弃 roll——
 * 本 mixin 在 {@code Camera.setup} 之后构造一个新事件、直接调用
 * {@link CameraAnglesEvent#onCameraAngles}（不 post，避免连带触发 tavern 的
 * 微醺监听造成双重 roll），再把 roll 应用到渲染 PoseStack。
 */
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            shift = At.Shift.AFTER
        )
    )
    private void kwl$applyCameraRoll(float tickDelta, long nanoTime, PoseStack poseStack, CallbackInfo ci) {
        // setup() 会覆盖相机旋转，所以要在其执行之后再应用 roll。
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles(
            this.mainCamera, tickDelta, this.mainCamera.getYRot(), this.mainCamera.getXRot(), 0.0F
        );
        CameraAnglesEvent.onCameraAngles(event);
        if (event.getRoll() != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(event.getRoll()));
        }
    }
}
