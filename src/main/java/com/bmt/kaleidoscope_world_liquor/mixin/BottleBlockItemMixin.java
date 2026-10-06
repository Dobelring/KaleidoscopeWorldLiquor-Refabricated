package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData.Entry;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.resources.DrinkEffectDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
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
 * 官方 1.1.11：自定义 4 色酒瓶的 tooltip——颜色前缀行（color.kaleidoscope_world_liquor.&lt;color&gt;）
 * + 酿造等级效果列表。目标是 tavern 的 BottleBlockItem。
 * <p>
 * 26.x 适配：appendHoverText 为 5 参形式（TooltipDisplay + Consumer），行经 {@code consumer.accept} 追加；
 * PotionContents.addPotionTooltip 亦为 Consumer 形式。
 */
@Mixin(BottleBlockItem.class)
public abstract class BottleBlockItemMixin {

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void kwl$appendHoverText(ItemStack stack, @Nullable TooltipContext context, TooltipDisplay display,
                                     Consumer<Component> consumer, TooltipFlag flag, CallbackInfo ci) {
        int rgb = ModCocktailColors.getCustomColor(stack.getItem());
        String name = ModCocktailColors.getCustomColorName(stack.getItem());
        if (rgb == ModCocktailColors.NO_COLOR || name == null) {
            return;
        }
        ci.cancel();
        // 自定义色酒瓶：tavern 原生配色缓存不认识这 4 个 tag，整段 tooltip 由本 mixin 重建
        consumer.accept(Component.translatable("color.kaleidoscope_tavern.prefix")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("color.kaleidoscope_world_liquor.%s".formatted(name))
                        .withStyle(style -> style.withColor(rgb))));
        int brewLevel = BottleBlockItem.getBrewLevel(stack);
        if (brewLevel <= 0) {
            return;
        }
        Component brewLevelText = Component.translatable("message.kaleidoscope_tavern.barrel.brew_level.%d".formatted(brewLevel));
        consumer.accept(Component.translatable("tooltip.kaleidoscope_tavern.bottle_block.brew_level", brewLevelText)
                .withStyle(ChatFormatting.GRAY));
        DrinkEffectData effectData = DrinkEffectDataReloadListener.INSTANCE.get(stack.getItem());
        if (effectData == null) {
            return;
        }
        List<List<Entry>> effects = effectData.effects();
        if (effects.isEmpty() || brewLevel - 1 >= effects.size()) {
            return;
        }
        List<MobEffectInstance> effectsShow = Lists.newArrayList();
        for (Entry entry : effects.get(brewLevel - 1)) {
            if (entry.probability() >= 1.0F) {
                Holder<MobEffect> effect = entry.effect();
                int duration = entry.duration() * 20;
                int amplifier = entry.amplifier();
                effectsShow.add(new MobEffectInstance(effect, duration, amplifier));
            }
        }
        if (!effectsShow.isEmpty()) {
            consumer.accept(CommonComponents.space());
            float tickRate = context != null ? context.tickRate() : 20.0F;
            PotionContents.addPotionTooltip(effectsShow, consumer, 1.0F, tickRate);
        }
    }
}
