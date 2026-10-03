package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.compat.jade.block.FreezerComponentProvider;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

// 插件入口点已在 fabric.mod.json 的 jade 中配置；
// @WailaPlugin 注解在 Fabric 版 Jade 中不会被扫描（仅 Fabric 入口点生效），保留以贴近官方源码
@WailaPlugin
public class ModPlugin implements IWailaPlugin {
    public static final ResourceLocation FREEZER = new ResourceLocation("kaleidoscope_world_liquor", "freezer");

    public ModPlugin() {
    }

    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FreezerComponentProvider.INSTANCE, FreezerBlock.class);
    }
}
