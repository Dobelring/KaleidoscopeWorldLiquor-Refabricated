package com.bmt.kaleidoscope_world_liquor.mixin.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.ChairSeatSupport;
import com.bmt.kaleidoscope_world_liquor.mixin.create.accessor.ContraptionAccessor;
import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import com.zurrtum.create.content.contraptions.Contraption;
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

/**
 * 装置组装时把坐在吧台凳上的乘客记进装置的 initialPassengers，
 * 这样装置开始移动时人还留在凳子上（官方同名 mixin，只改 Create 包名）。
 */
@Mixin(Contraption.class)
public abstract class ContraptionAssemblyMixin {
    @Inject(method = "addBlock", at = @At("TAIL"))
    private void kaleidoscopeWorldLiquor$captureChairPassenger(Level level, BlockPos worldPos, Pair<StructureBlockInfo, BlockEntity> captured, CallbackInfo ci) {
        if (!ChairSeatSupport.isChairSeat(captured.getLeft().state())) {
            return;
        }
        Entity passenger = null;
        for (SitEntity sitEntity : level.getEntitiesOfClass(SitEntity.class, new AABB(worldPos))) {
            passenger = sitEntity.getFirstPassenger();
            if (passenger != null) {
                break;
            }
        }
        if (passenger != null) {
            Contraption contraption = (Contraption) (Object) this;
            BlockPos localPos = worldPos.subtract(contraption.anchor);
            ((ContraptionAccessor) contraption).kaleidoscope_world_liquor$getInitialPassengers().put(localPos, passenger);
        }
    }
}
