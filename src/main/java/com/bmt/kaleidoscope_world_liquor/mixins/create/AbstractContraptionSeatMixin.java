package com.bmt.kaleidoscope_world_liquor.mixins.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.ChairSeatSupport;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractContraptionEntity.class})
public abstract class AbstractContraptionSeatMixin {
    public AbstractContraptionSeatMixin() {
    }

    @Inject(
        method = {"positionRider"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void kaleidoscopeWorldLiquor$positionChairPassenger(Entity passenger, MoveFunction callback, CallbackInfo ci) {
        AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity)(Object) this;
        if (passenger.getVehicle() == contraptionEntity) {
            Vec3 position = ChairSeatSupport.getPassengerPosition(contraptionEntity, passenger, 1.0F);
            if (position != null) {
                callback.accept(passenger, position.x, position.y, position.z);
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"getPassengerPosition"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void kaleidoscopeWorldLiquor$getChairPassengerPosition(Entity passenger, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity)(Object) this;
        if (contraptionEntity.getContraption() != null) {
            Vec3 position = ChairSeatSupport.getPassengerPosition(contraptionEntity, passenger, partialTicks);
            if (position != null) {
                cir.setReturnValue(position);
            }
        }
    }
}
