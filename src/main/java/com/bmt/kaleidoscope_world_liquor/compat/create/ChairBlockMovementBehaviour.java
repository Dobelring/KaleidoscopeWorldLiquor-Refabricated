package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;

public class ChairBlockMovementBehaviour extends SeatMovementBehaviour implements BlockRemovalAwareMovementBehaviour {
    public ChairBlockMovementBehaviour() {
    }

    public void startMoving(MovementContext context) {
        if (!context.contraption.getSeats().contains(context.localPos)) {
            context.contraption.getSeats().add(context.localPos);
        }

        super.startMoving(context);
    }

    @Override
    public void onBlockRemoved(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        ChairSeatSupport.removeSeat(contraptionEntity, localPos);
    }
}
