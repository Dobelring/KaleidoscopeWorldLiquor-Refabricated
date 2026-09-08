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
 * 反重力：把相机视线点移到脚下（26.1.2 Camera.setup 已删除）。
 * 注入 alignWithEntity TAIL：rotation 在此刚由 setRotation 重建并置矩阵脏标记，
 * 随后 update 内部的 getViewRotationMatrix 缓存才会带上我们的滚转；
 * 若注入 update TAIL 则矩阵缓存已生成，修改不生效（26.1.2 矩阵缓存机制）。
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Vec3 position;
    @Shadow private org.joml.Quaternionf rotation;

    @Shadow public abstract net.minecraft.world.entity.Entity entity();

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void kwl$adjustEyePosition(float partialTick, CallbackInfo ci) {
        if (this.entity() instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            double eyeHeight = player.getEyeHeight();
            double bbHeight = player.getBbHeight();
            double offset = bbHeight - 2.0 * eyeHeight;
            // 只偏移相机位置（保留原相机逻辑），并加 540.35° 滚转实现倒置视角
            this.position = new Vec3(this.position.x, this.position.y + offset, this.position.z);
            this.rotation.rotateZ((float) Math.toRadians(540.3539364174444));
        }
    }
}
