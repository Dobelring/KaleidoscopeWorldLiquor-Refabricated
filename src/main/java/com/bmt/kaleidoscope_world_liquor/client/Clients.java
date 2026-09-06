package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.client.renderer.BarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.renderer.BarCellarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.renderer.FreezerRenderer;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModCompatItems;
import com.bmt.kaleidoscope_world_liquor.init.ModEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public final class Clients {
   private Clients() {
   }

   public static void registerRenderers() {
      EntityRendererRegistry.register(ModEntities.OAK_LOG_STOOL, NoopRenderer::new);
      BlockEntityRenderers.register(ModBlockEntities.FREEZER_BE, FreezerRenderer::new);
      BlockEntityRenderers.register(ModBlockEntities.BAR_CABINET_BE, BarCabinetBlockEntityRender::new);
      BlockEntityRenderers.register(ModBlockEntities.BAR_CELLAR_CABINET_BE, BarCellarCabinetBlockEntityRender::new);
      // 牛奶流体渲染 handler（冰柜与世界中放置的牛奶流体均显示牛奶贴图）
      ResourceLocation milkStill = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "block/milk_still");
      ResourceLocation milkFlowing = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "block/milk_flowing");
      FluidRenderHandlerRegistry.INSTANCE.register(
         ModFluids.MILK_STILL, ModFluids.MILK_FLOWING, new SimpleFluidRenderHandler(milkStill, milkFlowing, milkStill, 0xFFFFFFFF)
      );
   }

   /** 1.21.1 原版模型 JSON 不支持 render_type 字段（Forge 扩展）→ 用 BlockRenderLayerMap 强制渲染层 */
   public static void registerRenderLayers() {
      for (int i = 0; i <= 5; i++) {
         Block doll = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "doll_" + i));
         if (doll != Blocks.AIR) {
            BlockRenderLayerMap.INSTANCE.putBlock(doll, RenderType.cutoutMipped());
         }
      }
      BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FREEZER, RenderType.cutout());
      // 酒方块贴图均含 38%~86% 透明像素：世界路径默认 solid 层不丢弃透明像素会发暗，须统一注册 cutout
      // （BE 酒柜渲染走 renderSingleBlock→cutout 不受影响，未注册时表现为"酒柜里正常、放地上发暗"）
      BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
         ModBlocks.BOMBAY_SAPPHIRE_GIN, ModBlocks.JACK_DANIEL, ModBlocks.SPIRYT_VODKA, ModBlocks.SMIRNOFF_RED_VODKA, ModBlocks.ABSOLUT_VODKA,
         ModBlocks.PINA_COLADA, ModBlocks.MAOTAI, ModBlocks.BACARDI_CARTA_BLANCA, ModBlocks.SPIRYT_VODKA,
         ModBlocks.SKYY_VODKA, ModBlocks.JOHNNIE_WALKER, ModBlocks.LAFITE_1982, ModBlocks.STRONGBOW,
         ModBlocks.DASSAI, ModBlocks.KWAS_CHLEBOWY, ModBlocks.BAMBOO_LEAF_GREEN_LIQUOR, ModBlocks.COOL_TEA,
         ModBlocks.SOUR_PLUM, ModBlocks.JERK, ModBlocks.AROUND_THE_WORLD, ModBlocks.LONG_ISLAND_ICED_TEA,
         ModBlocks.SHRIMP_COOKTAIL, ModBlocks.GIN_TONIC,
         ModCompatItems.ICE_TEA_BLOCK
      );
   }
}
