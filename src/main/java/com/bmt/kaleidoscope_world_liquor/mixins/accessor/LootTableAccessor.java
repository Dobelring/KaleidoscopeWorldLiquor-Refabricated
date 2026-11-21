package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 官方 1.2.0 mixins/smc/LootTableAccessor：战利品表池数组只读访问（smc 饮品随机酿造等级挂载）。
 *  1.20.1 的 pools 字段是数组（官方 List 版本为 1.21+ 形态）。 */
@Mixin(LootTable.class)
public interface LootTableAccessor {
    @Accessor("pools")
    LootPool[] kwl$getPools();
}
