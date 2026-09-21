package com.bmt.kaleidoscope_world_liquor.mixin.accessor;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** row/column 是 private final，Fabric 的 fabric-creative-tab-api-v1 也是这么改的（见其 CreativeModeTabAccessor）。 */
@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {
    @Accessor("row")
    @Mutable
    @Final
    void kwl$setRow(CreativeModeTab.Row row);

    @Accessor("column")
    @Mutable
    @Final
    void kwl$setColumn(int column);
}
