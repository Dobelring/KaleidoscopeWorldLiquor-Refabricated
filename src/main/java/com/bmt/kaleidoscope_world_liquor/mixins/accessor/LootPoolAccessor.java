package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 官方 1.2.0 mixins/smc/LootPoolAccessor：池内条目数组只读访问。 */
@Mixin(LootPool.class)
public interface LootPoolAccessor {
    @Accessor("entries")
    LootPoolEntryContainer[] kwl$getEntries();
}
