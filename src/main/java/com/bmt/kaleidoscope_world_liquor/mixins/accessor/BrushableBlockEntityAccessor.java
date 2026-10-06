package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({BrushableBlockEntity.class})
public interface BrushableBlockEntityAccessor {
    @Accessor("lootTable")
    @Nullable
    ResourceLocation kwl$getLootTable();
}
