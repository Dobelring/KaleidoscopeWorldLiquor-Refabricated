package com.bmt.kaleidoscope_world_liquor.init.smc;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.loot.RandomBrewLevelFunction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

/**
 * 官方 1.2.0：smc 战利品随机酿造等级的 LootItemFunctionType。
 * 原版为 DeferredRegister 且仅在 smc 在场时注册（主类守卫）；Fabric 等价：
 * register() 在 smc 在场时直接注册表注册。
 */
public class SMCLootFunctionTypes {
    public static LootItemFunctionType RANDOM_BREW_LEVEL;

    public SMCLootFunctionTypes() {
    }

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("smc")) {
            RANDOM_BREW_LEVEL = Registry.register(
                BuiltInRegistries.LOOT_FUNCTION_TYPE,
                KaleidoscopeWorldLiquor.id("random_brew_level"),
                new LootItemFunctionType(new RandomBrewLevelFunction.RandomBrewLevelSerializer())
            );
        }
    }
}
