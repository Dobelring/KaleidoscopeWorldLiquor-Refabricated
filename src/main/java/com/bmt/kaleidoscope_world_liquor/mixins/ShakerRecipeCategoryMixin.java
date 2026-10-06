package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.ShakerRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
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
      RecipeHolder<ShakerRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY, CallbackInfo ci
   ) {
      NonNullList<Ingredient> ingredients = ((ShakerRecipe)recipe.value()).getIngredients();

      for (int i = 0; i < ingredients.size(); i++) {
         Ingredient ingredient = (Ingredient)ingredients.get(i);
         if (!ingredient.isEmpty()) {
            int rgb = -1;

            for (ItemStack stack : ingredient.getItems()) {
               int color = ModCocktailColors.getCustomColor(stack.getItem());
               if (color != -1) {
                  rgb = color;
                  break;
               }
            }

            if (rgb != -1) {
               int x = 66;
               int y = 14 + 18 * i;
               guiGraphics.fill(x, y, x + 8, y + 16, 0xFF000000 | rgb);
            }
         }
      }
   }
}
