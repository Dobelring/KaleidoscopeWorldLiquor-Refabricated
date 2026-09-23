package com.bmt.kaleidoscope_world_liquor.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Create 联动那组 mixin 的守卫（官方同名类的移植）。
 * <p>
 * 未装 Create（Fabric 侧由 Create Fly 提供）时整份配置不生效，避免目标类不存在导致启动崩溃。
 * 官方判的是 {@code com/simibubi/create/...}，这里改成 Create Fly 的
 * {@code com/zurrtum/create/...}。
 */
public final class ContraptionMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String CREATE_CONTRAPTION_CLASS =
            "com/zurrtum/create/content/contraptions/AbstractContraptionEntity.class";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return resourceExists(CREATE_CONTRAPTION_CLASS)
                && resourceExists(targetClassName.replace('.', '/') + ".class");
    }

    private static boolean resourceExists(String resourceName) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && contextLoader.getResource(resourceName) != null) {
            return true;
        }
        ClassLoader pluginLoader = ContraptionMixinConfigPlugin.class.getClassLoader();
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
