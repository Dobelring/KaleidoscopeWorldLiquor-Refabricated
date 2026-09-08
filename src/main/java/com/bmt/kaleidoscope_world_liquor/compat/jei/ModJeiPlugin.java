package com.bmt.kaleidoscope_world_liquor.compat.jei;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "jei");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FreezerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(FreezerCategory.TYPE, FreezerCategory.getRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.FREEZER, FreezerCategory.TYPE);
    }

    public static void syncRecipes() {
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipes.FREEZER_SERIALIZER);
    }

    @Override
    public @NotNull Identifier getPluginUid() {
        return UID;
    }
}
