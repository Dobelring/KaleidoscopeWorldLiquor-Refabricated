package com.bmt.kaleidoscope_world_liquor.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 主 mixin 配置守卫：只对「目标类属可选依赖」的 mixin 做在场判断。
 * <p>
 * 目前只有一条——{@code ShakerRecipeCategoryMixin} 打的是 tavern 的 JEI 分类类，
 * 该类实现 {@code mezz.jei.api.recipe.IRecipeCategory}；JEI 是 compileOnly（可选），
 * 缺席时目标类加载不了会让整份配置 required 失败，故按 JEI 类资源在场与否跳过。
 * 其余 mixin 的目标都是硬依赖（原版 / tavern），不在场就该直接崩，不做静默吞错。
 */
public final class KaleidoscopeWorldLiquorMixinPlugin implements IMixinConfigPlugin {
    private static final String JEI_RECIPE_CATEGORY = "mezz/jei/api/recipe/category/IRecipeCategory.class";
    private static final String SHAKER_RECIPE_CATEGORY_MIXIN =
            "com.bmt.kaleidoscope_world_liquor.mixin.ShakerRecipeCategoryMixin";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (SHAKER_RECIPE_CATEGORY_MIXIN.equals(mixinClassName)) {
            return resourceExists(JEI_RECIPE_CATEGORY)
                    && resourceExists(targetClassName.replace('.', '/') + ".class");
        }
        return true;
    }

    private static boolean resourceExists(String resourceName) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && contextLoader.getResource(resourceName) != null) {
            return true;
        }
        ClassLoader pluginLoader = KaleidoscopeWorldLiquorMixinPlugin.class.getClassLoader();
        return pluginLoader != null && pluginLoader.getResource(resourceName) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
