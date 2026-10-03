package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.event.BrewAcceleratorEventHandler;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.ItemCombinerMenuAccessor;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 铁砧拦截（事件批次新增，需登记进 kaleidoscope_world_liquor.mixins.json 的 mixins 数组）。
 * <p>
 * 原 Forge {@code AnvilUpdateEvent}（BrewAcceleratorEventHandler#onAnvilUpdate：右槽为
 * 臻酿加速附魔书、左槽不是附魔书时取消铁砧运算）在 Fabric 无对应事件，
 * 改为在 {@code AnvilMenu#createResult} 头部判定；取消时复刻原版“空输出 + 经验费用清零”
 * 的效果（与原 setCanceled 后铁砧产出为空的行为一致），随后 ci.cancel() 跳过后续计算。
 * <p>
 * 输入/输出槽字段声明在父类 ItemCombinerMenu，@Shadow 只解析目标类自身声明字段，
 * 故经 mixins/accessor/ItemCombinerMenuAccessor 访问；cost 字段声明在 AnvilMenu 自身，可直接 @Shadow。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    // 目标字段是 private final：按 TV 同款写法（字段声明本身不加 final 关键字——否则 javac
    // 报“未在构造器初始化”，用 @Final 注解向 mixin 声明其 final 性）
    @Shadow
    @Final
    private DataSlot cost;

    @Inject(
        method = "createResult",
        at = @At("HEAD"),
        cancellable = true
    )
    private void kaleidoscope_world_liquor$blockBrewAcceleratorAnvil(CallbackInfo ci) {
        ItemCombinerMenuAccessor self = (ItemCombinerMenuAccessor) (Object) this;
        ItemStack left = self.getInputSlots().getItem(0);
        ItemStack right = self.getInputSlots().getItem(1);
        if (BrewAcceleratorEventHandler.onAnvilUpdate(left, right)) {
            self.getResultSlots().setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            ci.cancel();
        }
    }
}
