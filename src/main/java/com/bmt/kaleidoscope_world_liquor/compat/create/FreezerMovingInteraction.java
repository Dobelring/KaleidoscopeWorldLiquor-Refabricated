package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * 官方 1.2.0：装置上冰箱的交互（与方块本体同语义）。
 * 差异：官方 1.2.0 另有虚拟 BE 基建让冰箱在装置上实时合成；本端口沿用
 * 交互时按 NBT 加载/回写的既有机制，实时合成暂缺（26.x 移植差异）。
 */
public class FreezerMovingInteraction extends BlockEntityDelegatingMovingInteraction {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !(info.state().getBlock() instanceof FreezerBlock)) {
            return false;
        }
        if (contraptionEntity.level().isClientSide()) {
            return true;
        }
        BlockState state = info.state();
        FreezerBlockEntity freezer = loadBlockEntity(new FreezerBlockEntity(localPos, state), info.nbt(), contraptionEntity);
        if (state.getValue(FreezerBlock.WORKING)) {
            int remaining = Math.max(0, (freezer.getMaxProgress() - freezer.getProgress()) / 20);
            player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.remaining_time", remaining));
            return true;
        }
        if (player.isCrouching()) {
            if (state.getValue(FreezerBlock.OPEN)) {
                boolean success = freezer.tryStartCrafting();
                state = success ? state.setValue(FreezerBlock.OPEN, false).setValue(FreezerBlock.WORKING, true)
                        : state.setValue(FreezerBlock.OPEN, false);
                if (success) {
                    player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.start_crafting"));
                }
                ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.BARREL_CLOSE);
            } else {
                state = state.setValue(FreezerBlock.OPEN, true);
                ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.BARREL_OPEN);
            }
            ContraptionDataSync.updateContraptionDataLocally(contraptionEntity, localPos,
                    new StructureBlockInfo(localPos, state, info.nbt()));
            return true;
        }
        if (!state.getValue(FreezerBlock.OPEN)) {
            return true;
        }
        ItemStack held = player.getItemInHand(hand);
        if (freezer.hasOutput()) {
            if (freezer.extractOutput(player, hand)) {
                ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.ITEM_PICKUP);
            }
            saveBlockEntity(contraptionEntity, localPos, info, freezer);
            return true;
        }
        if (freezer.extractItem(player, held)) {
            ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM);
            saveBlockEntity(contraptionEntity, localPos, info, freezer);
            return true;
        }
        if (held.isEmpty()) {
            return true;
        }
        boolean tankEmpty = freezer.tank.isResourceBlank() || freezer.tank.getFluidAmountMb() == 0;
        if (held.is(Items.MILK_BUCKET) && tankEmpty) {
            freezer.tank.fill(FluidVariant.of(com.bmt.kaleidoscope_world_liquor.fluids.MilkFluid.STILL), FluidConstants.BUCKET,
                    CustomFluidTank.FluidAction.EXECUTE);
            giveBucketBack(player, held);
            ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_EMPTY);
            saveBlockEntity(contraptionEntity, localPos, info, freezer);
            return true;
        }
        if (held.getItem() instanceof BucketItem bucket) {
            Fluid content = bucket.getContent();
            if (content != Fluids.EMPTY && tankEmpty) {
                freezer.tank.fill(FluidVariant.of(content), FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE);
                giveBucketBack(player, held);
                ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_EMPTY);
                saveBlockEntity(contraptionEntity, localPos, info, freezer);
                return true;
            }
            if (content == Fluids.EMPTY && !freezer.tank.isResourceBlank() && freezer.tank.getFluidAmountMb() >= 1000) {
                freezer.tank.drain(FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_FILL);
                saveBlockEntity(contraptionEntity, localPos, info, freezer);
                return true;
            }
        }
        if (freezer.insertItem(held, player)) {
            ContraptionInteractionSupport.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_ADD_ITEM);
        }
        saveBlockEntity(contraptionEntity, localPos, info, freezer);
        return true;
    }

    private void giveBucketBack(Player player, ItemStack held) {
        if (player.isCreative()) {
            return;
        }
        held.shrink(1);
        ItemStack bucket = new ItemStack(Items.BUCKET);
        if (held.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
        } else if (!player.getInventory().add(bucket)) {
            player.drop(bucket, false);
        }
    }
}
