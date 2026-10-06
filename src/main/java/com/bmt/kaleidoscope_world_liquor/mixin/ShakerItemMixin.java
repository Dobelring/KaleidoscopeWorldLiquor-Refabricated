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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * 官方 1.1.11 雪克杯调色：
 * <ul>
 *   <li>{@code pourResult} 在写入特调鸡尾酒效果时，一并按存储物混合色设置方块颜色；</li>
 *   <li>{@code appendHoverText} 存储清单按自定义 4 色 / tavern tag 色着色。</li>
 * </ul>
 * 目标均为 tavern 实例方法；{@code pourResult} 是 mod 方法（remap=false）。
 */
@Mixin(ShakerItem.class)
public abstract class ShakerItemMixin {

    @Inject(method = "pourResult", remap = false, at = @At(value = "INVOKE", remap = false,
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/blockentity/mixology/SignatureCocktailBlockEntity;setEffects(Ljava/util/List;)V"))
    private void kwl$fixPourColor(Level level, ItemStack stack, BlockPos pos, BlockState rawState, CallbackInfo ci) {
        if (level.getBlockEntity(pos) instanceof SignatureCocktailBlockEntity cocktail) {
            cocktail.setColor(ModCocktailColors.mixStorageColors(ShakerItem.getStorage(stack)));
        }
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void kwl$appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                     Consumer<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
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
                text = component.copy().withStyle(style -> style.withColor(custom));
            } else {
                TextColor textColor = ColorUtils.ITEM_COLOR_CACHE.apply(ingredient.getItem());
                text = textColor != null ? component.copy().withColor(textColor) : component.copy();
            }
            tooltip.accept(Component.literal("▶ ").withStyle(ChatFormatting.GRAY).append(text));
        }
    }
}
