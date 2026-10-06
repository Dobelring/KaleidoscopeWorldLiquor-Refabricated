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
 * 官方 1.1.11 调色 mixin 之四（目标：tavern JEI 雪克杯配方分类）：
 * draw 尾部给含自定义 4 色的原料补画色块（tavern 自身的 ingredientColors 只认
 * COCKTAIL_INGREDIENT_COLORS 里的原版 tag，这 4 个新色查不到会被漏画）。
 * <p>
 * 26.x 适配：draw 有泛型桥接重载，用全 descriptor 精确匹配（remap=false，
 * 本工程无 refmap、运行期即 mojmap 名）；坐标对齐本分支 tavern 的
 * {@code renderIngredientColor}（x=69 宽 5；官方 1.21.1 的 66/8 是旧布局）。
 */
@Mixin(ShakerRecipeCategory.class)
public abstract class ShakerRecipeCategoryMixin {

    @Inject(
            method = "draw(Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphicsExtractor;DD)V",
            remap = false,
            at = @At("TAIL")
    )
    private void kwl$drawExtraColorBlocks(RecipeHolder<ShakerRecipe> holder, IRecipeSlotsView recipeSlotsView,
                                          GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY, CallbackInfo ci) {
        NonNullList<Ingredient> ingredients = holder.value().ingredients();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
                continue;
            }
            int rgb = ModCocktailColors.NO_COLOR;
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
