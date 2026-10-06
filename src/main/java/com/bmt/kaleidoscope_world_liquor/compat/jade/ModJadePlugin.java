package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import net.minecraft.resources.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ModJadePlugin implements IWailaPlugin {
    public static final Identifier FREEZER = Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "freezer");
    /** 官方 1.1.11：冰柜牛奶流体 tooltip 的配置/组件 uid */
    public static final Identifier FREEZER_FLUID_UID = Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "freezer_fluid");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // 官方 neo 版同样没有 ServerDataProvider——依赖 Jade 自带的流体同步
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FreezerComponentProvider.INSTANCE, FreezerBlock.class);
        // 官方 1.1.11：冰柜牛奶流体 tooltip（自定义奶贴图进度条 + 覆盖默认流体条）
        registration.registerBlockComponent(FreezerFluidProviders.TooltipProvider.INSTANCE, FreezerBlock.class);
    }
}
