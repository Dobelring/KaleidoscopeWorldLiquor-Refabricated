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
    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("TAIL"))
    private void kwl$adjustEyePosition(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity focusedEntity,
                                       boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
        if (focusedEntity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            double eyeHeight = player.getEyeHeight();
            double bbHeight = player.getBbHeight();
            double offset = bbHeight - 2.0 * eyeHeight;
            Vec3 pos = player.getPosition(partialTick);
            this.setPosition(pos.x, pos.y + offset, pos.z);
        }
    }
}
