package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.fluids.MilkFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

/**
 * 牛奶流体注册（Still/Flowing）。必须在 ModBlocks 之前调用——
 * LiquidBlock 构造需要 STILL 实例；MilkFluid.createLegacyBlock 引用的
 * MILK_LIQUID_BLOCK 为运行期惰性引用。
 */
public final class ModFluids {
    private ModFluids() {
    }

    public static FlowingFluid MILK_STILL;
    public static FlowingFluid MILK_FLOWING;

    public static void register() {
        MILK_STILL = (FlowingFluid) Registry.register(BuiltInRegistries.FLUID, id("milk_still"), MilkFluid.STILL);
        MILK_FLOWING = (FlowingFluid) Registry.register(BuiltInRegistries.FLUID, id("milk_flowing"), MilkFluid.FLOWING);
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor.MOD_ID, name);
    }
}
