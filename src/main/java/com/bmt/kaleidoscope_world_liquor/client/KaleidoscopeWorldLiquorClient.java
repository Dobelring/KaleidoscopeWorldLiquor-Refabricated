package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.client.renderer.BarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.renderer.BarCellarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.renderer.FreezerRender;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * 客户端入口点。
 * <p>
 * 原 Forge 的 ClientSetup（RegisterRenderers 事件）与各客户端事件类的注册内容
 * 全部合并到这里（Fabric 无 mod 总线事件）。
 */
@Environment(EnvType.CLIENT)
public class KaleidoscopeWorldLiquorClient implements ClientModInitializer {
    /**
     * 需要 CUTOUT 渲染层的方块 id（模型声明了 render_type=cutout 的，外加官方漏声明
     * render_type 但贴图带透明像素的 5 个名酒：jack_daniel/maotai/pina_colada/
     * kwas_chlebowy/lafite_1982 —— 行为规格 §52 要求 24 酒方块全部 CUTOUT）。
     * 用 id 查表而非直接引字段：玩偶 doll_0..5 与 smc:ice_tea 是条件注册，可能不存在。
     */
    private static final String[] CUTOUT_BLOCKS = {
        "kaleidoscope_world_liquor:absolut_vodka",
        "kaleidoscope_world_liquor:bacardi_carta_blanca",
        "kaleidoscope_world_liquor:bamboo_leaf_green_liquor",
        "kaleidoscope_world_liquor:bar_stool_black",
        "kaleidoscope_world_liquor:bar_stool_blue",
        "kaleidoscope_world_liquor:bar_stool_brown",
        "kaleidoscope_world_liquor:bar_stool_cyan",
        "kaleidoscope_world_liquor:bar_stool_gray",
        "kaleidoscope_world_liquor:bar_stool_green",
        "kaleidoscope_world_liquor:bar_stool_light_blue",
        "kaleidoscope_world_liquor:bar_stool_light_gray",
        "kaleidoscope_world_liquor:bar_stool_lime",
        "kaleidoscope_world_liquor:bar_stool_magenta",
        "kaleidoscope_world_liquor:bar_stool_orange",
        "kaleidoscope_world_liquor:bar_stool_pink",
        "kaleidoscope_world_liquor:bar_stool_purple",
        "kaleidoscope_world_liquor:bar_stool_red",
        "kaleidoscope_world_liquor:bar_stool_white",
        "kaleidoscope_world_liquor:bar_stool_yellow",
        "kaleidoscope_world_liquor:birch_bar_cabinet",
        "kaleidoscope_world_liquor:birch_cellar_cabinet",
        "kaleidoscope_world_liquor:bombay_sapphire_gin",
        "kaleidoscope_world_liquor:cherry_bar_cabinet",
        "kaleidoscope_world_liquor:cherry_cellar_cabinet",
        "kaleidoscope_world_liquor:cool_tea",
        "kaleidoscope_world_liquor:dark_oak_bar_cabinet",
        "kaleidoscope_world_liquor:dark_oak_cellar_cabinet",
        "kaleidoscope_world_liquor:dassai",
        "kaleidoscope_world_liquor:freezer",
        "kaleidoscope_world_liquor:jack_daniel",
        "kaleidoscope_world_liquor:johnnie_walker",
        "kaleidoscope_world_liquor:kwas_chlebowy",
        "kaleidoscope_world_liquor:lafite_1982",
        "kaleidoscope_world_liquor:maotai",
        "kaleidoscope_world_liquor:oak_bar_cabinet",
        "kaleidoscope_world_liquor:oak_cellar_cabinet",
        "kaleidoscope_world_liquor:oak_glass_bar_cabinet",
        "kaleidoscope_world_liquor:pina_colada",
        "kaleidoscope_world_liquor:skyy_vodka",
        "kaleidoscope_world_liquor:smirnoff_red_vodka",
        "kaleidoscope_world_liquor:sour_plum",
        "kaleidoscope_world_liquor:spiryt_vodka",
        "kaleidoscope_world_liquor:spruce_bar_cabinet",
        "kaleidoscope_world_liquor:spruce_cellar_cabinet",
        "kaleidoscope_world_liquor:strongbow",
        "kaleidoscope_world_liquor:wall_record",
        "kaleidoscope_world_liquor:jungle_bar_cabinet",
        "kaleidoscope_world_liquor:jungle_cellar_cabinet",
        "kaleidoscope_world_liquor:acacia_bar_cabinet",
        "kaleidoscope_world_liquor:acacia_cellar_cabinet",
        "kaleidoscope_world_liquor:mangrove_bar_cabinet",
        "kaleidoscope_world_liquor:mangrove_cellar_cabinet",
        "kaleidoscope_world_liquor:bamboo_bar_cabinet",
        "kaleidoscope_world_liquor:bamboo_cellar_cabinet",
        "kaleidoscope_world_liquor:crimson_bar_cabinet",
        "kaleidoscope_world_liquor:crimson_cellar_cabinet",
        "kaleidoscope_world_liquor:warped_bar_cabinet",
        "kaleidoscope_world_liquor:warped_cellar_cabinet",
        "smc:ice_tea"
    };
    /** 玩偶：模型声明 cutout_mipped；doll 模组未装时不注册，查表跳过。 */
    private static final String[] CUTOUT_MIPPED_BLOCKS = {
        "kaleidoscope_world_liquor:doll_0",
        "kaleidoscope_world_liquor:doll_1",
        "kaleidoscope_world_liquor:doll_2",
        "kaleidoscope_world_liquor:doll_3",
        "kaleidoscope_world_liquor:doll_4",
        "kaleidoscope_world_liquor:doll_5"
    };
    /** 鸡尾酒：模型声明 translucent。 */
    private static final String[] TRANSLUCENT_BLOCKS = {
        "kaleidoscope_world_liquor:around_the_world",
        "kaleidoscope_world_liquor:gin_tonic",
        "kaleidoscope_world_liquor:highball",
        "kaleidoscope_world_liquor:jerk",
        "kaleidoscope_world_liquor:long_island_iced_tea",
        "kaleidoscope_world_liquor:pine_colada",
        "kaleidoscope_world_liquor:shrimp_cocktail"
    };

    public KaleidoscopeWorldLiquorClient() {
    }

    @Override
    public void onInitializeClient() {
        // 方块实体渲染器（原 ClientSetup.registerRenderers / RegisterRenderers 事件）
        BlockEntityRenderers.register(ModBlockEntities.FREEZER_BE, FreezerRender::new);
        BlockEntityRenderers.register(ModBlockEntities.BAR_CABINET_BE, BarCabinetBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlockEntities.BAR_CELLAR_CABINET_BE, BarCellarCabinetBlockEntityRender::new);

        // 渲染层（原版忽略模型里的 render_type 字段，Fabric 需按方块登记）
        registerRenderLayers();

        // 牛奶流体渲染（原 Forge FluidType.initializeClient 提供贴图）
        registerFluidRenderers();

        // 客户端事件（原 ClientForgeEvents / CameraAnglesEvent / TreasureSenseRender 等）
        com.bmt.kaleidoscope_world_liquor.client.event.ClientForgeEvents.register();
        com.bmt.kaleidoscope_world_liquor.client.event.CameraAnglesEvent.register();

        // 原嵌套 ClientEventHandler（RenderTickEvent END）→ END_CLIENT_TICK（敌对生物发光标记刷新）
        com.bmt.kaleidoscope_world_liquor.event.EventHandlers.ClientEventHandler.register();
        // 唱片 tooltip（原 ItemTooltipEvent，客户端 API）
        com.bmt.kaleidoscope_world_liquor.event.MusicDiscEvents.registerClient();
        // 创造栏左侧两个分类按钮（原 Forge CreativeTabFilter，ScreenEvents 版）
        com.bmt.kaleidoscope_world_liquor.client.creativetab.CreativeTabFilter.register();

        // twilight 四食物 tooltip（原 init.kaleidoscope_twilight.TooltipEvents，@EventBusSubscriber Dist.CLIENT）
        com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.TooltipEvents.register();

        // Ponder 场景（原 compat.ponder.init.ClientSetupEvents，Forge mod 总线事件）
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("create")) {
            com.bmt.kaleidoscope_world_liquor.compat.ponder.init.ClientSetupEvents.register();
        }
    }

    private static void registerRenderLayers() {
        for (String id : CUTOUT_BLOCKS) putLayer(id, RenderType.cutout());
        for (String id : CUTOUT_MIPPED_BLOCKS) putLayer(id, RenderType.cutoutMipped());
        for (String id : TRANSLUCENT_BLOCKS) putLayer(id, RenderType.translucent());
    }

    private static void putLayer(String id, RenderType layer) {
        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(id));
        if (block != null && block != Blocks.AIR) {
            BlockRenderLayerMap.INSTANCE.putBlock(block, layer);
        }
    }

    private static void registerFluidRenderers() {
        ResourceLocation still = new ResourceLocation("kaleidoscope_world_liquor", "block/milk_still");
        ResourceLocation flowing = new ResourceLocation("kaleidoscope_world_liquor", "block/milk_flowing");
        SimpleFluidRenderHandler handler = new SimpleFluidRenderHandler(still, flowing, 0xFFFFFFFF);
        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.MILK_STILL, handler);
        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.MILK_FLOWING, handler);
    }
}
