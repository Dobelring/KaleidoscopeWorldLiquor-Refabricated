package com.bmt.kaleidoscope_world_liquor.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 官方 1.1.11：沙砾类容器（BrushableBlockEntity）的战利品表判定。
 * 26.x 的 {@code lootTable} 是 {@code ResourceKey<LootTable>} 且为 private，无 getter，故走 accessor。
 */
@Mixin(BrushableBlockEntity.class)
public interface BrushableBlockEntityAccessor {
    @Accessor("lootTable")
    @Nullable
    ResourceKey<LootTable> kwl$getLootTable();
}
