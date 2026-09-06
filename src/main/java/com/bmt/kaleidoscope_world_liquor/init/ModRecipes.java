package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipeSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
   public static final FreezerRecipeSerializer FREEZER_SERIALIZER = new FreezerRecipeSerializer();
   public static final RecipeType<FreezerRecipe> FREEZER_TYPE = new RecipeType<FreezerRecipe>() {};

   public static void registerRecipes() {
      Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "freezer"), FREEZER_SERIALIZER);
      Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "freezer"), FREEZER_TYPE);
   }
}
