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
    /** 官方 1.1.11：冰柜牛奶流体 tooltip 组件 */
    public static final Identifier FREEZER_FLUID_UID = Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "freezer_fluid");

    @Override
    public void register(IWailaCommonRegistration registration) {
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FreezerComponentProvider.INSTANCE, FreezerBlock.class);
        // 官方 1.1.11：冰柜牛奶流体 tooltip（服务端数据由 Jade 自带流体同步提供，无 ServerDataProvider）
        registration.registerBlockComponent(FreezerFluidProviders.TooltipProvider.INSTANCE, FreezerBlock.class);
    }
}
