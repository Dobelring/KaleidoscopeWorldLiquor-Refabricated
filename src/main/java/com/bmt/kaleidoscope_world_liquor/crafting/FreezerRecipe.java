package com.bmt.kaleidoscope_world_liquor.crafting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * 冰柜配方（record + MapCodec/StreamCodec，照 1.21.11 自定义配方范式；
 * 配方 id 由 RecipeHolder 携带，record 本体不存 id）。
 * 匹配语义与 1.20.1 一致：流体种类相同且量足够；ingredients 逐个匹配后
 * 输入槽必须全部用尽（允许空成分占位）。
 */
public record FreezerRecipe(
        Identifier fluid,
        int fluidAmount,
        List<Ingredient> ingredients,
        ItemStackTemplate result,
        int craftTime,
        Identifier resultTexture,
        java.util.Optional<Ingredient> carrier
) implements Recipe<FreezerInput> {

    public static final int MAX_INGREDIENTS = 4;

    @Override
    public boolean matches(@NotNull FreezerInput input, @NotNull Level level) {
        if (!input.fluidMatches(fluid(), fluidAmount())) {
            return false;
        }
        // 官方 1.2.0：数量相等 + 双射匹配（NeoForge RecipeMatcher 语义，回溯实现）
        java.util.List<Ingredient> required = new java.util.ArrayList<>();
        for (Ingredient ingredient : ingredients()) {
            if (!ingredient.isEmpty()) {
                required.add(ingredient);
            }
        }
        java.util.List<ItemStack> present = new java.util.ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            if (!input.getItem(i).isEmpty()) {
                present.add(input.getItem(i));
            }
        }
        if (present.size() != required.size()) {
            return false;
        }
        return required.isEmpty() || findMatches(present, required);
    }

    private static boolean findMatches(java.util.List<ItemStack> present, java.util.List<Ingredient> required) {
        boolean[] used = new boolean[required.size()];
        return matchAt(present, required, 0, used);
    }

    private static boolean matchAt(java.util.List<ItemStack> present, java.util.List<Ingredient> required, int index, boolean[] used) {
        if (index == present.size()) {
            return true;
        }
        for (int r = 0; r < required.size(); r++) {
            if (!used[r] && required.get(r).test(present.get(index))) {
                used[r] = true;
                if (matchAt(present, required, index + 1, used)) {
                    return true;
                }
                used[r] = false;
            }
        }
        return false;
    }
    public boolean canExtract(ItemStack heldItem) {
        return this.carrier.isEmpty() || this.carrier.get().test(heldItem);
    }

    public @NotNull ItemStack assemble(@NotNull FreezerInput input) {
        return this.result.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<FreezerInput>> getSerializer() {
        return ModRecipes.FREEZER_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<FreezerInput>> getType() {
        return ModRecipes.FREEZER_RECIPE_TYPE;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return ModRecipes.FREEZER_RECIPE_BOOK_CATEGORY;
    }
}
