package com.bmt.kaleidoscope_world_liquor.mixin.accessor;

import java.util.List;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 官方 1.2.0 mixins/smc/CompositeEntryBaseAccessor：复合条目的 children 只读访问（递归下钻）。 */
@Mixin(CompositeEntryBase.class)
public interface CompositeEntryBaseAccessor {
    @Accessor("children")
    List<LootPoolEntryContainer> kwl$getChildren();
}
