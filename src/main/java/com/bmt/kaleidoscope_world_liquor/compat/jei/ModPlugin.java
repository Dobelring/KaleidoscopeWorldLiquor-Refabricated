package com.bmt.kaleidoscope_world_liquor.compat.jei;

import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ModPlugin implements IModPlugin {
   private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "jei_plugin");

   @NotNull
   public ResourceLocation getPluginUid() {
      return PLUGIN_ID;
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      registration.addRecipeCategories(new IRecipeCategory[]{new FreezerCategory(registration.getJeiHelpers().getGuiHelper())});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      registration.addRecipes(FreezerCategory.TYPE, FreezerCategory.getRecipes());
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(ModBlocks.FREEZER.asItem().getDefaultInstance(), FreezerCategory.TYPE);
   }
}
