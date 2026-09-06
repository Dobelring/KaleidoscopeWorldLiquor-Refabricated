package com.bmt.kaleidoscope_world_liquor.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
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
        ItemStack result,
        int craftTime,
        Identifier resultTexture,
        java.util.Optional<Ingredient> extractIngredient
) implements Recipe<FreezerInput> {

    public static final int MAX_INGREDIENTS = 4;

    @Override
    public boolean matches(@NotNull FreezerInput input, @NotNull Level level) {
        if (!input.fluidMatches(fluid(), fluidAmount())) {
            return false;
        }
        boolean[] used = new boolean[input.size()];
        for (Ingredient ingredient : ingredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            boolean matched = false;
            for (int i = 0; i < input.size(); i++) {
                if (!used[i] && ingredient.test(input.getItem(i))) {
                    used[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        for (int i = 0; i < input.size(); i++) {
            if (!input.getItem(i).isEmpty() && !used[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean canExtract(ItemStack heldItem) {
        return this.extractIngredient.isEmpty() || this.extractIngredient.get().test(heldItem);
    }

    public @NotNull ItemStack assemble(@NotNull FreezerInput input, HolderLookup.@NotNull Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.result;
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
