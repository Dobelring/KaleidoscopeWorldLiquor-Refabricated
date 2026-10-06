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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
 * 官方 1.1.11：雪克杯倒出鸡尾酒时用存储原料的混合色（含 4 个 extra 色）覆盖泡 default 色，
 * 并在 tooltip 里对含自定义色原料的存储列表整体重绘着色。
 * <p>
 * 26.x 差异：{@code pourResult} / {@code appendHoverText} 签名见 javap（appendHoverText 为 5 参）。
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
            MutableComponent text;
            int custom = ModCocktailColors.getCustomColor(ingredient.getItem());
            if (custom != ModCocktailColors.NO_COLOR) {
                text = component.withStyle(style -> style.withColor(custom));
            } else {
                ChatFormatting chatFormatting = ColorUtils.ITEM_COLOR_CACHE.apply(ingredient.getItem());
                if (chatFormatting == ChatFormatting.RESET) {
                    chatFormatting = ChatFormatting.GRAY;
                }
                text = component.withStyle(chatFormatting);
            }
            tooltip.accept(Component.literal("▶ ").withStyle(ChatFormatting.GRAY).append(text));
        }
    }
}
