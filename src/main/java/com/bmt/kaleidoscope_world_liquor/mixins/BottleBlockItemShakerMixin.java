package com.bmt.kaleidoscope_world_liquor.mixins;

import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 雪克杯联动：tavern 的 {@link BottleBlockItem#isValidForShaker} 要求酿造等级 >= 4，
 * 而世界名酒的酒默认酿造等级为 0（未定义），导致无法加入雪克杯。
 * 这里对 kaleidoscope_world_liquor / smc 命名空间的酒直接放行，恢复原版联动行为。
 */
@Mixin(BottleBlockItem.class)
public class BottleBlockItemShakerMixin {
   @Inject(method = "isValidForShaker", at = @At("HEAD"), cancellable = true)
   private static void kaleidoscopeWorldLiquor$allowOwnDrinks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
      if (key != null) {
         String ns = key.getNamespace();
         if ("kaleidoscope_world_liquor".equals(ns) || "smc".equals(ns)) {
            cir.setReturnValue(true);
         }
      }
   }
}
