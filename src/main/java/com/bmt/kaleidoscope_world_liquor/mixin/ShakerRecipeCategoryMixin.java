package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.ShakerRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 官方 1.1.11：JEI 雪克杯配方分类里为含 extra 色原料的槽补画一个色块。
 * <p>
 * 26.x 差异：{@code draw} 第三参是 {@link GuiGraphicsExtractor}（非 GuiGraphics）；
 * {@code ShakerRecipe#getIngredients()} 更名为 {@code ingredients()}；色块坐标跟随本分支
 * tavern 自绘（x=69 宽 5，与贴图方框内部重合；官方 1.21.1 的 66/8 在本分支会偏左）。
 */
@Mixin(ShakerRecipeCategory.class)
public abstract class ShakerRecipeCategoryMixin {

    @Inject(method = "draw", remap = false, at = @At("TAIL"))
    private void kwl$drawExtraColorBlocks(RecipeHolder<ShakerRecipe> recipe, IRecipeSlotsView recipeSlotsView,
                                          GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY, CallbackInfo ci) {
        NonNullList<Ingredient> ingredients = recipe.value().ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
                continue;
            }
            int rgb = ModCocktailColors.NO_COLOR;
            // 26.x：Ingredient#items() 返回 Stream<Holder<Item>>（不再是 ItemStack[]）
            for (ItemStack stack : ingredient.items().map(ItemStack::new).toList()) {
                int color = ModCocktailColors.getCustomColor(stack.getItem());
                if (color != ModCocktailColors.NO_COLOR) {
                    rgb = color;
                    break;
                }
            }
            if (rgb != ModCocktailColors.NO_COLOR) {
                int x = 69;
                int y = 14 + 18 * i;
                guiGraphics.fill(x, y, x + 5, y + 16, 0xFF000000 | rgb);
            }
        }
    }
}
