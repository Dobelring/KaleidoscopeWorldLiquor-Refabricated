package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.gui.overlay.ShakerOverlay;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ShakerOverlay.class})
public abstract class ShakerOverlayMixin {
   private static final ResourceLocation KWL_ICON = ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "gui/rhombus");

   @Inject(
      method = {"renderShakerBlockTips"},
      remap = false,
      at = {@At("HEAD")},
      cancellable = true
   )
   // tavern 的 renderShakerBlockTips 是 static 方法，注入 handler 必须同为 static（实机崩溃修复）
   private static void kwl$renderShakerBlockTips(GuiGraphics guiGraphics, int screenWidth, int screenHeight, Minecraft minecraft, LocalPlayer player, CallbackInfo ci) {
      if (minecraft.hitResult instanceof BlockHitResult result) {
         Level level = player.level();
         BlockPos blockPos = result.getBlockPos();
         BlockState blockState = level.getBlockState(blockPos);
         if (blockState.is(ModBlocks.SHAKER)) {
            if (level.getBlockEntity(blockPos) instanceof ShakerBlockEntity shaker) {
               ItemStackHandler storage = shaker.getStorage();
               boolean hasCustom = false;

               for (int font = 0; font < storage.getSlots(); font++) {
                  ItemStack stack = storage.getStackInSlot(font);
                  if (!stack.isEmpty() && ModCocktailColors.getCustomColor(stack.getItem()) != -1) {
                     hasCustom = true;
                     break;
                  }
               }

               if (hasCustom) {
                  ci.cancel();
                  Font fontx = Minecraft.getInstance().font;
                  int x = screenWidth / 2 - 28;
                  int y = screenHeight / 2 + 26;

                  for (int i = 0; i < storage.getSlots(); i++) {
                     ItemStack stack = storage.getStackInSlot(i);
                     if (!stack.isEmpty()) {
                        int custom = ModCocktailColors.getCustomColor(stack.getItem());
                        if (custom != -1) {
                           kwl$renderIcon(guiGraphics, x, y + 6, custom | 0xFF000000);
                        } else {
                           ChatFormatting chatFormatting = (ChatFormatting)ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
                           if (chatFormatting == ChatFormatting.RESET) {
                              guiGraphics.renderFakeItem(stack, x, y);
                              guiGraphics.renderItemDecorations(fontx, stack, x, y);
                           } else {
                              int color = Objects.requireNonNull(chatFormatting.getColor()) | 0xFF000000;
                              kwl$renderIcon(guiGraphics, x, y + 6, color);
                           }
                        }

                        x += 20;
                     }
                  }
               }
            }
         }
      }
   }

   private static void kwl$renderIcon(GuiGraphics pGuiGraphics, int x, int y, int color) {
      float alpha = ARGB32.alpha(color) / 255.0F;
      float red = ARGB32.red(color) / 255.0F;
      float green = ARGB32.green(color) / 255.0F;
      float blue = ARGB32.blue(color) / 255.0F;
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(KWL_ICON);
      pGuiGraphics.blit(x, y, 0, 16, 16, sprite, red, green, blue, alpha);
   }
}
