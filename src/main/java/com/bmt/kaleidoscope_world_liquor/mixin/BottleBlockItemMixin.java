package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.resources.DrinkEffectDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * 官方 1.1.11 调酒颜色 tooltip：给带自定义 4 色 tag 的酒瓶
 * 顶掉原版「颜色：XX」行，改用本模组色值 + 等级效果列表。
 * <p>
 * 目标 {@code BottleBlockItem#appendHoverText} 是实例方法（26.3 签名带
 * {@code TooltipDisplay} 且 tooltip 是 {@code Consumer<Component>}）。
 */
@Mixin(BottleBlockItem.class)
public abstract class BottleBlockItemMixin {

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void kwl$appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                     Consumer<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        int rgb = ModCocktailColors.getCustomColor(stack.getItem());
        String name = ModCocktailColors.getCustomColorName(stack.getItem());
        if (rgb == ModCocktailColors.NO_COLOR || name == null) {
            return;
        }
        ci.cancel();
        tooltip.accept(Component.translatable("color.kaleidoscope_tavern.prefix")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("color.kaleidoscope_world_liquor.%s".formatted(name))
                        .withStyle(style -> style.withColor(rgb))));

        int brewLevel = BottleBlockItem.getBrewLevel(stack);
        if (brewLevel > 0) {
            Component brewLevelText = Component.translatable(
                    "message.kaleidoscope_tavern.barrel.brew_level.%d".formatted(brewLevel));
            tooltip.accept(Component.translatable("tooltip.kaleidoscope_tavern.bottle_block.brew_level", brewLevelText)
                    .withStyle(ChatFormatting.GRAY));

            DrinkEffectData effectData = DrinkEffectDataReloadListener.INSTANCE.get(stack.getItem());
            if (effectData == null) {
                return;
            }
            List<List<DrinkEffectData.Entry>> effects = effectData.effects();
            if (effects.isEmpty() || brewLevel - 1 >= effects.size()) {
                return;
            }

            List<MobEffectInstance> effectsShow = Lists.newArrayList();
            for (DrinkEffectData.Entry entry : effects.get(brewLevel - 1)) {
                if (entry.probability() >= 1.0F) {
                    Holder<MobEffect> effect = entry.effect();
                    int duration = entry.duration() * 20;
                    int amplifier = entry.amplifier();
                    effectsShow.add(new MobEffectInstance(effect, duration, amplifier));
                }
            }

            if (!effectsShow.isEmpty()) {
                tooltip.accept(CommonComponents.space());
                float tickRate = context != null ? context.tickRate() : 20.0F;
                PotionContents.addPotionTooltip(effectsShow, tooltip, 1.0F, tickRate);
            }
        }
    }
}
