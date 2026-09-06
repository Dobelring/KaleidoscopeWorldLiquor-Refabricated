package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.ITapBehavior;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModParticles;
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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({TapBlock.class})
public abstract class TapBlockMixin {
   @Redirect(
      method = {"tryOpen"},
      remap = false,
      at = @At(
         value = "INVOKE",
         target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;isMatch(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
         remap = false
      )
   )
   private boolean kwl$redirectIsMatchTryOpen(
      ITapBehavior behavior, Level level, @Nullable Player player, BlockPos tapPos, BlockState tapState, BlockState sourceState, BlockState destinationState
   ) {
      boolean original = behavior.isMatch(level, player, tapPos, tapState, sourceState, destinationState);
      return original ? true : kwl$isFreezerMatch(level, tapPos, sourceState, destinationState);
   }

   @Redirect(
      method = {"tryOpen"},
      remap = false,
      at = @At(
         value = "INVOKE",
         target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;onStartExtract(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/particles/ParticleOptions;",
         remap = false
      )
   )
   private ParticleOptions kwl$redirectOnStartExtract(
      ITapBehavior behavior, Level level, @Nullable Player player, BlockPos tapPos, BlockState tapState, BlockState sourceState, BlockState destinationState
   ) {
      return kwl$isFreezerMatch(level, tapPos, sourceState, destinationState)
         ? kwl$getParticle(sourceState)
         : behavior.onStartExtract(level, player, tapPos, tapState, sourceState, destinationState);
   }

   @Redirect(
      method = {"tick"},
      remap = false,
      at = @At(
         value = "INVOKE",
         target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;isMatch(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
         remap = false
      )
   )
   private boolean kwl$redirectIsMatchTick(
      ITapBehavior behavior, Level level, @Nullable Player player, BlockPos tapPos, BlockState tapState, BlockState sourceState, BlockState destinationState
   ) {
      boolean original = behavior.isMatch(level, player, tapPos, tapState, sourceState, destinationState);
      return original ? true : kwl$isFreezerMatch(level, tapPos, sourceState, destinationState);
   }

   @Redirect(
      method = {"tick"},
      remap = false,
      at = @At(
         value = "INVOKE",
         target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/api/blockentity/ITapBehavior;onEndExtract(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
         remap = false
      )
   )
   private void kwl$redirectOnEndExtract(
      ITapBehavior behavior, Level level, BlockPos tapPos, BlockState tapState, BlockState sourceState, BlockState destinationState
   ) {
      if (kwl$isFreezerMatch(level, tapPos, sourceState, destinationState)) {
         kwl$fillFreezer(level, tapPos, sourceState, destinationState);
      } else {
         behavior.onEndExtract(level, tapPos, tapState, sourceState, destinationState);
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
               if (be.tank.getFluidAmount() >= be.tank.getCapacity()) {
                  return false;
               } else {
                  if (!be.tank.isEmpty()) {
                     FluidStack injectFluid = kwl$isLavaSource(sourceState) ? new FluidStack(Fluids.LAVA, 1000) : new FluidStack(Fluids.WATER, 1000);
                     if (!be.tank.getFluid().getFluid().isSame(injectFluid.getFluid())) {
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
      return state.is(ModBlocks.FREEZER) && state.hasProperty(FreezerBlock.OPEN) && Boolean.TRUE.equals(state.getValue(FreezerBlock.OPEN));
   }

   private static ParticleOptions kwl$getParticle(BlockState sourceState) {
      return kwl$isLavaSource(sourceState) ? ModParticles.LAVA_TAP_DRIP : ModParticles.WATER_TAP_DRIP;
   }

   private static void kwl$fillFreezer(Level level, BlockPos tapPos, BlockState sourceState, BlockState destinationState) {
      BlockPos belowPos = tapPos.below();
      if (level.getBlockEntity(belowPos) instanceof FreezerBlockEntity be) {
         FluidStack fluid = kwl$isLavaSource(sourceState) ? new FluidStack(Fluids.LAVA, 1000) : new FluidStack(Fluids.WATER, 1000);
         int filled = be.tank.fill(fluid, false);
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
