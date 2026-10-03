package com.bmt.kaleidoscope_world_liquor.mixins.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.ChairSeatSupport;
import com.bmt.kaleidoscope_world_liquor.mixins.create.accessor.ContraptionAccessor;
import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {Contraption.class},
    remap = false
)
public abstract class ContraptionAssemblyMixin {
    public ContraptionAssemblyMixin() {
    }

    @Inject(
        method = {"addBlock"},
        at = {@At("TAIL")},
        remap = false
    )
    private void kaleidoscopeWorldLiquor$captureChairPassenger(Level level, BlockPos worldPos, Pair<StructureBlockInfo, BlockEntity> captured, CallbackInfo ci) {
        if (ChairSeatSupport.isChairSeat(((StructureBlockInfo)captured.getLeft()).state())) {
            Entity passenger = null;

            for (SitEntity chairEntity : level.getEntitiesOfClass(SitEntity.class, new AABB(worldPos))) {
                passenger = chairEntity.getFirstPassenger();
                if (passenger != null) {
                    break;
                }
            }

            if (passenger != null) {
                Contraption contraption = (Contraption)(Object) this;
                ContraptionAccessor accessor = (ContraptionAccessor)contraption;
                BlockPos localPos = worldPos.subtract(accessor.getAnchor());
                accessor.getInitialPassengers().put(localPos, passenger);
            }
        }
    }
}
