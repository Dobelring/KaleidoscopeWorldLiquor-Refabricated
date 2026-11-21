package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

/**
 * 官方 1.2.0：装置上冰箱的交互 + 虚拟 BE 实时合成。
 * 升级说明：1.1.11.4 版按 NBT 每次交互加载/回写（合成不推进）；现按官方接
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
                player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.remaining_time", remaining), true);
                return true;
            }
            if (player.isShiftKeyDown()) {
                if (state.getValue(FreezerBlock.OPEN)) {
                    boolean success = freezer.tryStartCrafting();
                    state = success ? state.setValue(FreezerBlock.OPEN, false).setValue(FreezerBlock.WORKING, true)
                            : state.setValue(FreezerBlock.OPEN, false);
                    if (success) {
                        player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.start_crafting"), true);
                    }
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    state = state.setValue(FreezerBlock.OPEN, true);
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                ContraptionVirtualBlockEntities.setBlock(contraptionEntity, localPos, state);
                return true;
            }
            if (!state.getValue(FreezerBlock.OPEN)) {
                return true;
            }
            ItemStack held = player.getItemInHand(hand);
            if (freezer.hasOutput()) {
                // 官方 1.2.0：按交互手取产物
                if (freezer.extractOutput(player, hand)) {
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return true;
            }
            if (freezer.extractItem(player, held)) {
                ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.1F);
                return true;
            }
            if (held.isEmpty()) {
                return true;
            }
            if (held.is(Items.MILK_BUCKET)) {
                long acceptedMb = freezer.tank.getCapacityTransfer() - freezer.tank.getFluidAmountTransfer();
                if (acceptedMb >= FluidConstants.BUCKET) {
                    freezer.tank.fill(FluidVariant.of(ModFluids.MILK_STILL), FluidConstants.BUCKET,
                            com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank.FluidAction.EXECUTE);
                    giveBucketBack(player, held);
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }
            // 通用桶装流体注入（原版桶循环遍历，与方块本体 use 同语义）
            if (held.getItem() instanceof BucketItem bucket && !(bucket instanceof net.minecraft.world.item.MobBucketItem)) {
                Fluid content = null;
                for (Fluid candidate : BuiltInRegistries.FLUID) {
                    if (candidate != Fluids.EMPTY && candidate.getBucket() == held.getItem()) {
                        content = candidate;
                        break;
                    }
                }
                if (content != null && content != Fluids.EMPTY && freezer.tank.isResourceBlank()) {
                    freezer.tank.fill(FluidVariant.of(content), FluidConstants.BUCKET,
                            com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank.FluidAction.EXECUTE);
                    giveBucketBack(player, held);
                    ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }
            if (freezer.insertItem(held, player)) {
                ContraptionBlocks.playSound(contraptionEntity, localPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.1F);
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
