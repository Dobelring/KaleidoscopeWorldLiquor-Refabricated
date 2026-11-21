package com.bmt.kaleidoscope_world_liquor.mixins;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 官方 1.2.0：酒桶产物只对 BottleBlockItem（瓶装酒）回填 BrewLevel，酒杯类饮品
 * （DrinkBlockItem，如朋友的酒/冰茶）拿到的成品没有酿造等级 → 饮用时 addDrinkEffect
 * 按 tier 取效果直接落空（tier 0 不给任何效果）。官方仅在 smc 装机时用创造栏补丁
 * （SMCCreativeTabEvents）遮掩了这条路径；本代注册环境没有 smc，酒桶是唯一获取途径，
 * 故在产物出桶时对酒杯类饮品同样补写当前酿造等级。
 */
@Mixin(BarrelBlockEntity.class)
public abstract class BarrelBlockEntityMixin {
   @Inject(method = "extractOneOutput", at = @At("RETURN"), remap = false)
   private void kaleidoscope$tagDrinkBlockBrewLevel(ItemStack result, CallbackInfoReturnable<ItemStack> cir) {
      ItemStack stack = cir.getReturnValue();
      if (!stack.isEmpty() && stack.getItem() instanceof DrinkBlockItem) {
         int brewLevel = ((BarrelBlockEntity) (Object) this).getBrewLevel();
         if (brewLevel >= 1) {
            BottleBlockItem.setBrewLevel(stack, brewLevel);
         }
      }
   }
}
