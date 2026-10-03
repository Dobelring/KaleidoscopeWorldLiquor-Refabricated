package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.event.EventHandlers;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 掉落捕获（事件批次新增，需登记进 kaleidoscope_world_liquor.mixins.json 的 mixins 数组）。
 * <p>
 * 原 Forge 在 {@code LivingEntity#dropAllDeathLoot} 内开 captureDrops、把死亡掉落
 * 收集起来过 {@code LivingDropsEvent}（淘金热复制 + 斩首补头两个监听器）。
 * Fabric 无掉落事件，这里在 dropAllDeathLoot 前后开关 {@link EventHandlers} 的线程本地
 * 捕获栈，RETURN 时弹出本层列表并回调原两个监听器（调用顺序 = 原同类声明顺序：
 * 先复制、后补头，头颅不会被复制）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDropsMixin {

    @Inject(
        method = "dropAllDeathLoot",
        at = @At("HEAD")
    )
    private void kaleidoscope_world_liquor$startDropCapture(DamageSource source, CallbackInfo ci) {
        EventHandlers.beginDropCapture();
    }

    @Inject(
        method = "dropAllDeathLoot",
        at = @At("RETURN")
    )
    private void kaleidoscope_world_liquor$finishDropCapture(DamageSource source, CallbackInfo ci) {
        List<ItemEntity> drops = EventHandlers.endDropCapture();
        LivingEntity self = (LivingEntity) (Object) this;
        // 原 onLivingDrops(LivingDropsEvent)
        EventHandlers.onLivingDrops(self, source, drops);
        // 原 onLivingDropsBeheading(LivingDropsEvent)
        EventHandlers.onLivingDropsBeheading(self, drops);
    }
}
