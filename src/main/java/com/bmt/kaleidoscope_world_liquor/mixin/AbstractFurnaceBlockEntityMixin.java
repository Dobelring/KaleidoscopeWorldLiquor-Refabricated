package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModFuels;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3：原版燃料改物品组件（minecraft:cooking_fuel）后无法覆盖外部模组物品，
 * 熔炉 {@code getBurnDuration} 返 0 时查 {@link ModFuels} 表回填
 * （家具燃料 + 酒馆折叠梯，见 ModFuels 注释）。
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

   @Inject(method = "getBurnDuration", at = @At("RETURN"), cancellable = true)
   private void kwl$furnitureFuel(ServerLevel level, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
      if (cir.getReturnValueI() == 0) {
         int burnTime = ModFuels.burnTimeOrZero(stack.getItem());
         if (burnTime > 0) {
            cir.setReturnValue(burnTime);
         }
      }
   }
}
