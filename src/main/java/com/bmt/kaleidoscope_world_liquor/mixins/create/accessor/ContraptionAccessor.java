package com.bmt.kaleidoscope_world_liquor.mixins.create.accessor;

import com.simibubi.create.content.contraptions.Contraption;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {Contraption.class},
    remap = false
)
public interface ContraptionAccessor {
    @Accessor(
        value = "updateTags",
        remap = false
    )
    Map<BlockPos, CompoundTag> getUpdateTags();

    @Accessor(
        value = "anchor",
        remap = false
    )
    BlockPos getAnchor();

    @Accessor(
        value = "initialPassengers",
        remap = false
    )
    Map<BlockPos, Entity> getInitialPassengers();
}
