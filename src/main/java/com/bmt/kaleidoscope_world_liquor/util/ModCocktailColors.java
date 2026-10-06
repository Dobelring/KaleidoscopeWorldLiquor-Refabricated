package com.bmt.kaleidoscope_world_liquor.util;

import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.forge.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class ModCocktailColors {
   public static final int NO_COLOR = -1;
   public static final List<ModCocktailColors.ExtraColor> EXTRA_COLORS = Lists.newArrayList(
      new ModCocktailColors.ExtraColor[]{
         new ModCocktailColors.ExtraColor(tag("cocktail_ingredient_brown"), 8606770, "brown"),
         new ModCocktailColors.ExtraColor(tag("cocktail_ingredient_orange"), 16351261, "orange"),
         new ModCocktailColors.ExtraColor(tag("cocktail_ingredient_light_blue"), 3847130, "light_blue"),
         new ModCocktailColors.ExtraColor(tag("cocktail_ingredient_pink"), 15961002, "pink")
      }
   );

   private static TagKey<Item> tag(String name) {
      return TagKey.create(Registries.ITEM, new ResourceLocation("kaleidoscope_tavern", name));
   }

   public static int getCustomColorByTag(TagKey<Item> tag) {
      for (ModCocktailColors.ExtraColor extra : EXTRA_COLORS) {
         if (extra.tag().equals(tag)) {
            return extra.rgb();
         }
      }

      return -1;
   }

   public static int getCustomColor(Item item) {
      for (ModCocktailColors.ExtraColor extra : EXTRA_COLORS) {
         if (item.builtInRegistryHolder().is(extra.tag())) {
            return extra.rgb();
         }
      }

      return -1;
   }

   @Nullable
   public static String getCustomColorName(Item item) {
      for (ModCocktailColors.ExtraColor extra : EXTRA_COLORS) {
         if (item.builtInRegistryHolder().is(extra.tag())) {
            return extra.name();
         }
      }

      return null;
   }

   public static int getMixColor(ItemStack stack) {
      if (stack.getItem() instanceof PotionItem) {
         return 16777215;
      } else {
         int custom = getCustomColor(stack.getItem());
         if (custom != -1) {
            return custom;
         } else {
            ChatFormatting formatting = (ChatFormatting)ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
            return formatting != ChatFormatting.RESET && formatting.getColor() != null ? formatting.getColor() : -1;
         }
      }
   }

   public static int mixStorageColors(ItemStackHandler storage) {
      List<Integer> colors = Lists.newArrayList();

      for (int i = 0; i < storage.getSlots(); i++) {
         ItemStack ingredient = storage.getStackInSlot(i);
         if (!ingredient.isEmpty()) {
            int color = getMixColor(ingredient);
            if (color != -1) {
               colors.add(color);
            }
         }
      }

      return mixRgb(colors);
   }

   public static int mixRgb(List<Integer> colors) {
      if (colors.isEmpty()) {
         return 16777215;
      } else {
         int totalR = 0;
         int totalG = 0;
         int totalB = 0;

         for (int color : colors) {
            totalR += color >> 16 & 0xFF;
            totalG += color >> 8 & 0xFF;
            totalB += color & 0xFF;
         }

         int count = colors.size();
         return totalR / count << 16 | totalG / count << 8 | totalB / count;
      }
   }

   public record ExtraColor(TagKey<Item> tag, int rgb, String name) {
   }
}
