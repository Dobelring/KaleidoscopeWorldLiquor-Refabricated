package com.bmt.kaleidoscope_world_liquor.mixins;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class CreateMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String CREATE_CLASS = "com/simibubi/create/content/contraptions/Contraption.class";

    public CreateMixinConfigPlugin() {
    }

    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !resourceExists("com/simibubi/create/content/contraptions/Contraption.class")
            ? false
            : resourceExists(targetClassName.replace('.', '/') + ".class");
    }

    private static boolean resourceExists(String resourceName) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && contextLoader.getResource(resourceName) != null) {
            return true;
        } else {
            ClassLoader pluginLoader = CreateMixinConfigPlugin.class.getClassLoader();
            return pluginLoader != null && pluginLoader.getResource(resourceName) != null;
        }
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return Collections.emptyList();
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
