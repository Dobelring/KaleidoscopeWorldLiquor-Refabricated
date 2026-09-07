package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.client.MultiJumpHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 多段跳（客户端 tick）。
 * 反重力横向移动反向由 ReverseGravityStrafeMixin 负责（applyInput RETURN 注入）。
 */
@Mixin(LocalPlayer.class)
public abstract class ClientPlayerMixin {
    @Unique
    private final MultiJumpHandler kaleidoscope_world_liquor$multiJumpHandler = new MultiJumpHandler();

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void kwl$multiJumpTick(CallbackInfo ci) {
        this.kaleidoscope_world_liquor$multiJumpHandler.tickMovement((LocalPlayer) (Object) this);
    }
}
