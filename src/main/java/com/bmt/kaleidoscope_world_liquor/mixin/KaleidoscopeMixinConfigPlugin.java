package com.bmt.kaleidoscope_world_liquor.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 主 mixin 配置守卫：只拦「目标类依赖可选前置」的条目，其余一律放行。
 * <p>
 * 目前唯一受管条目是 {@code ShakerRecipeCategoryMixin}——它的目标类虽然打进 tavern jar，
 * 但实现的 JEI 接口只在装了 JEI 时才在场；未装 JEI 时按 required 配置参与校验有风险，
 * 故这里显式按 JEI 在场与否决定是否应用（与 create 配置的 ContraptionMixinConfigPlugin 同思路）。
 */
public final class KaleidoscopeMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String JEI_CATEGORY_CLASS = "mezz/jei/api/recipe/category/IRecipeCategory.class";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".ShakerRecipeCategoryMixin")) {
            return resourceExists(JEI_CATEGORY_CLASS);
        }
        return true;
    }

    private static boolean resourceExists(String resourceName) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && contextLoader.getResource(resourceName) != null) {
            return true;
        }
        ClassLoader pluginLoader = KaleidoscopeMixinConfigPlugin.class.getClassLoader();
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
