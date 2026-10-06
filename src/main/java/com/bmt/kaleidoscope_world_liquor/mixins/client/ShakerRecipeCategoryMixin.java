package com.bmt.kaleidoscope_world_liquor.mixins.client;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.ShakerRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ShakerRecipeCategory.class})
public abstract class ShakerRecipeCategoryMixin {
   @Inject(
      method = {"draw"},
      remap = false,
      at = {@At("TAIL")}
   )
   private void kwl$drawExtraColorBlocks(
      ShakerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY, CallbackInfo ci
   ) {
      NonNullList<Ingredient> ingredients = recipe.getIngredients();

      for (int i = 0; i < ingredients.size(); i++) {
         Ingredient ingredient = (Ingredient)ingredients.get(i);
         if (!ingredient.isEmpty()) {
            int rgb = kwl$getTagColor(ingredient);
            if (rgb != -1) {
               int x = 66;
               int y = 14 + 18 * i;
               guiGraphics.fill(x, y, x + 8, y + 16, 0xFF000000 | rgb);
            }
         }
      }
   }

   private static int kwl$getTagColor(Ingredient ingredient) {
      JsonElement element = ingredient.toJson();
      if (!element.isJsonObject()) {
         return -1;
      } else {
         JsonObject jsonObject = element.getAsJsonObject();
         if (!jsonObject.has("tag")) {
            return -1;
         } else {
            ResourceLocation location = ResourceLocation.tryParse(jsonObject.get("tag").getAsString());
            if (location == null) {
               return -1;
            } else {
               TagKey<Item> tagKey = TagKey.create(Registries.ITEM, location);
               return ModCocktailColors.getCustomColorByTag(tagKey);
            }
         }
      }
   }
}
