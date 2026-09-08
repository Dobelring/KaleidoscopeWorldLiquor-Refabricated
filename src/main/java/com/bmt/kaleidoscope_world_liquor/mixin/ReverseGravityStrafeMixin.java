package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 反重力：反转横向移动输入。
 * 1.20.1 原版注入 LocalPlayer.serverAiStep RETURN 反转 xxa；1.21.11
 * serverAiStep 仅服务端分支调用，改注入 LocalPlayer.applyInput RETURN——
 * 该覆写版把 input.getMoveVector() 写入 xxa/zza（aiStep 随后读取消费），
 * 客户端每 tick 必经。
 */
@Mixin(LocalPlayer.class)
public abstract class ReverseGravityStrafeMixin {

    @Inject(method = "applyInput", at = @At("RETURN"))
    private void kwl$invertStrafe(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            player.xxa = -player.xxa;
        }
    }
}
