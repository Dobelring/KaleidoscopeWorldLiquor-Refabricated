package com.bmt.kaleidoscope_world_liquor.init.smc;

import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 星喵工艺联动：smc:ice_tea。1.20.1 即无条件注册（smc 模组本体无此物品，不冲突）。
 */
public final class SMCItems {
    public static final String SMC_MODID = "smc";

    private static ResourceKey<Block> blockId(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(SMC_MODID, name));
    }

    public static final Block SMC_ICE_TEA_BLOCK = Registry.register(BuiltInRegistries.BLOCK,
            blockId("ice_tea"),
            DrinkBlock.create()
                    .setId(blockId("ice_tea"))
                    .maxCount(4)
                    .shapes(ModBlocks.canShapes(14.0))
                    .build());

    private static final ResourceKey<Item> ICE_TEA_ITEM_ID = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SMC_MODID, "ice_tea"));

    public static final Item SMC_ICE_TEA_ITEM = Registry.register(BuiltInRegistries.ITEM,
            ICE_TEA_ITEM_ID,
            withBlock(new DrinkBlockItem(SMC_ICE_TEA_BLOCK, new Item.Properties().useBlockDescriptionPrefix().stacksTo(16).setId(ICE_TEA_ITEM_ID))));

    private static Item withBlock(DrinkBlockItem item) {
        item.registerBlocks(Item.BY_BLOCK, item);
        return item;
    }

    private SMCItems() {
    }

    public static void register() {
    }
}
