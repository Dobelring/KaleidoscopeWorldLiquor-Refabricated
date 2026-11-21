package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import java.util.function.BiFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mutable;

/** 官方 1.2.0 mixins/smc/LootPoolSingletonContainerAccessor：单条目 functions/compositeFunction 写入（挂随机酿造等级）。 */
@Mixin(LootPoolSingletonContainer.class)
public interface LootPoolSingletonContainerAccessor {
    @Accessor("functions")
    LootItemFunction[] kwl$getFunctions();

    @Mutable
    @Accessor("functions")
    void kwl$setFunctions(LootItemFunction[] functions);

    @Mutable
    @Accessor("compositeFunction")
    void kwl$setCompositeFunction(BiFunction<ItemStack, LootContext, ItemStack> compositeFunction);
}
