package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.gui.overlay.ShakerOverlay;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 官方 1.1.11 雪克杯 HUD 调色：存储里有自定义 4 色物料时，把原版
 * 「tag 色菱形」提示改成本模组色值绘制。
 * <p>
 * <b>目标 {@code ShakerOverlay#renderShakerBlockTips} 是 private static</b>
 * （26.3 tavern 实测），故 handler 必须同为 static，否则
 * "non-static callback method targets a static method" 客户端启动即崩。
 * 渲染走 26.3 的 {@link GuiGraphicsExtractor#blitSprite}，不再手搓图集精灵。
 */
@Mixin(ShakerOverlay.class)
public abstract class ShakerOverlayMixin {
    private static final Identifier KWL_ICON = Identifier.fromNamespaceAndPath("kaleidoscope_tavern", "gui/rhombus");

    @Inject(method = "renderShakerBlockTips", remap = false, at = @At("HEAD"), cancellable = true)
    private static void kwl$renderShakerBlockTips(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight,
                                                  Minecraft minecraft, LocalPlayer player, CallbackInfo ci) {
        if (!(minecraft.hitResult instanceof BlockHitResult result)) {
            return;
        }
        Level level = player.level();
        BlockPos blockPos = result.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(ModBlocks.SHAKER)) {
            return;
        }
        if (!(level.getBlockEntity(blockPos) instanceof ShakerBlockEntity shaker)) {
            return;
        }

        ItemStackHandler storage = shaker.getStorage();
        boolean hasCustom = false;
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (!stack.isEmpty() && ModCocktailColors.getCustomColor(stack.getItem()) != ModCocktailColors.NO_COLOR) {
                hasCustom = true;
                break;
            }
        }
        if (!hasCustom) {
            return;
        }

        ci.cancel();
        Font font = Minecraft.getInstance().font;
        int x = screenWidth / 2 - 28;
        int y = screenHeight / 2 + 26;
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            int custom = ModCocktailColors.getCustomColor(stack.getItem());
            if (custom != ModCocktailColors.NO_COLOR) {
                kwl$renderIcon(guiGraphics, x, y + 6, custom | 0xFF000000);
            } else {
                TextColor textColor = ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
                if (textColor == null) {
                    guiGraphics.fakeItem(stack, x, y);
                    guiGraphics.itemDecorations(font, stack, x, y);
                } else {
                    kwl$renderIcon(guiGraphics, x, y + 6, textColor.getValue() | 0xFF000000);
                }
            }
            x += 20;
        }
    }

    private static void kwl$renderIcon(GuiGraphicsExtractor guiGraphics, int x, int y, int color) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, KWL_ICON, x, y, 16, 16, color);
    }
}
