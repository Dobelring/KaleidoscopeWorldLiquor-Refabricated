package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

/**
 * 客户端初始化：
 * - 牛奶流体模型（FluidModel.Unbaked：still/flowing 贴图，26.1.2 渲染层按贴图 alpha 自动判定）
 * - 渲染层无需注册：26.1.2 模型烘焙期按贴图透明度自动分层（Solid/Cutout/Translucent）
 * - BER/客户端事件接线见 ClientRenderers
 */
public final class KaleidoscopeWorldLiquorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerFluidRender();
        ClientRenderers.register();
        com.bmt.kaleidoscope_world_liquor.client.event.HostileDetectionHandler.register();
        com.bmt.kaleidoscope_world_liquor.client.render.TreasureSenseRenderer.register();
    }

    private void registerFluidRender() {
        Material still = new Material(Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "block/milk_still"));
        Material flowing = new Material(Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "block/milk_flowing"));
        // overlay/tint 均 null（tavern 自家流体同款）：牛奶不透明，无需水面覆盖与群系染色
        FluidModel.Unbaked model = new FluidModel.Unbaked(still, flowing, null,
                (BlockTintSource) null);
        FluidRenderingRegistry.register(
                com.bmt.kaleidoscope_world_liquor.init.ModFluids.MILK_STILL,
                com.bmt.kaleidoscope_world_liquor.init.ModFluids.MILK_FLOWING,
                model);
    }
}
