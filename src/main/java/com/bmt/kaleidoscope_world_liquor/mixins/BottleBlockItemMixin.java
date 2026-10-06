package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData.Entry;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.resources.DrinkEffectDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BottleBlockItem.class})
public abstract class BottleBlockItemMixin {
   @Inject(
      method = {"appendHoverText"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kwl$appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
      int rgb = ModCocktailColors.getCustomColor(stack.getItem());
      String name = ModCocktailColors.getCustomColorName(stack.getItem());
      if (rgb != -1 && name != null) {
         ci.cancel();
         tooltip.add(
            Component.translatable("color.kaleidoscope_tavern.prefix")
               .withStyle(ChatFormatting.GRAY)
               .append(Component.translatable("color.kaleidoscope_world_liquor.%s".formatted(name)).withStyle(style -> style.withColor(rgb)))
         );
         int brewLevel = BottleBlockItem.getBrewLevel(stack);
         if (0 < brewLevel) {
            Component brewLevelText = Component.translatable("message.kaleidoscope_tavern.barrel.brew_level.%d".formatted(brewLevel));
            tooltip.add(Component.translatable("tooltip.kaleidoscope_tavern.bottle_block.brew_level", new Object[]{brewLevelText}).withStyle(ChatFormatting.GRAY));
            DrinkEffectData effectData = (DrinkEffectData)DrinkEffectDataReloadListener.INSTANCE.get(stack.getItem());
            if (effectData == null) {
               return;
            }

            List<List<Entry>> effects = effectData.effects();
            if (effects.isEmpty()) {
               return;
            }

            List<MobEffectInstance> effectsShow = Lists.newArrayList();

            for (Entry entry : effects.get(brewLevel - 1)) {
               if (entry.probability() >= 1.0F) {
                  MobEffect effect = entry.effect();
                  int duration = entry.duration() * 20;
                  int amplifier = entry.amplifier();
                  effectsShow.add(new MobEffectInstance(effect, duration, amplifier));
               }
            }

            if (!effectsShow.isEmpty()) {
               tooltip.add(CommonComponents.space());
               PotionUtils.addPotionTooltip(effectsShow, tooltip, 1.0F);
            }
         }
      }
   }
}
