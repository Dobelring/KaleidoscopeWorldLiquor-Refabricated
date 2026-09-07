package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems;
import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * 两个创造栏：酒水栏 + 家具栏。条目顺序照 1.20.1 原版。
 * 冰柜/酒柜/酒窖柜物品在第 5 步注册后加入家具栏与酒水栏。
 */
public final class ModCreativeModeTabs {
    private ModCreativeModeTabs() {
    }

    private static final ResourceKey<CreativeModeTab> KALEIDOSCOPE_WORLD_LIQUOR_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "kaleidoscope_world_liquor_tab"));
    private static final ResourceKey<CreativeModeTab> KALEIDOSCOPE_WORLD_LIQUOR_FURNITURE_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "kaleidoscope_world_liquor_furniture_tab"));

    private static void drinks(CreativeModeTab.Output output) {
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.BOMBAY_SAPPHIRE_GIN));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.JACK_DANIEL));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SMIRNOFF_RED_VODKA));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.ABSOLUT_VODKA));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.PINA_COLADA));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.MAOTAI));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.BACARDI_CARTA_BLANCA));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SPIRYT_VODKA));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SKYY_VODKA));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.JOHNNIE_WALKER));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.LAFITE_1982));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.STRONGBOW));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.DASSAI));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.BAMBOO_LEAF_GREEN_LIQUOR));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.KWAS_CHLEBOWY));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.COOL_TEA));
        output.accept(BottleBlockItem.getMaxLevelDrink(ModItems.SOUR_PLUM));
        output.accept(ModItems.COLA);
        output.accept(ModItems.TONIC_WATER);
        output.accept(ModItems.JERK);
        output.accept(ModItems.SHRIMP_COOKTAIL);
        output.accept(ModItems.AROUND_THE_WORLD);
        output.accept(ModItems.LONG_ISLAND_ICED_TEA);
        output.accept(ModItems.PINE_COLADA);
        output.accept(ModItems.GIN_TONIC);
        output.accept(ModItems.FREEZER);
        output.accept(ModItems.CUSTOM_RECORD);
        // 玩偶（1.21.11 无 doll 模组，代注册进酒水栏）
        output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(PortHelper.createItemId("doll_0")));
        output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(PortHelper.createItemId("doll_1")));
        output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(PortHelper.createItemId("doll_2")));
        output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(PortHelper.createItemId("doll_3")));
        output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(PortHelper.createItemId("doll_4")));
        output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(PortHelper.createItemId("doll_5")));
        // twilight 缺席时，联动食物由本模组代注册并进酒水栏（第 12 步接 KTItems）
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("kaleidoscope_twilight")) {
            output.accept(KTItems.LIANGSHAN_ICE_CONE);
            output.accept(KTItems.KITA_STUFFED_CRISP);
            output.accept(KTItems.POCHI_PUDDING);
            output.accept(KTItems.MAGIC_CRISPY_CORNER);
        }
        // smc 冰红茶照 1.20.1/1.21.1：创造栏给最高品质（否则无 tooltip 品质/buff、喝了无效果）
        output.accept(com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem.getMaxLevelDrink(SMCItems.SMC_ICE_TEA_ITEM));
    }

    private static void furniture(CreativeModeTab.Output output) {
        output.accept(ModItems.BAR_STOOL_WHITE);
        output.accept(ModItems.BAR_STOOL_LIGHT_GRAY);
        output.accept(ModItems.BAR_STOOL_GRAY);
        output.accept(ModItems.BAR_STOOL_BLACK);
        output.accept(ModItems.BAR_STOOL_BROWN);
        output.accept(ModItems.BAR_STOOL_RED);
        output.accept(ModItems.BAR_STOOL_ORANGE);
        output.accept(ModItems.BAR_STOOL_YELLOW);
        output.accept(ModItems.BAR_STOOL_LIME);
        output.accept(ModItems.BAR_STOOL_GREEN);
        output.accept(ModItems.BAR_STOOL_CYAN);
        output.accept(ModItems.BAR_STOOL_LIGHT_BLUE);
        output.accept(ModItems.BAR_STOOL_BLUE);
        output.accept(ModItems.BAR_STOOL_PURPLE);
        output.accept(ModItems.BAR_STOOL_MAGENTA);
        output.accept(ModItems.BAR_STOOL_PINK);
        output.accept(ModItems.OAK_BAR_CABINET);
        output.accept(ModItems.OAK_CELLAR_CABINET);
        output.accept(ModItems.BIRCH_BAR_CABINET);
        output.accept(ModItems.BIRCH_CELLAR_CABINET);
        output.accept(ModItems.SPRUCE_BAR_CABINET);
        output.accept(ModItems.SPRUCE_CELLAR_CABINET);
        output.accept(ModItems.DARK_OAK_BAR_CABINET);
        output.accept(ModItems.DARK_OAK_CELLAR_CABINET);
        output.accept(ModItems.CHERRY_BAR_CABINET);
        output.accept(ModItems.CHERRY_CELLAR_CABINET);
        // 酒柜/酒窖柜物品第 5 步加入
    }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KALEIDOSCOPE_WORLD_LIQUOR_TAB, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.kaleidoscope_world_liquor_tab"))
                .icon(() -> new ItemStack(ModItems.BOMBAY_SAPPHIRE_GIN))
                .displayItems((parameters, output) -> drinks(output))
                .build());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KALEIDOSCOPE_WORLD_LIQUOR_FURNITURE_TAB, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.kaleidoscope_world_liquor_furniture_tab"))
                .icon(() -> new ItemStack(ModItems.BAR_STOOL_WHITE))
                .displayItems((parameters, output) -> furniture(output))
                .build());
    }
}
