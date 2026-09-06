package com.bmt.kaleidoscope_world_liquor.fluids;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

/**
 * 流体工具：在 1.21.1 中 BucketItem 不再提供 getFluid()，
 * 改为遍历流体注册表，通过 Fluid.getBucket() 反查物品对应的流体。
 */
public final class FluidUtils {
   private FluidUtils() {
   }

   /**
    * 返回持有该物品作为桶的流体；空桶（无对应流体）返回 null。
    */
   public static Fluid findFluidForBucket(Item item) {
      for (Fluid fluid : BuiltInRegistries.FLUID) {
         if (fluid.getBucket() == item) {
            return fluid;
         }
      }
      return null;
   }
}
