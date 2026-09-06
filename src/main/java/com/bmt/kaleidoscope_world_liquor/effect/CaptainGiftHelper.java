package com.bmt.kaleidoscope_world_liquor.effect;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 船长赠礼水上行走（1.20.1 CaptainGiftMixinHooks 迁移）：
 * 未潜行、携带效果、下落（或平行移动）时，在 12 个候选偏移里找水面并把 y 提到水面高度。
 */
public final class CaptainGiftHelper {
    private static final int[][] OFFSETS = {
            {1, 0, 1}, {1, 0, 0}, {1, -1, 0}, {1, 0, -1}, {0, 0, 1}, {0, 0, 0},
            {0, -1, 0}, {0, 0, -1}, {-1, 0, 1}, {-1, 0, 0}, {-1, -1, 0}, {-1, 0, -1}
    };

    private CaptainGiftHelper() {
    }

    public static Vec3 enableWaterWalking(Entity entity, Vec3 original) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return original;
        }
        if (original.y > 0.0 || !livingEntity.hasEffect(ModEffects.CAPTAIN_GIFT) || livingEntity.isCrouching()) {
            return original;
        }
        Level level = livingEntity.level();
        double highestWaterY = original.y;
        boolean foundWater = false;
        BlockPos sourcePos = livingEntity.blockPosition();
        for (int[] offset : OFFSETS) {
            BlockPos pos = new BlockPos(sourcePos.getX() + offset[0], sourcePos.getY() + offset[1], sourcePos.getZ() + offset[2]);
            FluidState fluidState = level.getFluidState(pos);
            if (!fluidState.isEmpty() && fluidState.is(FluidTags.WATER)) {
                VoxelShape shape = Shapes.block().move(pos.getX(), pos.getY() + fluidState.getHeight(level, pos), pos.getZ());
                if (Shapes.joinIsNotEmpty(shape, Shapes.create(livingEntity.getBoundingBox().move(0.5, 0.0, 0.5)), BooleanOp.AND)) {
                    double height = shape.max(Direction.Axis.Y) - livingEntity.getY() - 1.0;
                    if (highestWaterY < height) {
                        highestWaterY = height;
                        foundWater = true;
                    }
                }
            }
        }
        if (foundWater) {
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().x, 0.0, livingEntity.getDeltaMovement().z);
            livingEntity.fallDistance = 0.0F;
            livingEntity.setNoGravity(true);
            return new Vec3(original.x, highestWaterY, original.z);
        }
        return original;
    }
}
