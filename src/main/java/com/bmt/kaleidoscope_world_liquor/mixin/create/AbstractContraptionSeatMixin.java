package com.bmt.kaleidoscope_world_liquor.mixin.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.ChairSeatSupport;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让坐在装置上吧台凳里的乘客待在"凳面高度"上（官方同名 mixin，只改 Create 包名）。
 * <p>
 * Create 默认把乘客按座椅方块的常规座位高度摆放，吧台凳的座面比普通座椅高，故此处接管
 * {@code positionRider} 与 {@code getPassengerPosition} 两个位置计算点。
 */
@Mixin(AbstractContraptionEntity.class)
public abstract class AbstractContraptionSeatMixin {
    @Inject(method = "positionRider", at = @At("HEAD"), cancellable = true)
    private void kaleidoscopeWorldLiquor$positionChairPassenger(Entity passenger, MoveFunction callback, CallbackInfo ci) {
        AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) (Object) this;
        if (passenger.getVehicle() == contraptionEntity) {
            Vec3 position = ChairSeatSupport.getPassengerPosition(contraptionEntity, passenger, 1.0F);
            if (position != null) {
                callback.accept(passenger, position.x, position.y, position.z);
                ci.cancel();
            }
        }
    }

    @Inject(method = "getPassengerPosition", at = @At("HEAD"), cancellable = true)
    private void kaleidoscopeWorldLiquor$getChairPassengerPosition(Entity passenger, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) (Object) this;
        if (contraptionEntity.getContraption() != null) {
            Vec3 position = ChairSeatSupport.getPassengerPosition(contraptionEntity, passenger, partialTicks);
            if (position != null) {
                cir.setReturnValue(position);
            }
        }
    }
}
