package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 官方 1.2.0：霜行效果从玩家 tick 挪到 onChangedBlock 钩子——任意生物持效果移动时冻结水面，
 * 带 canSurvive/无阻碍判定；站定不再持续冻结。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFrostWalkerMixin {
   @Inject(method = "onChangedBlock", at = @At("TAIL"))
   private void kaleidoscope$applyFrostWalkerEffect(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity) (Object) this;
      MobEffectInstance effect = entity.getEffect(ModEffects.FROST_WALKER_EFFECT);
      if (effect != null && entity.onGround()) {
         int radius = Math.min(16, 2 + effect.getAmplifier() + 1);
         BlockState frostedIceState = Blocks.FROSTED_ICE.defaultBlockState();
         MutableBlockPos abovePos = new MutableBlockPos();

         for (BlockPos waterPos : BlockPos.betweenClosed(pos.offset(-radius, -1, -radius), pos.offset(radius, -1, radius))) {
            if (waterPos.closerToCenterThan(entity.position(), radius)) {
               abovePos.set(waterPos.getX(), waterPos.getY() + 1, waterPos.getZ());
               if (serverLevel.getBlockState(abovePos).isAir()) {
                  BlockState state = serverLevel.getBlockState(waterPos);
                  if (state.is(Blocks.WATER)
                     && state.getFluidState().is(Fluids.WATER)
                     && state.getFluidState().isSource()
                     && frostedIceState.canSurvive(serverLevel, waterPos)
                     && serverLevel.isUnobstructed(frostedIceState, waterPos, CollisionContext.empty())) {
                     serverLevel.setBlockAndUpdate(waterPos, frostedIceState);
                     serverLevel.scheduleTick(waterPos, Blocks.FROSTED_ICE, Mth.nextInt(entity.getRandom(), 60, 120));
                  }
               }
            }
         }
      }
   }
}
