package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.client.MultiJumpHandler;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 多段跳（客户端 tick）+ 反重力横向移动反向（原 serverAiStep 反转 strafe，
 * 1.21.11 无 serverAiStep，改在 aiStep TAIL 做）。
 */
@Mixin(LocalPlayer.class)
public abstract class ClientPlayerMixin {
    @Unique
    private final MultiJumpHandler kaleidoscope_world_liquor$multiJumpHandler = new MultiJumpHandler();

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void kwl$multiJumpTick(CallbackInfo ci) {
        this.kaleidoscope_world_liquor$multiJumpHandler.tickMovement((LocalPlayer) (Object) this);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void kwl$invertStrafe(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            player.xxa = -player.xxa;
        }
    }
}
