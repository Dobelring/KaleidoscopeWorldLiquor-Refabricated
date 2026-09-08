package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.event.DrinkingSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge LivingEntityUseItemEvent.Start/Tick/Finish/Stop 的等价钩子。
 * 1.21.11 生命周期方法：startUsingItem / updateUsingItem（每 tick）/
 * completeUsingItem（喝完）/ releaseUsingItem+stopUsingItem（中断）。
 */
@Mixin(LivingEntity.class)
public abstract class UseItemEventsMixin {

    @Inject(method = "startUsingItem", at = @At("TAIL"))
    private void kwl$onStartUsingItem(@NotNull InteractionHand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        DrinkingSounds.onStart(entity, entity.getItemInHand(hand));
    }

    @Inject(method = "updateUsingItem", at = @At("TAIL"))
    private void kwl$onTickUsingItem(@NotNull ItemStack stack, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        DrinkingSounds.onTick(entity, stack);
    }

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void kwl$onCompleteUsingItem(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        DrinkingSounds.onFinish(entity, entity.getUseItem());
    }

    @Inject(method = "stopUsingItem", at = @At("TAIL"))
    private void kwl$onStopUsingItem(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        ItemStack stack = entity.getUseItem();
        if (!stack.isEmpty()) {
            DrinkingSounds.onStop(entity, stack);
        }
    }
}
