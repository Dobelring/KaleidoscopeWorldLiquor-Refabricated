package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.fluids.MilkFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModFluids {
    public static final MilkFluid.Still MILK_STILL = new MilkFluid.Still();
    public static final MilkFluid.Flowing MILK_FLOWING = new MilkFluid.Flowing();

    public ModFluids() {
    }

    public static void registerFluids() {
        Registry.register(BuiltInRegistries.FLUID, KaleidoscopeWorldLiquor.id("milk_still"), MILK_STILL);
        Registry.register(BuiltInRegistries.FLUID, KaleidoscopeWorldLiquor.id("milk_flowing"), MILK_FLOWING);
    }
}
