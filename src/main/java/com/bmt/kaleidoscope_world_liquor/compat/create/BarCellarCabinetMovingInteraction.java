package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.BarCellarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.forge.ItemStackHandler;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class BarCellarCabinetMovingInteraction extends BlockEntityDelegatingMovingInteraction {
    public BarCellarCabinetMovingInteraction() {
    }

    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        } else {
            StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
            if (info != null && info.state().getBlock() instanceof BarCellarCabinetBlock) {
                Optional<ContraptionInteractionSupport.Hit> hit = ContraptionInteractionSupport.findHit(player, localPos, contraptionEntity);
                if (hit.isEmpty()) {
                    return false;
                } else {
                    int slot = ContraptionInteractionSupport.getCellarCabinetSlot(info.state(), localPos, hit.get());
                    if (slot < 0) {
                        return false;
                    } else {
                        BarCellarCabinetBlockEntity cellarCabinet = this.loadBlockEntity(
                            new BarCellarCabinetBlockEntity(localPos, info.state()), info.nbt(), contraptionEntity
                        );
                        ItemStack held = player.getItemInHand(activeHand);
                        ItemStackHandler items = cellarCabinet.getItems();
                        ItemStack stored = items.getStackInSlot(slot);
                        boolean isNativeBottle = held.getItem() instanceof BottleBlockItem;
                        boolean isBlocked;
                        if (isNativeBottle) {
                            isBlocked = held.is(BarCellarCabinetBlock.BAR_CELLAR_CABINET_NATIVE_BLACKLIST);
                        } else {
                            isBlocked = !held.is(BarCellarCabinetBlock.BAR_CELLAR_CABINET_PLACEABLE);
                        }

                        if (held.isEmpty()) {
                            if (stored.isEmpty()) {
                                return false;
                            } else if (contraptionEntity.level().isClientSide) {
                                return true;
                            } else {
                                ItemStack extracted = items.extractItem(slot, 1, false);
                                player.setItemInHand(activeHand, extracted);
                                this.saveBlockEntity(contraptionEntity, localPos, info, cellarCabinet);
                                ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM);
                                return true;
                            }
                        } else if (isBlocked || !stored.isEmpty()) {
                            return false;
                        } else if (contraptionEntity.level().isClientSide) {
                            return true;
                        } else {
                            items.setStackInSlot(slot, held.split(1));
                            this.saveBlockEntity(contraptionEntity, localPos, info, cellarCabinet);
                            ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.STONE_PLACE);
                            return true;
                        }
                    }
                }
            } else {
                return false;
            }
        }
    }
}
