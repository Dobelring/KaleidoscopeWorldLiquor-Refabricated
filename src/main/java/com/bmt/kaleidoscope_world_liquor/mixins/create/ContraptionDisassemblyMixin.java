package com.bmt.kaleidoscope_world_liquor.mixins.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.ChairSeatSupport;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {Contraption.class},
    remap = false
)
public abstract class ContraptionDisassemblyMixin {
    public ContraptionDisassemblyMixin() {
    }

    @Inject(
        method = {"addPassengersToWorld"},
        at = {@At("HEAD")},
        remap = false
    )
    private void kaleidoscopeWorldLiquor$restoreChairPassengers(Level level, StructureTransform transform, List<Entity> seatedEntities, CallbackInfo ci) {
        if (!level.isClientSide) {
            Contraption contraption = (Contraption)(Object) this;
            List<ContraptionDisassemblyMixin.RestoreRequest> requests = new ArrayList<>();

            for (Entity passenger : new ArrayList<>(seatedEntities)) {
                Integer seatIndex = (Integer)contraption.getSeatMapping().get(passenger.getUUID());
                if (seatIndex != null && seatIndex >= 0 && seatIndex < contraption.getSeats().size()) {
                    BlockPos localPos = (BlockPos)contraption.getSeats().get(seatIndex);
                    StructureBlockInfo info = (StructureBlockInfo)contraption.getBlocks().get(localPos);
                    if (info != null) {
                        BlockState state = transform.apply(info.state());
                        if (ChairSeatSupport.isChairSeat(state)) {
                            requests.add(new ContraptionDisassemblyMixin.RestoreRequest(passenger, transform.apply(localPos), state));
                        }
                    }
                }
            }

            for (ContraptionDisassemblyMixin.RestoreRequest request : requests) {
                ChairSeatSupport.restorePassenger(level, request.worldPos(), request.state(), request.passenger());
            }
        }
    }

    private record RestoreRequest(Entity passenger, BlockPos worldPos, BlockState state) {
    }
}
