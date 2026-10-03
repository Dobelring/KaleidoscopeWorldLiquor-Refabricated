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
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

// 插件入口点已在 fabric.mod.json 的 jei_mod_plugin 中配置；
// Fabric 版 JEI 只走入口点发现（不扫注解），@JeiPlugin 保留以贴近官方源码
@JeiPlugin
public class Plugins implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation("kaleidoscope_world_liquor:jei");

    public Plugins() {
    }

    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new IRecipeCategory[]{new FreezerCategory(registration.getJeiHelpers().getGuiHelper())});
    }

    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(FreezerCategory.TYPE, FreezerCategory.getRecipes());
    }

    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst((ItemLike)ModBlocks.FREEZER, new RecipeType[]{FreezerCategory.TYPE});
    }

    @NotNull
    public ResourceLocation getPluginUid() {
        return UID;
    }
}
