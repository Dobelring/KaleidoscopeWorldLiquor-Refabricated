package com.bmt.kaleidoscope_world_liquor.mixin.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.ChairSeatSupport;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 装置拆解回世界时，把原本坐在吧台凳上的乘客放回凳子（官方同名 mixin，只改 Create 包名）。
 * <p>
 * Create 自己只会把乘客丢到装置外，吧台凳的座面较高，故这里按凳子位置重建 tavern 的 SitEntity
 * 再把乘客放回去。
 */
@Mixin(Contraption.class)
public abstract class ContraptionDisassemblyMixin {
    @Inject(method = "addPassengersToWorld", at = @At("HEAD"))
    private void kaleidoscopeWorldLiquor$restoreChairPassengers(Level level, StructureTransform transform, List<Entity> seatedEntities, CallbackInfo ci) {
        if (level.isClientSide()) {
            return;
        }
        Contraption contraption = (Contraption) (Object) this;
        List<RestoreRequest> requests = new ArrayList<>();
        for (Entity passenger : new ArrayList<>(seatedEntities)) {
            Integer seatIndex = contraption.getSeatMapping().get(passenger.getUUID());
            if (seatIndex == null || seatIndex < 0 || seatIndex >= contraption.getSeats().size()) {
                continue;
            }
            BlockPos localPos = contraption.getSeats().get(seatIndex);
            StructureBlockInfo info = contraption.getBlocks().get(localPos);
            if (info == null) {
                continue;
            }
            BlockState state = transform.apply(info.state());
            if (ChairSeatSupport.isChairSeat(state)) {
                requests.add(new RestoreRequest(passenger, transform.apply(localPos), state));
            }
        }
        for (RestoreRequest request : requests) {
            ChairSeatSupport.restorePassenger(level, request.worldPos(), request.state(), request.passenger());
        }
    }

    private record RestoreRequest(Entity passenger, BlockPos worldPos, BlockState state) {
    }
}
