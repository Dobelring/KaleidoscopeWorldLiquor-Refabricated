package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 反重力：把相机视线点移到脚下（1.21.11 setup 签名 Level/Entity/ZZF）。
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3 position;
    @Shadow private org.joml.Quaternionf rotation;

    @Inject(method = "setup", at = @At("TAIL"))
    private void kwl$adjustEyePosition(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity focusedEntity,
                                       boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
        if (focusedEntity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            double eyeHeight = player.getEyeHeight();
            double bbHeight = player.getBbHeight();
            double offset = bbHeight - 2.0 * eyeHeight;
            // 只偏移相机位置（保留原相机逻辑），并加 540.35° 滚转实现倒置视角
            this.position = new Vec3(this.position.x, this.position.y + offset, this.position.z);
            this.rotation.rotateZ((float) Math.toRadians(540.3539364174444));
        }
    }
}
