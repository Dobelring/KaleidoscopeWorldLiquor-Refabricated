package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;

/**
 * 客户端初始化：
 * - 牛奶流体渲染 handler（still/flowing 贴图 + overlay 水面覆盖）
 * - 24 个酒方块（17 名酒 + 冰红茶/酸梅汤矮罐 + smc:ice_tea）+ 6 鸡尾酒
 *   全部 CUTOUT（贴图含 38%~86% 透明像素，solid 层会发暗——1.21.1 实测结论）
 */
public final class KaleidoscopeWorldLiquorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerFluidRender();
        registerRenderLayers();
        ClientRenderers.register();
    }

    private void registerFluidRender() {
        FluidRenderHandlerRegistry.INSTANCE.register(
                com.bmt.kaleidoscope_world_liquor.init.ModFluids.MILK_STILL,
                com.bmt.kaleidoscope_world_liquor.init.ModFluids.MILK_FLOWING,
                new SimpleFluidRenderHandler(
                        Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "block/milk_still"),
                        Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "block/milk_flowing"),
                        SimpleFluidRenderHandler.WATER_OVERLAY
                ));
    }

    private void registerRenderLayers() {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
                // 17 名酒 + 2 矮罐
                ModBlocks.BOMBAY_SAPPHIRE_GIN, ModBlocks.JACK_DANIEL, ModBlocks.SMIRNOFF_RED_VODKA,
                ModBlocks.ABSOLUT_VODKA, ModBlocks.PINA_COLADA, ModBlocks.MAOTAI,
                ModBlocks.BACARDI_CARTA_BLANCA, ModBlocks.SPIRYT_VODKA, ModBlocks.SKYY_VODKA,
                ModBlocks.JOHNNIE_WALKER, ModBlocks.LAFITE_1982, ModBlocks.STRONGBOW,
                ModBlocks.DASSAI, ModBlocks.KWAS_CHLEBOWY, ModBlocks.BAMBOO_LEAF_GREEN_LIQUOR,
                ModBlocks.COOL_TEA, ModBlocks.SOUR_PLUM,
                // 6 鸡尾酒
                ModBlocks.JERK, ModBlocks.AROUND_THE_WORLD, ModBlocks.LONG_ISLAND_ICED_TEA,
                ModBlocks.SHRIMP_COOKTAIL, ModBlocks.PINE_COLADA, ModBlocks.GIN_TONIC,
                // smc 联动
                com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems.SMC_ICE_TEA_BLOCK,
                // 冰柜（fridge_close/fridge_open 模型同样声明 render_type）
                ModBlocks.FREEZER
        );
        // 11 个酒柜/酒窖柜（base_bar_cabinet/base_cellar_cabinet 模型声明 render_type: cutout；
        // solid 层下透明像素渲染为黑色——联排内侧左右面、叠放内侧上下面发黑即此因）
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
                ModBlocks.OAK_BAR_CABINET, ModBlocks.OAK_GLASS_BAR_CABINET, ModBlocks.BIRCH_BAR_CABINET,
                ModBlocks.SPRUCE_BAR_CABINET, ModBlocks.DARK_OAK_BAR_CABINET, ModBlocks.CHERRY_BAR_CABINET,
                ModBlocks.OAK_CELLAR_CABINET, ModBlocks.BIRCH_CELLAR_CABINET, ModBlocks.SPRUCE_CELLAR_CABINET,
                ModBlocks.DARK_OAK_CELLAR_CABINET, ModBlocks.CHERRY_CELLAR_CABINET);
        // 6 玩偶（1.21.11 模型 JSON 的 render_type 已失效，渲染层按方块注册；
        // 1.20.1 模型声明 cutout_mipped，贴图带透明像素，solid 层会发糊）
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
                com.bmt.kaleidoscope_world_liquor.init.DollIntegration.DOLL_BLOCKS.toArray(new net.minecraft.world.level.block.Block[0]));
    }
}
