package com.bmt.kaleidoscope_world_liquor.mixin.create.accessor;

import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Create 的 {@code Contraption} 两个私有/受保护成员没有 getter，装置上吧台凳与酒柜的逻辑需要它们
 * （官方同样靠 accessor mixin，此处只改 Create 包名）：
 * <ul>
 *   <li>{@code updateTags}：方块实体数据同步用（见 ContraptionDataSync）</li>
 *   <li>{@code initialPassengers}：装置组装时把凳子上的乘客带进装置用（见 ContraptionAssemblyMixin）</li>
 * </ul>
 */
@Mixin(Contraption.class)
public interface ContraptionAccessor {
    @Accessor("updateTags")
    Map<BlockPos, CompoundTag> kaleidoscope_world_liquor$getUpdateTags();

    @Accessor("initialPassengers")
    Map<BlockPos, Entity> kaleidoscope_world_liquor$getInitialPassengers();
}
