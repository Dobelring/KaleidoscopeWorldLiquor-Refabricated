package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.SignatureCocktailBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * 官方 1.1.11 调色 mixin 之二（目标：tavern ShakerItem）：
 * <ul>
 *   <li>pourResult 注入 setEffects 调用点——特调鸡尾酒颜色改由 ModCocktailColors
 *       按存储原料混合（自定义 4 色优先），覆盖 tavern 自算的 mixColors。</li>
 *   <li>appendHoverText——存储原料中存在自定义 4 色时接管 tooltip（自定义色直接上色，
 *       其余沿用 tavern 的原料色缓存）。</li>
 * </ul>
 * 26.x 适配：pourResult 为 private 实例方法（method 仅给名字 + remap=false，descriptor 含原版类
 * 不参与 remap，同仓 TapBlockMixin 惯例）；appendHoverText 为 5 参 Consumer 形式。
 */
@Mixin(ShakerItem.class)
public abstract class ShakerItemMixin {

    @Inject(
            method = "pourResult",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/blockentity/mixology/SignatureCocktailBlockEntity;setEffects(Ljava/util/List;)V",
                    remap = false
            )
    )
    private void kwl$fixPourColor(Level level, ItemStack stack, BlockPos pos, BlockState rawState, CallbackInfo ci) {
        if (level.getBlockEntity(pos) instanceof SignatureCocktailBlockEntity cocktail) {
            cocktail.setColor(ModCocktailColors.mixStorageColors(ShakerItem.getStorage(stack)));
        }
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void kwl$appendHoverText(ItemStack stack, @Nullable TooltipContext context, TooltipDisplay display,
                                     Consumer<Component> consumer, TooltipFlag flag, CallbackInfo ci) {
        if (ShakerItem.hasResult(stack) || !ShakerItem.hasStorage(stack)) {
            return;
        }
        ItemStackHandler storage = ShakerItem.getStorage(stack);
        boolean hasCustom = false;
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack ingredient = storage.getStackInSlot(i);
            if (!ingredient.isEmpty() && ModCocktailColors.getCustomColor(ingredient.getItem()) != ModCocktailColors.NO_COLOR) {
                hasCustom = true;
                break;
            }
        }
        if (!hasCustom) {
            return;
        }
        ci.cancel();
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack ingredient = storage.getStackInSlot(i);
            if (ingredient.isEmpty() || !(ingredient.getHoverName() instanceof MutableComponent component)) {
                continue;
            }
            int custom = ModCocktailColors.getCustomColor(ingredient.getItem());
            MutableComponent text;
            if (custom != ModCocktailColors.NO_COLOR) {
                text = component.withColor(custom);
            } else {
                TextColor textColor = ColorUtils.ITEM_COLOR_CACHE.apply(ingredient.getItem());
                text = textColor != null ? component.withColor(textColor) : component;
            }
            consumer.accept(Component.literal("▶ ").withStyle(ChatFormatting.GRAY).append(text));
        }
    }
}
