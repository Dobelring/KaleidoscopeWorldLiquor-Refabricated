package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;

public interface BlockRemovalAwareMovementBehaviour {
    void onBlockRemoved(AbstractContraptionEntity var1, BlockPos var2);
}
