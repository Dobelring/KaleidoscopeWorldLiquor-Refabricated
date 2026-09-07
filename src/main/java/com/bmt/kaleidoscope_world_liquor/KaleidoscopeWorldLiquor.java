package com.bmt.kaleidoscope_world_liquor;

import com.bmt.kaleidoscope_world_liquor.config.ModConfigs;
import com.bmt.kaleidoscope_world_liquor.event.BrewCommands;
import com.bmt.kaleidoscope_world_liquor.event.DollInteractionEvents;
import com.bmt.kaleidoscope_world_liquor.event.EventHandlers;
import com.bmt.kaleidoscope_world_liquor.event.FreezerTapBehavior;
import com.bmt.kaleidoscope_world_liquor.event.MiscEvents;
import com.bmt.kaleidoscope_world_liquor.event.MusicDiscEvents;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.DollIntegration;
import com.bmt.kaleidoscope_world_liquor.init.ModCreativeModeTabs;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import com.bmt.kaleidoscope_world_liquor.init.ModEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.init.ModPaintings;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KaleidoscopeWorldLiquor implements ModInitializer {
    public static final String MOD_ID = "kaleidoscope_world_liquor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfigs.register();
        ModSounds.register();
        // 流体先于方块：MILK_LIQUID_BLOCK 构造需要 MILK_STILL 实例
        ModFluids.register();
        ModBlocks.register();
        ModBlocks.registerDrinkBeSupportedBlocks();
        ModPaintings.register();
        ModEntities.register();
        SMCItems.register();
        KTItems.register();
        ModItems.register();
        ModBlockEntities.register();
        ModRecipes.register();
        // 客户端配方同步（tavern 同款：JEI 在场才需要；不开 JEI 查询走不到同步表也无妨）
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("jei")) {
            com.bmt.kaleidoscope_world_liquor.compat.jei.ModJeiPlugin.syncRecipes();
        }
        ModEffects.register();
        ModEnchantments.register();
        DollIntegration.register();
        ModCreativeModeTabs.register();
        com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity.registerFluidStorage();
        EventHandlers.register();
        DollInteractionEvents.register();
        MusicDiscEvents.register();
        MiscEvents.registerBrewAccelerator();
        BrewCommands.register();
        FreezerTapBehavior.register();
        LOGGER.info("[Kaleidoscope World Liquor] initialized");
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
