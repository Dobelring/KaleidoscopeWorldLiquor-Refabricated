package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 官方 1.1.11 调色 mixin 之三（目标：tavern ShakerOverlay 雪克杯 HUD 提示）：
 * 存储原料含自定义 4 色时接管渲染——自定义色画菱形色块，其余照 tavern 原样。
 * <p>
 * 26.x 适配（静态核对过签名）：{@code renderShakerBlockTips} 是 <b>private static</b>
 * （参数 GuiGraphicsExtractor/int/int/Minecraft/LocalPlayer），handler 必须同为 static；
 * 菱形图标用 {@code blitSprite(RenderPipelines.GUI_TEXTURED, …, color)}（tavern renderIcon 同款）。
 */
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.client.gui.overlay.ShakerOverlay")
public abstract class ShakerOverlayMixin {
    /** tavern ShakerOverlay.ICON 同值（其为 private，这里同 id 重建） */
    private static final Identifier KWL_ICON = Identifier.fromNamespaceAndPath("kaleidoscope_tavern", "gui/rhombus");

    @Inject(
            method = "renderShakerBlockTips",
            remap = false,
            at = @At("HEAD"),
            cancellable = true
    )
    private static void kwl$renderShakerBlockTips(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight,
                                                  Minecraft minecraft, LocalPlayer player, CallbackInfo ci) {
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult result)) {
            return;
        }
        Level level = player.level();
        BlockPos blockPos = result.getBlockPos();
        if (!level.getBlockState(blockPos).is(ModBlocks.SHAKER)) {
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
            // 无自定义色：交给原版（tavern 自绘）逻辑
            return;
        }
        ci.cancel();
        Font font = minecraft.font;
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
