package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.event.EventHandlers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 掉落捕获（事件批次新增，需登记进 kaleidoscope_world_liquor.mixins.json 的 mixins 数组）。
 * <p>
 * 原 Forge 的 {@code Entity#spawnAtLocation} 在 captureDrops 打开时把 ItemEntity 收进列表
 * （即 LivingDropsEvent 的掉落来源）；Fabric 无该机制。原版所有 spawnAtLocation 重载
 * 最终都汇聚到 {@code spawnAtLocation(ItemStack, float)}，所以注入它的 RETURN 即可捕获全部
 * 死亡掉落——但只在 {@link EventHandlers#beginDropCapture}/{@link EventHandlers#endDropCapture}
 * 的捕获窗口（dropAllDeathLoot 期间）内生效，窗口外的常规掉落不受影响。
 */
@Mixin(Entity.class)
public class EntitySpawnAtLocationMixin {

    @Inject(
        method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN")
    )
    private void kaleidoscope_world_liquor$captureDeathDrop(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity spawned = cir.getReturnValue();
        if (spawned != null) {
            EventHandlers.captureDrop(spawned);
        }
    }
}
