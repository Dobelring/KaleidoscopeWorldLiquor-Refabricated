package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.Optional;

/**
 * 装置上酒窖柜（9 格）的取放（官方同名类的移植）。
 * <p>
 * 与官方的差别：官方的槽位容器是 NeoForge 的 {@code ItemStackHandler}、取自
 * {@code getItems()}；本端口用的是 tavern 提供的同名兼容类（同样有 getStackInSlot /
 * extractItem / setStackInSlot），取自 {@code items()}。标签也从方块类挪到了 {@code ModTags}。
 */
public class BarCellarCabinetMovingInteraction extends BlockEntityDelegatingMovingInteraction {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof BarCellarCabinetBlock)) {
            return false;
        }
        Optional<ContraptionInteractionSupport.Hit> hit = ContraptionInteractionSupport.findHit(player, localPos, contraptionEntity);
        if (hit.isEmpty()) {
            return false;
        }
        int slot = ContraptionInteractionSupport.getCellarCabinetSlot(info.state(), localPos, hit.get());
        if (slot < 0) {
            return false;
        }

        BarCellarCabinetBlockEntity cellarCabinet = loadBlockEntity(
                new BarCellarCabinetBlockEntity(localPos, info.state()), info.nbt(), contraptionEntity);
        ItemStack held = player.getItemInHand(activeHand);
        ItemStackHandler items = cellarCabinet.items();
        ItemStack stored = items.getStackInSlot(slot);

        boolean isBlocked;
        if (held.getItem() instanceof BottleBlockItem) {
            isBlocked = held.is(ModTags.BAR_CELLAR_CABINET_NATIVE_BLACKLIST);
        } else {
            isBlocked = !held.is(ModTags.BAR_CELLAR_CABINET_PLACEABLE);
        }

        if (held.isEmpty()) {
            if (stored.isEmpty()) {
                return false;
            }
            if (contraptionEntity.level().isClientSide()) {
                return true;
            }
            ItemStack extracted = items.extractItem(slot, 1, false);
            player.setItemInHand(activeHand, extracted);
            saveBlockEntity(contraptionEntity, localPos, info, cellarCabinet);
            ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM);
            return true;
        }

        if (isBlocked || !stored.isEmpty()) {
            return false;
        }
        if (contraptionEntity.level().isClientSide()) {
            return true;
        }
        items.setStackInSlot(slot, held.split(1));
        saveBlockEntity(contraptionEntity, localPos, info, cellarCabinet);
        ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.STONE_PLACE);
        return true;
    }
}
