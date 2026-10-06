package com.bmt.kaleidoscope_world_liquor.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 官方 1.1.11：宝藏感知服务端扫描需要读饰纹陶罐（悬顶沙）的战利品表。
 * 26.x 的 {@code BrushableBlockEntity#lootTable} 是 {@code ResourceKey<LootTable>} 且 private。
 */
@Mixin(BrushableBlockEntity.class)
public interface BrushableBlockEntityAccessor {
    @Accessor("lootTable")
    @Nullable
    ResourceKey<LootTable> kwl$getLootTable();
}
