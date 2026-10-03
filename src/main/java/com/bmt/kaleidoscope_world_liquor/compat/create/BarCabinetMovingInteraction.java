package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class BarCabinetMovingInteraction extends BlockEntityDelegatingMovingInteraction {
    public BarCabinetMovingInteraction() {
    }

    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        if (activeHand != InteractionHand.MAIN_HAND) {
            return false;
        } else {
            StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
            if (info != null && info.state().getBlock() instanceof BarCabinetBlock) {
                Optional<ContraptionInteractionSupport.Hit> hit = ContraptionInteractionSupport.findHit(player, localPos, contraptionEntity);
                if (hit.isEmpty()) {
                    return false;
                } else {
                    BarCabinetBlockEntity cabinet = this.loadBlockEntity(new BarCabinetBlockEntity(localPos, info.state()), info.nbt(), contraptionEntity);
                    ItemStack held = player.getItemInHand(activeHand);
                    ItemStack leftItem = cabinet.getLeftItem();
                    ItemStack rightItem = cabinet.getRightItem();
                    boolean isLeftSide = ContraptionInteractionSupport.isLeftSide(
                        info.state().getValue(BlockStateProperties.HORIZONTAL_FACING), localPos, hit.get().point()
                    );
                    boolean irregular = false;
                    boolean single = cabinet.isSingle();
                    boolean isNativeBottle = held.getItem() instanceof BottleBlockItem;
                    boolean isNativeCocktail = held.getItem() instanceof CocktailBlockItem;
                    boolean isNativeDrink = isNativeBottle || isNativeCocktail;
                    boolean isTagNormal = held.is(BarCabinetBlock.BAR_CABINET_PLACEABLE) && held.getItem() instanceof BlockItem;
                    boolean isTagIrregular = held.is(BarCabinetBlock.BAR_CABINET_IRREGULAR) && held.getItem() instanceof BlockItem;
                    boolean isAnyPlaceable = isNativeDrink || isTagNormal || isTagIrregular;
                    if (isAnyPlaceable) {
                        if (single) {
                            return false;
                        }

                        if (isTagIrregular) {
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
                    } else if (!held.isEmpty() && !selected.isEmpty()) {
                        return false;
                    } else if (contraptionEntity.level().isClientSide) {
                        return true;
                    } else if (held.isEmpty()) {
                        player.setItemInHand(activeHand, selected.copy());
                        if (isLeftSide) {
                            cabinet.setLeftItem(ItemStack.EMPTY);
                        } else {
                            cabinet.setRightItem(ItemStack.EMPTY);
                        }

                        cabinet.setSingle(false);
                        this.saveBlockEntity(contraptionEntity, localPos, info, cabinet);
                        ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.GLASS_PLACE);
                        return true;
                    } else {
                        ItemStack placed = held.split(1);
                        if (isLeftSide) {
                            cabinet.setLeftItem(placed);
                        } else {
                            cabinet.setRightItem(placed);
                        }

                        cabinet.setSingle(irregular);
                        this.saveBlockEntity(contraptionEntity, localPos, info, cabinet);
                        ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.GLASS_PLACE);
                        return true;
                    }
                }
            } else {
                return false;
            }
        }
    }
}
