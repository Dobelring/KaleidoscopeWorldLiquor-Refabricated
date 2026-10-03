package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.network.ContraptionInteractionUtil;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

abstract class BlockEntityDelegatingMovingInteraction extends MovingInteractionBehaviour {
    BlockEntityDelegatingMovingInteraction() {
    }

    protected <T extends BlockEntity> T loadBlockEntity(T blockEntity, CompoundTag tag, AbstractContraptionEntity contraptionEntity) {
        blockEntity.setLevel(contraptionEntity.level());
        blockEntity.load(tag == null ? new CompoundTag() : tag.copy());
        return blockEntity;
    }

    protected void saveBlockEntity(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureBlockInfo info, BlockEntity blockEntity) {
        CompoundTag tag = blockEntity.saveWithFullMetadata();
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        ContraptionInteractionUtil.updateContraptionData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), tag));
    }
}
