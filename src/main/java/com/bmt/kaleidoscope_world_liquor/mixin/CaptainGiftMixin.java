package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.effect.CaptainGiftHelper;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 船长赠礼（captain_gift）水上行走。
 * 1.20.1 拦 Entity.collide(Vec3)；1.21.11 该方法已删除，改拦 Entity.move
 * 内 collideBoundingBox 的返回值，把最终位移的 y 提到水面高度。
 */
@Mixin(Entity.class)
public abstract class CaptainGiftMixin {

    @ModifyExpressionValue(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 kwl$enableWaterWalking(Vec3 original) {
        Entity entity = (Entity) (Object) this;
        return CaptainGiftHelper.enableWaterWalking(entity, original);
    }
}
