package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.SignatureCocktailBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.github.ysbbbbbb.kaleidoscopetavern.util.forge.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ShakerItem.class})
public abstract class ShakerItemMixin {
   @Inject(
      method = {"pourResult"},
      remap = false,
      at = {@At(
         value = "INVOKE",
         target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/blockentity/mixology/SignatureCocktailBlockEntity;setEffects(Ljava/util/List;)V",
         remap = false
      )}
   )
   private void kwl$fixPourColor(Level level, ItemStack stack, BlockPos pos, BlockState rawState, CallbackInfo ci) {
      if (level.getBlockEntity(pos) instanceof SignatureCocktailBlockEntity cocktail) {
         cocktail.setColor(ModCocktailColors.mixStorageColors(ShakerItem.getStorage(stack)));
      }
   }

   @Inject(
      method = {"appendHoverText"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kwl$appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
      if (!ShakerItem.hasResult(stack) && ShakerItem.hasStorage(stack)) {
         ItemStackHandler storage = ShakerItem.getStorage(stack);
         boolean hasCustom = false;

         for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack ingredient = storage.getStackInSlot(i);
            if (!ingredient.isEmpty() && ModCocktailColors.getCustomColor(ingredient.getItem()) != -1) {
               hasCustom = true;
               break;
            }
         }

         if (hasCustom) {
            ci.cancel();

            for (int ix = 0; ix < storage.getSlots(); ix++) {
               ItemStack ingredient = storage.getStackInSlot(ix);
               if (!ingredient.isEmpty() && ingredient.getHoverName() instanceof MutableComponent component) {
                  int custom = ModCocktailColors.getCustomColor(ingredient.getItem());
                  MutableComponent text;
                  if (custom != -1) {
                     text = component.withStyle(style -> style.withColor(custom));
                  } else {
                     ChatFormatting chatFormatting = (ChatFormatting)ColorUtils.ITEM_COLOR_CACHE.apply(ingredient.getItem());
                     if (chatFormatting == ChatFormatting.RESET) {
                        chatFormatting = ChatFormatting.GRAY;
                     }

                     text = component.withStyle(chatFormatting);
                  }

                  tooltip.add(Component.literal("▶ ").withStyle(ChatFormatting.GRAY).append(text));
               }
            }
         }
      }
   }
}
