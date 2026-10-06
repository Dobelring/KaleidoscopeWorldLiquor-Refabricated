package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.gui.overlay.ShakerOverlay;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * 官方 1.1.11：准星指向含自定义色原料的雪克杯方块时，用带色菱形图标重绘 HUD 提示
 * （含自定义色的槽画 extra 色方块、原版 tag 色画对应色方块、无色槽画物品本身）。
 * <p>
 * <b>目标是 static 方法</b>：{@code ShakerOverlay#renderShakerBlockTips} 在 26.x tavern 里是
 * {@code private static void (GuiGraphicsExtractor, int, int, Minecraft, LocalPlayer)}，
 * 因此 handler 也必须声明为 static，否则 mixin 应用期崩溃。
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
                ChatFormatting chatFormatting = ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
                if (chatFormatting == ChatFormatting.RESET) {
                    guiGraphics.fakeItem(stack, x, y);
                    guiGraphics.itemDecorations(font, stack, x, y);
                } else {
                    int color = Objects.requireNonNull(chatFormatting.getColor()) | 0xFF000000;
                    kwl$renderIcon(guiGraphics, x, y + 6, color);
                }
            }
            x += 20;
        }
    }

    /** 26.x：GuiGraphicsExtractor 无 atlas sprite blit，改用 tavern 同款 blitSprite 带色绘制。 */
    private static void kwl$renderIcon(GuiGraphicsExtractor guiGraphics, int x, int y, int color) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, KWL_ICON, x, y, 16, 16, color);
    }
}
