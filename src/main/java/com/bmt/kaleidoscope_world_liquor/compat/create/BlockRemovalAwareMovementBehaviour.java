package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;

/**
 * 装置上的方块被移除时回调（官方同名接口，只改 Create 包名）。
 * <p>
 * Create 的 {@code MovementBehaviour} 没有"方块从装置上消失"的钩子，官方用这个自定义接口
 * 让吧台凳在装置上被拆掉时能把自己从座位表里摘掉。
 */
public interface BlockRemovalAwareMovementBehaviour {
    void onBlockRemoved(AbstractContraptionEntity contraptionEntity, BlockPos localPos);
}
