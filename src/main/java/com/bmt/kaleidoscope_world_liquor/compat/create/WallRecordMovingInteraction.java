package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.WallRecordBlock;
import com.bmt.kaleidoscope_world_liquor.compat.create.network.ContraptionInteractionUtil;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/** 官方 1.2.0：装置上挂墙唱片——空手取下唱片并把方块从装置上移除。 */
public class WallRecordMovingInteraction extends MovingInteractionBehaviour {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof WallRecordBlock)) {
            return false;
        }
        if (contraptionEntity.level().isClientSide()) {
            return true;
        }
        if (!player.getItemInHand(hand).isEmpty()) {
            return false;
        }
        CompoundTag nbt = info.nbt();
        if (nbt != null && nbt.contains("Record")) {
            ItemStack record = ItemStack.of(nbt.getCompound("Record"));
            if (!record.isEmpty()) {
                ItemStack give = record.copy();
                if (!player.getInventory().add(give)) {
                    player.drop(give, false);
                }
            }
        }
        ContraptionInteractionUtil.removeBlockFromContraption(contraptionEntity, localPos);
        ContraptionInteractionUtil.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}
