package com.bmt.kaleidoscope_world_liquor.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 tavern 的两个 tab 排到本模组 tab 紧后面，对应官方挂 NeoForge CreativeModeTabRegistry 的那份 mixin。
 * 原版/Fabric 的 tab 顺序就是注册顺序，tavern 是依赖、其入口先跑，所以要显式重排。
 * 原版创造界面（布局/渲染/点击）走的是 CreativeModeTabs#tabs()，allTabs() 只用于刷新判定，无需重排。
 */
@Mixin(value = CreativeModeTabs.class, priority = 800)
public abstract class CreativeModeTabsMixin {
    @ModifyReturnValue(method = "tabs", at = @At("RETURN"))
    private static List<CreativeModeTab> kwl$moveTavernTabsAfterLiquor(List<CreativeModeTab> tabs) {
        CreativeModeTab ourTab = tab("kaleidoscope_world_liquor", "kaleidoscope_world_liquor_tab");
        CreativeModeTab tavernMain = tab("kaleidoscope_tavern", "tavern_main");
        CreativeModeTab tavernDeco = tab("kaleidoscope_tavern", "tavern_deco");
        if (ourTab == null || tavernMain == null) {
            return tabs;
        }

        int ourIndex = tabs.indexOf(ourTab);
        int mainIndex = tabs.indexOf(tavernMain);
        int decoIndex = tavernDeco == null ? -1 : tabs.indexOf(tavernDeco);
        if (ourIndex == -1 || mainIndex == -1) {
            return tabs;
        }
        if (mainIndex == ourIndex + 1 && (decoIndex == -1 || decoIndex == ourIndex + 2)) {
            return tabs;
        }

        List<CreativeModeTab> result = new ArrayList<>(tabs);
        result.remove(tavernMain);
        if (decoIndex != -1) {
            result.remove(tavernDeco);
        }

        ourIndex = result.indexOf(ourTab);
        result.add(ourIndex + 1, tavernMain);
        if (decoIndex != -1) {
            result.add(ourIndex + 2, tavernDeco);
        }
        return result;
    }

    private static CreativeModeTab tab(String namespace, String path) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getValue(Identifier.fromNamespaceAndPath(namespace, path));
    }
}
