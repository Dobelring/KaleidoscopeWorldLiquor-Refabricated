package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.ITapBehavior;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
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

/**
 * 龙头往冰柜注水/岩浆（官方 1.1.9 的 {@code TapBlockMixin}，@WrapOperation 版）。
 *
 * <p><b>为什么必须是 mixin</b>：tavern 的 {@code TapBehaviorManager} 是按<b>源头方块</b>查行为的
 * （{@code TapBlock#tryOpen/tick} 里是 {@code get(sourceState)}，源头 = 龙头朝向反方向的方块），
 * 而冰柜在龙头的<b>下方</b>（destination）。所以早前 1.21.11/26.x 移植版把行为注册到冰柜方块上
 * （{@code TapBehaviorManager.register(ModBlocks.FREEZER, …)}）是<b>永远不会被查到</b>的死代码，
 * 龙头注不进水柜。官方 1.20.1/1.21.1 一直是用本 mixin 包装 tavern 的
 * {@code isMatch/onStartExtract/onEndExtract} 调用来叠加冰柜分支。
 *
 * <p>源头判定照官方：水 = 水炼药锅或含水方块；岩浆 = 岩浆炼药锅。
 * （普通的水/岩浆流体方块不在此列——tavern 的 {@code contains(sourceState)} 对其为 false，
 * 根本不会走到这些调用点，官方亦然。）
 */
@Mixin(TapBlock.class)
public abstract class TapBlockMixin {
    @WrapOperation(
            method = "tryOpen",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;isMatch(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
                    remap = false
            )
    )
    private boolean kwl$isMatchTryOpen(ITapBehavior behavior, Level level, @Nullable Player player, BlockPos tapPos,
                                       BlockState tapState, BlockState sourceState, BlockState destinationState,
                                       Operation<Boolean> original) {
        return original.call(behavior, level, player, tapPos, tapState, sourceState, destinationState)
                || kwl$isFreezerMatch(level, tapPos, sourceState, destinationState);
    }

    @WrapOperation(
            method = "tryOpen",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;onStartExtract(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/ParticleOptions;",
                    remap = false
            )
    )
    private ParticleOptions kwl$onStartExtract(ITapBehavior behavior, Level level, @Nullable Player player, BlockPos tapPos,
                                              BlockState tapState, BlockState sourceState, BlockState destinationState,
                                              Operation<ParticleOptions> original) {
        return kwl$isFreezerMatch(level, tapPos, sourceState, destinationState)
                ? kwl$getParticle(sourceState)
                : original.call(behavior, level, player, tapPos, tapState, sourceState, destinationState);
    }

    @WrapOperation(
            method = "tick",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;isMatch(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
                    remap = false
            )
    )
    private boolean kwl$isMatchTick(ITapBehavior behavior, Level level, @Nullable Player player, BlockPos tapPos,
                                    BlockState tapState, BlockState sourceState, BlockState destinationState,
                                    Operation<Boolean> original) {
        return original.call(behavior, level, player, tapPos, tapState, sourceState, destinationState)
                || kwl$isFreezerMatch(level, tapPos, sourceState, destinationState);
    }

    @WrapOperation(
            method = "tick",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;onEndExtract(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
                    remap = false
            )
    )
    private void kwl$onEndExtract(ITapBehavior behavior, Level level, BlockPos tapPos, BlockState tapState,
                                  BlockState sourceState, BlockState destinationState, Operation<Void> original) {
        if (kwl$isFreezerMatch(level, tapPos, sourceState, destinationState)) {
            kwl$fillFreezer(level, tapPos, sourceState, destinationState);
        } else {
            original.call(behavior, level, tapPos, tapState, sourceState, destinationState);
        }
    }

    private static boolean kwl$isFreezerMatch(Level level, BlockPos tapPos, BlockState sourceState, BlockState destinationState) {
        if (!kwl$isOpenFreezer(destinationState)) {
            return false;
        }
        if (!kwl$isWaterSource(sourceState) && !kwl$isLavaSource(sourceState)) {
            return false;
        }

        BlockPos belowPos = tapPos.below();
        if (!(level.getBlockEntity(belowPos) instanceof FreezerBlockEntity be)) {
            return false;
        }
        if (be.isWorking() || be.hasOutput()) {
            return false;
        }
        if (be.tank.getFluidAmountMb() >= CustomFluidTank.MB_PER_BUCKET) {
            return false;
        }
        return be.tank.isResourceBlank() || be.tank.getFluidVariant().getFluid().isSame(kwl$injectFluid(sourceState));
    }

    private static boolean kwl$isWaterSource(BlockState sourceState) {
        return sourceState.is(Blocks.WATER_CAULDRON)
                || (sourceState.hasProperty(BlockStateProperties.WATERLOGGED)
                && Boolean.TRUE.equals(sourceState.getValue(BlockStateProperties.WATERLOGGED)));
    }

    private static boolean kwl$isLavaSource(BlockState sourceState) {
        return sourceState.is(Blocks.LAVA_CAULDRON);
    }

    private static boolean kwl$isOpenFreezer(BlockState state) {
        return state.is(ModBlocks.FREEZER)
                && state.hasProperty(FreezerBlock.OPEN)
                && Boolean.TRUE.equals(state.getValue(FreezerBlock.OPEN));
    }

    private static net.minecraft.world.level.material.Fluid kwl$injectFluid(BlockState sourceState) {
        return kwl$isLavaSource(sourceState) ? Fluids.LAVA : Fluids.WATER;
    }

    private static ParticleOptions kwl$getParticle(BlockState sourceState) {
        return kwl$isLavaSource(sourceState) ? ModParticles.LAVA_TAP_DRIP : ModParticles.WATER_TAP_DRIP;
    }

    private static void kwl$fillFreezer(Level level, BlockPos tapPos, BlockState sourceState, BlockState destinationState) {
        BlockPos belowPos = tapPos.below();
        if (!(level.getBlockEntity(belowPos) instanceof FreezerBlockEntity be)) {
            return;
        }

        long filled = be.tank.fill(FluidVariant.of(kwl$injectFluid(sourceState)), FluidConstants.BUCKET,
                CustomFluidTank.FluidAction.EXECUTE);
        if (filled <= 0) {
            return;
        }

        level.playSound(null, belowPos, kwl$isLavaSource(sourceState) ? SoundEvents.LAVA_POP : SoundEvents.AXOLOTL_SPLASH,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        ITapBehavior.sendParticles(level, tapPos);
        be.setChanged();
        level.sendBlockUpdated(belowPos, destinationState, destinationState, 3);
    }
}
