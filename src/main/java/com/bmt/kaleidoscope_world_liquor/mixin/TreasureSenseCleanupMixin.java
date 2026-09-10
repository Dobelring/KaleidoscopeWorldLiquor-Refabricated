package com.bmt.kaleidoscope_world_liquor.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GameRenderer 关闭时释放宝藏感知的 GPU 顶点缓冲（照官方文档穿墙渲染示例）。
 */
@Mixin(GameRenderer.class)
public abstract class TreasureSenseCleanupMixin {

    @Inject(method = "close", at = @At("RETURN"))
    private void kwl$closeTreasureSense(CallbackInfo ci) {
        com.bmt.kaleidoscope_world_liquor.client.render.TreasureSenseRenderer.close();
    }
}
