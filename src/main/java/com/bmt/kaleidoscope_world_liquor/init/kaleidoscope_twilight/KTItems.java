package com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight;

import com.bmt.kaleidoscope_world_liquor.init.ModFoods;
import com.bmt.kaleidoscope_world_liquor.item.CompatFoodItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

/**
 * twilight 4 食物代注册（kaleidoscope_twilight 命名空间，无条件注册——
 * 模组缺失时配方/图鉴/创造栏仍可用，与 1.20.1 原版一致）。
 */
public final class KTItems {
    public static final String KT_MODID = "kaleidoscope_twilight";

    private static Item register(String name, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(KT_MODID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, new CompatFoodItem(properties.setId(key), KT_MODID));
    }

    public static final Item LIANGSHAN_ICE_CONE = register("liangshan_ice_cone",
            new Item.Properties().stacksTo(64).food(ModFoods.LIANGSHAN_ICE_CONE, ModFoods.LIANGSHAN_ICE_CONE_CONSUMABLE));
    public static final Item KITA_STUFFED_CRISP = register("kita_stuffed_crisp",
            new Item.Properties().stacksTo(64).food(ModFoods.KITA_STUFFED_CRISP, ModFoods.KITA_STUFFED_CRISP_CONSUMABLE));
    public static final Item POCHI_PUDDING = register("pochi_pudding",
            new Item.Properties().stacksTo(64)
                    .food(ModFoods.POCHI_PUDDING, ModFoods.POCHI_PUDDING_CONSUMABLE)
                    .component(net.minecraft.core.component.DataComponents.USE_REMAINDER,
                            new net.minecraft.world.item.component.UseRemainder(new net.minecraft.world.item.ItemStackTemplate(net.minecraft.world.item.Items.BOWL))));
    public static final Item MAGIC_CRISPY_CORNER = register("magic_crispy_corner",
            new Item.Properties().stacksTo(64).food(ModFoods.MAGIC_CRISPY_CORNER, ModFoods.MAGIC_CRISPY_CORNER_CONSUMABLE));

    private KTItems() {
    }

    public static void register() {
    }
}
