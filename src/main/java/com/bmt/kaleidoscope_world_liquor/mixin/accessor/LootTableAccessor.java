package com.bmt.kaleidoscope_world_liquor.mixin.accessor;

import java.util.List;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 官方 1.2.0 mixins/smc/LootTableAccessor：战利品表池列表只读访问（smc 饮品随机酿造等级挂载）。 */
@Mixin(LootTable.class)
public interface LootTableAccessor {
    @Accessor("pools")
    List<LootPool> kwl$getPools();
}
