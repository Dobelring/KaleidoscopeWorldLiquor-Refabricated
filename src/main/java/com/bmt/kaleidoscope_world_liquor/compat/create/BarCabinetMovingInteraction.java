package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.Optional;

/**
 * 装置上酒柜的左右两格取放（官方同名类的移植）。
 * <p>
 * 与官方的差别只有两处：可放置/异形标签在官方挂在 {@code BarCabinetBlock} 上、
 * 本端口在 {@code ModTags} 上；方块实体包名是 {@code blockentity}（本端口没有 {@code block.entity}）。
 */
public class BarCabinetMovingInteraction extends BlockEntityDelegatingMovingInteraction {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        }
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof BarCabinetBlock)) {
            return false;
        }
        Optional<ContraptionInteractionSupport.Hit> hit = ContraptionInteractionSupport.findHit(player, localPos, contraptionEntity);
        if (hit.isEmpty()) {
            return false;
        }

        BarCabinetBlockEntity cabinet = loadBlockEntity(new BarCabinetBlockEntity(localPos, info.state()), info.nbt(), contraptionEntity);
        ItemStack held = player.getItemInHand(activeHand);
        ItemStack leftItem = cabinet.getLeftItem();
        ItemStack rightItem = cabinet.getRightItem();
        boolean isLeftSide = ContraptionInteractionSupport.isLeftSide(
                info.state().getValue(BlockStateProperties.HORIZONTAL_FACING), localPos, hit.get().point());

        boolean irregular = false;
        boolean single = cabinet.isSingle();
        boolean isNativeBottle = held.getItem() instanceof BottleBlockItem;
        boolean isNativeCocktail = held.getItem() instanceof CocktailBlockItem;
        boolean isNativeDrink = isNativeBottle || isNativeCocktail;
        boolean isTagNormal = held.is(ModTags.BAR_CABINET_PLACEABLE) && held.getItem() instanceof BlockItem;
        boolean isTagIrregular = held.is(ModTags.BAR_CABINET_IRREGULAR) && held.getItem() instanceof BlockItem;
        boolean isAnyPlaceable = isNativeDrink || isTagNormal || isTagIrregular;
        boolean isIrregularItem = isTagIrregular;

        if (isAnyPlaceable) {
            if (single) {
                return false;
            }
            if (isIrregularItem) {
                if (!leftItem.isEmpty() || !rightItem.isEmpty()) {
                    return false;
                }
                isLeftSide = true;
                irregular = true;
            } else if (!leftItem.isEmpty() && rightItem.isEmpty() && isLeftSide) {
                isLeftSide = false;
            } else if (leftItem.isEmpty() && !rightItem.isEmpty() && !isLeftSide) {
                isLeftSide = true;
            }
        } else {
            if (!held.isEmpty()) {
                return false;
            }
            if (single) {
                isLeftSide = true;
                irregular = true;
            } else if (leftItem.isEmpty() && !rightItem.isEmpty() && isLeftSide) {
                isLeftSide = false;
            } else if (!leftItem.isEmpty() && rightItem.isEmpty() && !isLeftSide) {
                isLeftSide = true;
            }
        }

        ItemStack selected = isLeftSide ? leftItem : rightItem;
        if (held.isEmpty() && selected.isEmpty()) {
            return false;
        }
        if (!held.isEmpty() && !selected.isEmpty()) {
            return false;
        }
        if (contraptionEntity.level().isClientSide()) {
            return true;
        }

        if (held.isEmpty()) {
            player.setItemInHand(activeHand, selected.copy());
            if (isLeftSide) {
                cabinet.setLeftItem(ItemStack.EMPTY);
            } else {
                cabinet.setRightItem(ItemStack.EMPTY);
            }
            cabinet.setSingle(false);
            saveBlockEntity(contraptionEntity, localPos, info, cabinet);
            ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.GLASS_PLACE);
            return true;
        }

        ItemStack placed = held.split(1);
        if (isLeftSide) {
            cabinet.setLeftItem(placed);
        } else {
            cabinet.setRightItem(placed);
        }
        cabinet.setSingle(irregular);
        saveBlockEntity(contraptionEntity, localPos, info, cabinet);
        ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.GLASS_PLACE);
        return true;
    }
}
