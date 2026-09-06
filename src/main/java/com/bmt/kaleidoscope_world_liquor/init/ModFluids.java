package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.fluids.MilkFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

public final class ModFluids {
   public static final FlowingFluid MILK_STILL = new MilkFluid.Still(
      () -> ModFluids.MILK_FLOWING,
      () -> ModFluids.MILK_STILL,
      () -> Items.MILK_BUCKET,
      () -> ModFluids.MILK_BLOCK
   );
   public static final FlowingFluid MILK_FLOWING = new MilkFluid.Flowing(
      () -> ModFluids.MILK_FLOWING,
      () -> ModFluids.MILK_STILL,
      () -> Items.MILK_BUCKET,
      () -> ModFluids.MILK_BLOCK
   );
   public static final LiquidBlock MILK_BLOCK = new LiquidBlock(MILK_STILL, Block.Properties.ofFullCopy(Blocks.WATER));

   public static void registerFluids() {
      Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "milk_still"), MILK_STILL);
      Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "milk_flowing"), MILK_FLOWING);
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "milk"), MILK_BLOCK);
   }
}
