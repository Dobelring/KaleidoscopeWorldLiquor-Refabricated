package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;

/**
 * 吧台凳在装置上的搬运行为：把自身登记成 Create 的座位（官方同名类，只改 Create 包名）。
 */
public class ChairBlockMovementBehaviour extends SeatMovementBehaviour implements BlockRemovalAwareMovementBehaviour {
    @Override
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
