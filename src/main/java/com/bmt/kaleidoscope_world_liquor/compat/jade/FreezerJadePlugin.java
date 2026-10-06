package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * 官方 1.1.12 新增：冰柜流体（牛奶）Jade 进度条式 tooltip 的注册入口。
 * Fabric 侧由 fabric.mod.json 的 "jade" entrypoint 一并注册（原版 Forge 靠 @WailaPlugin 扫描）。
 */
@WailaPlugin
public class FreezerJadePlugin implements IWailaPlugin {
    public static final ResourceLocation FREEZER_FLUID_UID = new ResourceLocation("kaleidoscope_world_liquor", "freezer_fluid");

    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(FreezerFluidProviders.ServerDataProvider.INSTANCE, FreezerBlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FreezerFluidProviders.TooltipProvider.INSTANCE, FreezerBlock.class);
    }
}
