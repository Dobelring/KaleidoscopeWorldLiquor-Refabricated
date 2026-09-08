package com.bmt.kaleidoscope_world_liquor.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 8 幅作者画的物品（方块在 ModBlocks.paintingReg，均为 kaleidoscope_tavern 命名空间）。
 * 物品=AuthorPaintingItem：背包 tooltip 追加作者行（tooltip.kaleidoscope_tavern.<画名>）；
 * 名称统一"挂画"；WTHIT 指向已放置方块不走物品 tooltip，故放出后不显示作者
 * （用户拍板 2026-09-08）。手持渲染=物品 generated 模型+画贴图，与 tavern 自带画一致。
 */
public final class ModPaintings {
    private ModPaintings() {
    }

    public static final Item BFXM_PAINTING = registerPaintingItem("bfxm_painting", ModBlocks.BFXM_PAINTING);
    public static final Item BMT_PAINTING = registerPaintingItem("bmt_painting", ModBlocks.BMT_PAINTING);
    public static final Item DREAM_PAINTING = registerPaintingItem("dream_painting", ModBlocks.DREAM_PAINTING);
    public static final Item CHA_PAINTING = registerPaintingItem("cha_painting", ModBlocks.CHA_PAINTING);
    public static final Item CHEN_PAINTING = registerPaintingItem("chen_painting", ModBlocks.CHEN_PAINTING);
    public static final Item RABBIT_PAINTING = registerPaintingItem("rabbit_painting", ModBlocks.RABBIT_PAINTING);
    public static final Item CH_PAINTING = registerPaintingItem("ch_painting", ModBlocks.CH_PAINTING);
    public static final Item QXXY_PAINTING = registerPaintingItem("qxxy_painting", ModBlocks.QXXY_PAINTING);

    private static Item registerPaintingItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("kaleidoscope_tavern", name));
        Item item = new com.bmt.kaleidoscope_world_liquor.item.AuthorPaintingItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(key));
        ((BlockItem) item).registerBlocks(Item.BY_BLOCK, item);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void register() {
    }
}
