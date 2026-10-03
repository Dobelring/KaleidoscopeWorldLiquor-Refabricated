package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.github.ysbbbbbb.kaleidoscopetavern.block.deco.PaintingBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;

/**
 * 8 幅作者挂画注册进 kaleidoscope_tavern 命名空间（与官方 Forge 版一致；
 * 本 jar 同时携带这 8 幅画的 assets/kaleidoscope_tavern 资源）。
 */
public class ModPaintings {
    public static final Block BFXM_PAINTING = new PaintingBlock();
    public static final Block BMT_PAINTING = new PaintingBlock();
    public static final Block DREAM_PAINTING = new PaintingBlock();
    public static final Block CHA_PAINTING = new PaintingBlock();
    public static final Block CHEN_PAINTING = new PaintingBlock();
    public static final Block RABBIT_PAINTING = new PaintingBlock();
    public static final Block CH_PAINTING = new PaintingBlock();
    public static final Block QXXY_PAINTING = new PaintingBlock();
    public static final Item BFXM_PAINTING_ITEM = new BlockItem(BFXM_PAINTING, new Properties());
    public static final Item BMT_PAINTING_ITEM = new BlockItem(BMT_PAINTING, new Properties());
    public static final Item DREAM_PAINTING_ITEM = new BlockItem(DREAM_PAINTING, new Properties());
    public static final Item CHA_PAINTING_ITEM = new BlockItem(CHA_PAINTING, new Properties());
    public static final Item CHEN_PAINTING_ITEM = new BlockItem(CHEN_PAINTING, new Properties());
    public static final Item RABBIT_PAINTING_ITEM = new BlockItem(RABBIT_PAINTING, new Properties());
    public static final Item CH_PAINTING_ITEM = new BlockItem(CH_PAINTING, new Properties());
    public static final Item QXXY_PAINTING_ITEM = new BlockItem(QXXY_PAINTING, new Properties());

    public ModPaintings() {
    }

    private static ResourceLocation paintingId(String name) {
        return new ResourceLocation("kaleidoscope_tavern", name);
    }

    public static void registerPaintings() {
        Registry.register(BuiltInRegistries.BLOCK, paintingId("bfxm_painting"), BFXM_PAINTING);
        Registry.register(BuiltInRegistries.BLOCK, paintingId("bmt_painting"), BMT_PAINTING);
        Registry.register(BuiltInRegistries.BLOCK, paintingId("dream_painting"), DREAM_PAINTING);
        Registry.register(BuiltInRegistries.BLOCK, paintingId("cha_painting"), CHA_PAINTING);
        Registry.register(BuiltInRegistries.BLOCK, paintingId("chen_painting"), CHEN_PAINTING);
        Registry.register(BuiltInRegistries.BLOCK, paintingId("rabbit_painting"), RABBIT_PAINTING);
        Registry.register(BuiltInRegistries.BLOCK, paintingId("ch_painting"), CH_PAINTING);
        Registry.register(BuiltInRegistries.BLOCK, paintingId("qxxy_painting"), QXXY_PAINTING);
        Registry.register(BuiltInRegistries.ITEM, paintingId("bfxm_painting"), BFXM_PAINTING_ITEM);
        Registry.register(BuiltInRegistries.ITEM, paintingId("bmt_painting"), BMT_PAINTING_ITEM);
        Registry.register(BuiltInRegistries.ITEM, paintingId("dream_painting"), DREAM_PAINTING_ITEM);
        Registry.register(BuiltInRegistries.ITEM, paintingId("cha_painting"), CHA_PAINTING_ITEM);
        Registry.register(BuiltInRegistries.ITEM, paintingId("chen_painting"), CHEN_PAINTING_ITEM);
        Registry.register(BuiltInRegistries.ITEM, paintingId("rabbit_painting"), RABBIT_PAINTING_ITEM);
        Registry.register(BuiltInRegistries.ITEM, paintingId("ch_painting"), CH_PAINTING_ITEM);
        Registry.register(BuiltInRegistries.ITEM, paintingId("qxxy_painting"), QXXY_PAINTING_ITEM);
    }
}
