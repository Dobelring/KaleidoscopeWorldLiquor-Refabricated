package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 官方 1.2.0 mixins/smc/LootItemAccessor：物品条目的 item 字段只读访问。 */
@Mixin(LootItem.class)
public interface LootItemAccessor {
    @Accessor("item")
    Item kwl$getItem();
}
