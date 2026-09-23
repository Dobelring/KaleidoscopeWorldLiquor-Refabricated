package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/** 把装置上某个方块的状态与方块实体数据在"装置数据结构"里替换掉（官方同名类的移植）。 */
abstract class BlockEntityDelegatingMovingInteraction extends MovingInteractionBehaviour {
    protected <T extends BlockEntity> T loadBlockEntity(T blockEntity, CompoundTag tag, AbstractContraptionEntity contraptionEntity) {
        return ContraptionDataSync.loadBlockEntity(blockEntity, tag, contraptionEntity);
    }

    protected void saveBlockEntity(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureBlockInfo info, BlockEntity blockEntity) {
        ContraptionDataSync.saveBlockEntity(contraptionEntity, localPos, info, blockEntity);
    }
}
