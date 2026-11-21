package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
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
 * 官方 1.2.0：装置上冰箱的交互 + 虚拟 BE 实时合成。
 * 升级说明：1.1.x 版按 NBT 每次交互加载/回写（合成不推进）；现按官方接
 * ContraptionVirtualBlockEntities——交互命中装置数据里的虚拟 BE，合成由
 * 服务端 tick（ContraptionVirtualBlockEntities.tickAll）持续推进，进度定期回写 NBT。
 */
public class FreezerMovingInteraction extends MovingInteractionBehaviour {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        StructureBlockInfo info = ContraptionBlocks.getInfo(contraptionEntity, localPos);
        if (info == null || !(info.state().getBlock() instanceof FreezerBlock)) {
            return false;
        }
        if (contraptionEntity.level().isClientSide()) {
            return true;
        }
        BlockState state = info.state();
        if (ContraptionVirtualBlockEntities.getOrCreateBlockEntity(contraptionEntity, localPos, info) instanceof FreezerBlockEntity freezer) {
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
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BARREL_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    state = state.setValue(FreezerBlock.OPEN, true);
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BARREL_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                ContraptionVirtualBlockEntities.setBlock(contraptionEntity, localPos, state);
                return true;
            }
            if (!state.getValue(FreezerBlock.OPEN)) {
                return true;
            }
            ItemStack held = player.getItemInHand(hand);
            if (freezer.hasOutput()) {
                if (freezer.extractOutput(player, hand)) {
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return true;
            }
            if (freezer.extractItem(player, held)) {
                ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.1F);
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
                ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
            if (held.getItem() instanceof BucketItem bucket) {
                Fluid content = bucket.getContent();
                if (content != Fluids.EMPTY && tankEmpty) {
                    freezer.tank.fill(FluidVariant.of(content), FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE);
                    giveBucketBack(player, held);
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
                if (content == Fluids.EMPTY && !freezer.tank.isResourceBlank() && freezer.tank.getFluidAmountMb() >= 1000) {
                    freezer.tank.drain(FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE);
                    if (!player.isCreative()) {
                        held.shrink(1);
                    }
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_FILL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }
            if (freezer.insertItem(held, player)) {
                ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_ADD_ITEM, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.1F);
            }
            return true;
        }
        return false;
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
