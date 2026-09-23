package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems;
import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * 单个创造栏（酒水 + 装饰），左侧由客户端 {@code client/creativetab/CreativeTabFilter} 提供分类过滤按钮
 * ——1.1.9 起官方把原来的「酒水栏 + 家具栏」两个 tab 合并为一个 tab + 过滤按钮。
 *
 * <p>两份快照（{@link #LIQUOR_ITEMS} / {@link #FURNITURE_ITEMS}）在 displayItems 回调里重建，
 * 过滤按钮据此整体替换创造界面的物品列表（与官方 1.1.9 同做法）。
 *
 * <p>8 幅作者画仍挂在 tavern 装饰栏（1.20.1 原版 putAfter MASTER_MARISA_PAINTING 同位）。
 */
public final class ModCreativeModeTabs {
    private ModCreativeModeTabs() {
    }

    public static final ResourceKey<CreativeModeTab> KALEIDOSCOPE_WORLD_LIQUOR_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "kaleidoscope_world_liquor_tab"));

    /** 过滤按钮用的分类快照：displayItems 回调里先 clear 再填充 */
    public static final NonNullList<ItemStack> LIQUOR_ITEMS = NonNullList.create();
    public static final NonNullList<ItemStack> FURNITURE_ITEMS = NonNullList.create();

    private static void accept(CreativeModeTab.Output output, NonNullList<ItemStack> categoryItems, ItemStack stack) {
        output.accept(stack);
        categoryItems.add(stack.copy());
    }

    private static void accept(CreativeModeTab.Output output, NonNullList<ItemStack> categoryItems, ItemLike item) {
        output.accept(item);
        categoryItems.add(new ItemStack(item));
    }

    private static void addLiquorItems(CreativeModeTab.Output output) {
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.BOMBAY_SAPPHIRE_GIN));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.JACK_DANIEL));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SMIRNOFF_RED_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.ABSOLUT_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.PINA_COLADA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.MAOTAI));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.BACARDI_CARTA_BLANCA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SPIRYT_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SKYY_VODKA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.JOHNNIE_WALKER));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.LAFITE_1982));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.STRONGBOW));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.DASSAI));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.BAMBOO_LEAF_GREEN_LIQUOR));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.KWAS_CHLEBOWY));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.COOL_TEA));
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(ModItems.SOUR_PLUM));
        accept(output, LIQUOR_ITEMS, ModItems.COLA);
        accept(output, LIQUOR_ITEMS, ModItems.TONIC_WATER);
        accept(output, LIQUOR_ITEMS, ModItems.JERK);
        accept(output, LIQUOR_ITEMS, ModItems.SHRIMP_COOKTAIL);
        accept(output, LIQUOR_ITEMS, ModItems.AROUND_THE_WORLD);
        accept(output, LIQUOR_ITEMS, ModItems.HIGHBALL);
        accept(output, LIQUOR_ITEMS, ModItems.LONG_ISLAND_ICED_TEA);
        accept(output, LIQUOR_ITEMS, ModItems.PINE_COLADA);
        accept(output, LIQUOR_ITEMS, ModItems.GIN_TONIC);
        accept(output, LIQUOR_ITEMS, ModItems.FREEZER);
        accept(output, LIQUOR_ITEMS, ModItems.CUSTOM_RECORD);
        // twilight 缺席时，联动食物由本模组代注册并进酒水栏
        if (!FabricLoader.getInstance().isModLoaded("kaleidoscope_twilight")) {
            accept(output, LIQUOR_ITEMS, KTItems.LIANGSHAN_ICE_CONE);
            accept(output, LIQUOR_ITEMS, KTItems.KITA_STUFFED_CRISP);
            accept(output, LIQUOR_ITEMS, KTItems.POCHI_PUDDING);
            accept(output, LIQUOR_ITEMS, KTItems.MAGIC_CRISPY_CORNER);
        }
        // smc 冰红茶照 1.20.1/1.21.1：创造栏给最高品质（否则无 tooltip 品质/buff、喝了无效果）
        accept(output, LIQUOR_ITEMS, BottleBlockItem.getMaxLevelDrink(SMCItems.SMC_ICE_TEA_ITEM));
    }

    private static void addFurnitureItems(CreativeModeTab.Output output) {
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_WHITE);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_LIGHT_GRAY);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_GRAY);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_BLACK);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_BROWN);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_RED);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_ORANGE);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_YELLOW);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_LIME);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_GREEN);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_CYAN);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_LIGHT_BLUE);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_BLUE);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_PURPLE);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_MAGENTA);
        accept(output, FURNITURE_ITEMS, ModItems.BAR_STOOL_PINK);
        accept(output, FURNITURE_ITEMS, ModItems.OAK_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.OAK_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.BIRCH_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.BIRCH_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.SPRUCE_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.SPRUCE_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.DARK_OAK_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.DARK_OAK_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.CHERRY_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.CHERRY_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.JUNGLE_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.JUNGLE_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.ACACIA_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.ACACIA_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.MANGROVE_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.MANGROVE_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.PALE_OAK_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.PALE_OAK_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.POPLAR_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.POPLAR_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.BAMBOO_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.BAMBOO_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.CRIMSON_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.CRIMSON_CELLAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.WARPED_BAR_CABINET);
        accept(output, FURNITURE_ITEMS, ModItems.WARPED_CELLAR_CABINET);
        // 玩偶（1.21.11+ 无 doll 模组，本模组代注册）：用户拍板归"装饰"分类
        for (int i = 0; i < 6; i++) {
            accept(output, FURNITURE_ITEMS, BuiltInRegistries.ITEM.getValue(PortHelper.id("doll_" + i)));
        }
    }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KALEIDOSCOPE_WORLD_LIQUOR_TAB, FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.kaleidoscope_world_liquor_tab"))
                .icon(() -> new ItemStack(ModItems.BOMBAY_SAPPHIRE_GIN))
                .displayItems((parameters, output) -> {
                    LIQUOR_ITEMS.clear();
                    FURNITURE_ITEMS.clear();
                    addLiquorItems(output);
                    addFurnitureItems(output);
                })
                .build());
        // 8 幅作者画挂在 tavern 的 MASTER_MARISA_PAINTING 之后（1.20.1 原版 putAfter 同位）。
        ResourceKey<CreativeModeTab> tavernDecoTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("kaleidoscope_tavern", "tavern_deco"));
        CreativeModeTabEvents.modifyOutputEvent(tavernDecoTab).register(entries ->
                ((FabricCreativeModeTabOutput) entries).insertAfter(
                        com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.MASTER_MARISA_PAINTING,
                        ModPaintings.BFXM_PAINTING, ModPaintings.BMT_PAINTING, ModPaintings.CHEN_PAINTING,
                        ModPaintings.DREAM_PAINTING, ModPaintings.CHA_PAINTING, ModPaintings.RABBIT_PAINTING,
                        ModPaintings.CH_PAINTING, ModPaintings.QXXY_PAINTING));
    }
}
