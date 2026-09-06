package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipeSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipes {
    private ModRecipes() {
    }

    public static final RecipeSerializer<FreezerRecipe> FREEZER_SERIALIZER = new FreezerRecipeSerializer();
    public static final RecipeType<FreezerRecipe> FREEZER_RECIPE_TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return KaleidoscopeWorldLiquor.MOD_ID + ":freezer";
        }
    };
    public static final RecipeBookCategory FREEZER_RECIPE_BOOK_CATEGORY = new RecipeBookCategory();

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("freezer"), FREEZER_SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, id("freezer"), FREEZER_RECIPE_TYPE);
        Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, id("freezer"), FREEZER_RECIPE_BOOK_CATEGORY);
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, name);
    }
}
