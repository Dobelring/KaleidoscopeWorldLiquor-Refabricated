package com.bmt.kaleidoscope_world_liquor;

import com.bmt.kaleidoscope_world_liquor.compat.create.CreateCompat;
import com.bmt.kaleidoscope_world_liquor.command.BrewCommands;
import com.bmt.kaleidoscope_world_liquor.config.ModConfigs;
import com.bmt.kaleidoscope_world_liquor.event.BrewAcceleratorEventHandler;
import com.bmt.kaleidoscope_world_liquor.event.CreativeTabEvents;
import com.bmt.kaleidoscope_world_liquor.event.DamageEvents;
import com.bmt.kaleidoscope_world_liquor.event.DollInteractionEvents;
import com.bmt.kaleidoscope_world_liquor.event.EventHandlers;
import com.bmt.kaleidoscope_world_liquor.event.MusicDiscEvents;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModCreativeModeTabs;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.init.ModPaintings;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCIntegrationEvents;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems;
import com.bmt.kaleidoscope_world_liquor.integration.KaleidoscopeDollIntegration;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主入口点。
 * <p>
 * 原 Forge 版在 @Mod 构造器里拿 mod 事件总线做全部注册；Fabric 拆成
 * 本类（服务端+客户端共用注册）与 {@link com.bmt.kaleidoscope_world_liquor.client.KaleidoscopeWorldLiquorClient}（客户端注册）。
 */
public class KaleidoscopeWorldLiquor implements ModInitializer {
    public static final String MODID = "kaleidoscope_world_liquor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(MODID, Type.COMMON, ModConfigs.SPEC);

        // 注册顺序：音效/效果/附魔先行，流体先于引用流体的方块，方块先于方块物品，
        // 方块实体在其方块之后；创造栏在全部物品之后（displayItems 是延迟 lambda，仅要求类可初始化）。
        ModSounds.registerSounds();
        ModEffects.registerEffects();
        ModEnchantments.registerEnchantments();
        ModFluids.registerFluids();
        ModBlocks.registerBlocks();
        ModItems.registerItems();
        ModBlockEntities.registerBlockEntities();
        ModRecipes.registerRecipes();
        SMCItems.registerSMCItems();
        if (!FabricLoader.getInstance().isModLoaded("kaleidoscope_twilight")) {
            KTItems.registerKTItems();
        }
        ModCreativeModeTabs.registerCreativeModeTabs();
        ModPaintings.registerPaintings();

        // 原 Forge 总线监听器（@EventBusSubscriber → 显式注册）
        EventHandlers.register();
        DamageEvents.register();
        DollInteractionEvents.register();
        MusicDiscEvents.register();
        BrewAcceleratorEventHandler.register();
        CreativeTabEvents.register();
        // 原 RegisterCommandsEvent → CommandRegistrationCallback（BrewCommands 内部注册）
        BrewCommands.register();
        // 原 Forge 仅在 smc 装载时注册本监听——但代注册冰红茶（smc 未装时由本模组提供物品/效果）
        // 同样要给肘击等 buff（行为规格 §57），故改为无条件注册；smc 未装时 elbow_strike 解析到代注册效果。
        SMCIntegrationEvents.register();

        // 玩偶联动门控：kaleidoscope_doll 已装 && kaleidoscope_nether 未装 才注册 doll_0..5
        KaleidoscopeDollIntegration.register();

        // 原 FMLCommonSetupEvent：Create 联动（移动行为/交互行为注册）
        if (FabricLoader.getInstance().isModLoaded("create")) {
            CreateCompat.register();
        }
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }

    /**
     * 原 Forge 版的同名便利方法（Forge 47.x 给 ResourceLocation 补了 fromNamespaceAndPath）。
     * 1.20.1 原版没有该方法，这里保留同名包装以兼容既有调用点。
     */
    public static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
