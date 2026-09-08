package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 反重力（reverse_gravity）实体倒置——借用原版 Dinnerbone 彩蛋链路。
 * 关键：AvatarRenderer（玩家渲染器）**覆写了 isEntityUpsideDown**（含披风可见短路），
 * 只注入 LivingEntityRenderer 父类版本对玩家无效——必须两个类都注入。
 * 强制 true 后原版渲染管线自动完成 isUpsideDown 置位 + xRot/yRot 取反 +
 * submit 内 translate/rotateZ(180) 模型倒置（与 1.20.1 scale(1,-1,1) 等价）。
 */
@Mixin({LivingEntityRenderer.class, AvatarRenderer.class})
public abstract class EntityRendererMixin {

    @Inject(method = "isEntityUpsideDown(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void kwl$forceUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            cir.setReturnValue(true);
        }
    }
}
