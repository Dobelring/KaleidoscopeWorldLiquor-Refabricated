package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.smc.SMCIntegrationEvents;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * smc 联动：玩家用完手上物品（LivingEntityUseItemEvent.Finish 的 Fabric 等价接线）。
 * 只在服务端结算（原 Forge 事件两侧都跑，但 addEffect 以服务端为准，行为等价）；
 * {@link SMCIntegrationEvents#onPlayerFinishDrink} 内部自带 smc 守卫，未装 smc 时为 no-op。
 */
@Mixin(LivingEntity.class)
public abstract class CompleteUsingItemMixin {
    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void kwl$onFinishUsingItem(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide) {
            SMCIntegrationEvents.onPlayerFinishDrink(self, self.getUseItem());
        }
    }
}
