package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.client.MultiJumpHandler;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 客户端玩家 tick：
 * <ul>
 *   <li>多段跳（客户端判定）；</li>
 *   <li>官方 1.1.11：反重力下创造飞行上下键反转——{@code aiStep} 内
 *       {@code LocalPlayer#setDeltaMovement(Vec3)} 的纵向分量取反。</li>
 * </ul>
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

    // 官方 1.1.11：反重力下创造飞行上下键反转修复
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void kwl$reverseCreativeFlyingThrust(LocalPlayer player, Vec3 desired, Operation<Void> original) {
        if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            Vec3 current = player.getDeltaMovement();
            double dy = desired.y - current.y;
            desired = desired.add(0.0, -2.0 * dy, 0.0);
        }
        original.call(player, desired);
    }
}
