package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.ITapBehavior;
import com.github.ysbbbbbb.kaleidoscopetavern.game.tap.TapBehaviorManager;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 龙头接冰柜：tavern 1.21.11 的 TapBehaviorManager 注册表让此联动无需 mixin
 * （1.20.1 是 TapBlockMixin @Redirect）。龙头朝向后方的水源/岩浆源 → 注入下方冰柜。
 */
public final class FreezerTapBehavior implements ITapBehavior {

    public static void register() {
        TapBehaviorManager.register(ModBlocks.FREEZER, new FreezerTapBehavior());
    }

    private static boolean isWaterSource(BlockState sourceState) {
        return sourceState.is(Blocks.WATER)
                || (sourceState.hasProperty(BlockStateProperties.WATERLOGGED)
                && Boolean.TRUE.equals(sourceState.getValue(BlockStateProperties.WATERLOGGED)));
    }

    private static boolean isLavaSource(BlockState sourceState) {
        return sourceState.is(Blocks.LAVA);
    }

    private static boolean isOpenFreezer(BlockState state) {
        return state.is(ModBlocks.FREEZER) && state.hasProperty(FreezerBlock.OPEN)
                && Boolean.TRUE.equals(state.getValue(FreezerBlock.OPEN));
    }

    @Override
    public boolean isMatch(@NotNull Level level, @Nullable Player player, @NotNull BlockPos tapPos,
                           @NotNull BlockState tapState, @NotNull BlockState sourceState, @NotNull BlockState destinationState) {
        if (!isOpenFreezer(destinationState) || (!isWaterSource(sourceState) && !isLavaSource(sourceState))) {
            return false;
        }
        BlockPos belowPos = tapPos.below();
        if (level.getBlockEntity(belowPos) instanceof FreezerBlockEntity be
                && !be.isWorking() && !be.hasOutput()
                && be.tank.getFluidAmountMb() < 1000
                && (be.tank.isResourceBlank() || be.tank.getFluidVariant().getFluid() == (isLavaSource(sourceState) ? net.minecraft.world.level.material.Fluids.LAVA : net.minecraft.world.level.material.Fluids.WATER))) {
            return true;
        }
        return false;
    }

    @Override
    public @Nullable ParticleOptions onStartExtract(@NotNull Level level, @Nullable Player player, @NotNull BlockPos tapPos,
                                                    @NotNull BlockState tapState, @NotNull BlockState sourceState, @NotNull BlockState destinationState) {
        return isLavaSource(sourceState) ? ParticleTypes.DRIPPING_LAVA : ParticleTypes.DRIPPING_WATER;
    }

    @Override
    public void onEndExtract(@NotNull Level level, @NotNull BlockPos tapPos, @NotNull BlockState tapState,
                             @NotNull BlockState sourceState, @NotNull BlockState destinationState) {
        BlockPos belowPos = tapPos.below();
        if (level.getBlockEntity(belowPos) instanceof FreezerBlockEntity be) {
            boolean lava = isLavaSource(sourceState);
            be.tank.fill(FluidVariant.of(lava ? net.minecraft.world.level.material.Fluids.LAVA : net.minecraft.world.level.material.Fluids.WATER),
                    FluidConstants.BUCKET, com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank.FluidAction.EXECUTE);
            level.playSound(null, belowPos, lava ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            ITapBehavior.sendParticles(level, tapPos);
            be.setChanged();
            level.sendBlockUpdated(belowPos, destinationState, destinationState, 3);
        }
    }
}
