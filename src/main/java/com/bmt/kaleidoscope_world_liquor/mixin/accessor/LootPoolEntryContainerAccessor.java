package com.bmt.kaleidoscope_world_liquor.mixin.accessor;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mutable;

/** 26.3：单条目类删除，functions 改为基类 modifier（Optional<Holder<LootItemFunction>>）——读写以附加随机酿造等级。 */
@Mixin(LootPoolEntryContainer.class)
public interface LootPoolEntryContainerAccessor {
    @Accessor("modifier")
    Optional<Holder<LootItemFunction>> kwl();

    @Mutable
    @Accessor("modifier")
    void kwl(Optional<Holder<LootItemFunction>> modifier);
}
