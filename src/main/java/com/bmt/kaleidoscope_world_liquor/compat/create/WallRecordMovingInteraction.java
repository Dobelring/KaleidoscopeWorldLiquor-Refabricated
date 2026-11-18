package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.WallRecordBlock;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/** 官方 1.2.0：装置上挂墙唱片——空手取下唱片并把方块从装置上移除。 */
public class WallRecordMovingInteraction extends BlockEntityDelegatingMovingInteraction {
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
            ItemStack record = ItemStack.CODEC.parse(
                    contraptionEntity.level().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                    nbt.getCompound("Record").orElse(new CompoundTag())).result().orElse(ItemStack.EMPTY);
            if (!record.isEmpty()) {
                ItemStack give = record.copyWithCount(1);
                if (!player.getInventory().add(give)) {
                    player.drop(give, false);
                }
            }
        }
        ContraptionDataSync.removeBlockFromContraption(contraptionEntity, localPos);
        ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM);
        return true;
    }
}
