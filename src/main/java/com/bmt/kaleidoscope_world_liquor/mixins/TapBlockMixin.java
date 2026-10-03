package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.ITapBehavior;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModParticles;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({TapBlock.class})
public abstract class TapBlockMixin {
    public TapBlockMixin() {
    }

    @WrapOperation(
        method = {"tryOpen"},
        remap = false,
        at = {@At(
            value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;isMatch(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            remap = false
        )}
    )
    private boolean kwl$redirectIsMatchTryOpen(
        ITapBehavior behavior,
        Level level,
        @Nullable Player player,
        BlockPos tapPos,
        BlockState tapState,
        BlockState sourceState,
        BlockState destinationState,
        Operation<Boolean> original
    ) {
        boolean originalResult = (Boolean)original.call(new Object[]{behavior, level, player, tapPos, tapState, sourceState, destinationState});
        return originalResult ? true : kwl$isFreezerMatch(level, tapPos, sourceState, destinationState);
    }

    @WrapOperation(
        method = {"tryOpen"},
        remap = false,
        at = {@At(
            value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;onStartExtract(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/ParticleOptions;",
            remap = false
        )}
    )
    private ParticleOptions kwl$redirectOnStartExtract(
        ITapBehavior behavior,
        Level level,
        @Nullable Player player,
        BlockPos tapPos,
        BlockState tapState,
        BlockState sourceState,
        BlockState destinationState,
        Operation<ParticleOptions> original
    ) {
        return kwl$isFreezerMatch(level, tapPos, sourceState, destinationState)
            ? kwl$getParticle(sourceState)
            : (ParticleOptions)original.call(new Object[]{behavior, level, player, tapPos, tapState, sourceState, destinationState});
    }

    @WrapOperation(
        method = {"tick"},
        at = {@At(
            value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;isMatch(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            remap = false
        )}
    )
    private boolean kwl$redirectIsMatchTick(
        ITapBehavior behavior,
        Level level,
        @Nullable Player player,
        BlockPos tapPos,
        BlockState tapState,
        BlockState sourceState,
        BlockState destinationState,
        Operation<Boolean> original
    ) {
        boolean originalResult = (Boolean)original.call(new Object[]{behavior, level, player, tapPos, tapState, sourceState, destinationState});
        return originalResult ? true : kwl$isFreezerMatch(level, tapPos, sourceState, destinationState);
    }

    @WrapOperation(
        method = {"tick"},
        at = {@At(
            value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;onEndExtract(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
            remap = false
        )}
    )
    private void kwl$redirectOnEndExtract(
        ITapBehavior behavior, Level level, BlockPos tapPos, BlockState tapState, BlockState sourceState, BlockState destinationState, Operation<Void> original
    ) {
        if (kwl$isFreezerMatch(level, tapPos, sourceState, destinationState)) {
            kwl$fillFreezer(level, tapPos, sourceState, destinationState);
        } else {
            original.call(new Object[]{behavior, level, tapPos, tapState, sourceState, destinationState});
        }
    }

    private static boolean kwl$isFreezerMatch(Level level, BlockPos tapPos, BlockState sourceState, BlockState destinationState) {
        if (!kwl$isOpenFreezer(destinationState)) {
            return false;
        } else if (!kwl$isWaterSource(sourceState) && !kwl$isLavaSource(sourceState)) {
            return false;
        } else {
            BlockPos belowPos = tapPos.below();
            if (level.getBlockEntity(belowPos) instanceof FreezerBlockEntity be) {
                if (!be.isWorking() && !be.hasOutput()) {
                    if (be.tank.getAmount() >= be.tank.getCapacity()) {
                        return false;
                    } else {
                        if (!be.tank.isResourceBlank()) {
                            FluidVariant injectFluid = FluidVariant.of(kwl$isLavaSource(sourceState) ? Fluids.LAVA : Fluids.WATER);
                            if (!be.tank.getResource().equals(injectFluid)) {
                                return false;
                            }
                        }

                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private static boolean kwl$isWaterSource(BlockState sourceState) {
        return sourceState.is(Blocks.WATER_CAULDRON)
            ? true
            : sourceState.hasProperty(BlockStateProperties.WATERLOGGED) && Boolean.TRUE.equals(sourceState.getValue(BlockStateProperties.WATERLOGGED));
    }

    private static boolean kwl$isLavaSource(BlockState sourceState) {
        return sourceState.is(Blocks.LAVA_CAULDRON);
    }

    private static boolean kwl$isOpenFreezer(BlockState state) {
        return state.is(ModBlocks.FREEZER)
            && state.hasProperty(FreezerBlock.OPEN)
            && Boolean.TRUE.equals(state.getValue(FreezerBlock.OPEN));
    }

    private static ParticleOptions kwl$getParticle(BlockState sourceState) {
        return kwl$isLavaSource(sourceState) ? (ParticleOptions)ModParticles.LAVA_TAP_DRIP : (ParticleOptions)ModParticles.WATER_TAP_DRIP;
    }

    private static void kwl$fillFreezer(Level level, BlockPos tapPos, BlockState sourceState, BlockState destinationState) {
        BlockPos belowPos = tapPos.below();
        if (level.getBlockEntity(belowPos) instanceof FreezerBlockEntity be) {
            FluidVariant fluid = FluidVariant.of(kwl$isLavaSource(sourceState) ? Fluids.LAVA : Fluids.WATER);
            long filled = be.tank.fill(fluid, FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE);
            if (filled > 0) {
                if (kwl$isLavaSource(sourceState)) {
                    level.playSound(null, belowPos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    level.playSound(null, belowPos, SoundEvents.AXOLOTL_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                ITapBehavior.sendParticles(level, tapPos);
                be.setChanged();
                level.sendBlockUpdated(belowPos, destinationState, destinationState, 3);
            }
        }
    }
}
