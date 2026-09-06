package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RespawnEffect extends MobEffect {
   public RespawnEffect() {
      super(MobEffectCategory.NEUTRAL, 8900331);
   }

   public boolean isInstantenous() {
      return true;
   }

   public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health) {
      this.performEffect(livingEntity, amplifier);
   }

   public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
      this.performEffect(livingEntity, amplifier);
      return true;
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration == 1;
   }

   private void performEffect(LivingEntity entity, int amplifier) {
      Level level = entity.level();
      if (!level.isClientSide()) {
         if (entity instanceof ServerPlayer serverPlayer) {
            BlockPos respawnPos = serverPlayer.getRespawnPosition();
            ResourceKey respawnDimension = serverPlayer.getRespawnDimension();
            ServerLevel targetLevel = level.getServer().getLevel(respawnDimension);
            if (targetLevel == null) {
               serverPlayer.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.respawn.no_dimension"), true);
            } else {
               BlockPos targetBlockPos;
               if (respawnPos == null) {
                  targetBlockPos = targetLevel.getSharedSpawnPos();
               } else {
                  targetBlockPos = respawnPos;
               }

               BlockPos safePos = this.findSafePositionAround(targetLevel, targetBlockPos);
               Vec3 targetPos = Vec3.atBottomCenterOf(safePos);
               level.playSound(
                  null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F
               );
               if (level.dimension() == respawnDimension) {
                  serverPlayer.teleportTo(targetPos.x, targetPos.y, targetPos.z);
               } else {
                  serverPlayer.changeDimension(
                     new DimensionTransition(
                        targetLevel, targetPos, Vec3.ZERO, serverPlayer.getYRot(), serverPlayer.getXRot(), false, DimensionTransition.DO_NOTHING
                     )
                  );
               }

               serverPlayer.fallDistance = 0.0F;
               serverPlayer.level().playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
               serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
            }
         }
      }
   }

   private boolean isSafePosition(ServerLevel level, BlockPos pos) {
      BlockPos headPos = pos.above();
      BlockPos belowPos = pos.below();
      BlockState feetState = level.getBlockState(pos);
      BlockState headState = level.getBlockState(headPos);
      BlockState belowState = level.getBlockState(belowPos);
      boolean feetNoCollision = feetState.getCollisionShape(level, pos).isEmpty();
      boolean headNoCollision = headState.getCollisionShape(level, headPos).isEmpty();
      return feetNoCollision && headNoCollision ? belowState.isSolid() || belowState.liquid() : false;
   }

   private BlockPos findSafePositionAround(ServerLevel level, BlockPos centerPos) {
      if (this.isSafePosition(level, centerPos)) {
         return centerPos;
      } else {
         int maxRadius = 3;
         int minYOffset = -3;
         int maxYOffset = 3;

         for (int yOffset = 0; yOffset >= minYOffset; yOffset--) {
            BlockPos safePos = this.searchSameYLevel(level, centerPos.atY(centerPos.getY() + yOffset), maxRadius);
            if (safePos != null) {
               return safePos;
            }
         }

         for (int yOffsetx = 1; yOffsetx <= maxYOffset; yOffsetx++) {
            BlockPos safePos = this.searchSameYLevel(level, centerPos.atY(centerPos.getY() + yOffsetx), maxRadius);
            if (safePos != null) {
               return safePos;
            }
         }

         return centerPos;
      }
   }

   @Nullable
   private BlockPos searchSameYLevel(ServerLevel level, BlockPos centerPos, int maxRadius) {
      for (int radius = 1; radius <= maxRadius; radius++) {
         for (int x = -radius; x <= radius; x++) {
            BlockPos checkPos = centerPos.offset(x, 0, -radius);
            if (this.isSafePosition(level, checkPos)) {
               return checkPos;
            }
         }

         for (int z = -radius + 1; z <= radius; z++) {
            BlockPos checkPos = centerPos.offset(radius, 0, z);
            if (this.isSafePosition(level, checkPos)) {
               return checkPos;
            }
         }

         for (int xx = radius - 1; xx >= -radius; xx--) {
            BlockPos checkPos = centerPos.offset(xx, 0, radius);
            if (this.isSafePosition(level, checkPos)) {
               return checkPos;
            }
         }

         for (int zx = radius - 1; zx >= -radius + 1; zx--) {
            BlockPos checkPos = centerPos.offset(-radius, 0, zx);
            if (this.isSafePosition(level, checkPos)) {
               return checkPos;
            }
         }
      }

      return null;
   }
}
