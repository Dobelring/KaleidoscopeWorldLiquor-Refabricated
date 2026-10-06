package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.util.ModCocktailColors;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.ShakerRecipeCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 官方 1.1.11 JEI 雪克配方补色块：tavern 的 {@code draw} 只画它那 16 色 tag 映射里的颜色，
 * 本模组新增的 4 色（brown/orange/light_blue/pink）需要在配方格右侧补画。
 * <p>
 * 坐标与 tavern 自家 {@code renderIngredientColor} 对齐（x=69 起宽 5，贴图方框内部），
 * 避免官方 1.21.1 的 66/8 在 26.3 贴图上偏左盖框。
 */
@Mixin(ShakerRecipeCategory.class)
public abstract class ShakerRecipeCategoryMixin {

    @Inject(method = "draw", remap = false, at = @At("TAIL"))
    private void kwl$drawExtraColorBlocks(RecipeHolder<ShakerRecipe> recipe, IRecipeSlotsView recipeSlotsView,
                                          GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY, CallbackInfo ci) {
        var ingredients = recipe.value().ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
                continue;
            }
            int rgb = ModCocktailColors.NO_COLOR;
            // 26.3 的 Ingredient 没有 getItems()，改用 items()（Stream<Holder<Item>>）
            for (var itemHolder : ingredient.items().toList()) {
                int color = ModCocktailColors.getCustomColor(itemHolder.value());
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
