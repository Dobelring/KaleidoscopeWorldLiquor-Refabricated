package com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight;

import com.bmt.kaleidoscope_world_liquor.init.ModFoods;
import com.bmt.kaleidoscope_world_liquor.item.CompatFoodItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;

/**
 * kaleidoscope_twilight 缺失时由本模组代注册的四个食物（原 Forge 版由主类在
 * {@code !ModList.isLoaded("kaleidoscope_twilight")} 守卫下调用注册；
 * Fabric 版语义相同：主类在 {@code !FabricLoader.isModLoaded("kaleidoscope_twilight")}
 * 时调用 {@link #registerKTItems()}，twilight 已装时本类不会被注册。
 */
public class KTItems {
    public static final String KT_MODID = "kaleidoscope_twilight";
    // 官方 1.2.0：四个 *_ID 公开常量移除（DollInteractionEvents 自持需要的 id），
    // 注册 id 在 registerKTItems 内联。
    public static final Item LIANGSHAN_ICE_CONE = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.LIANGSHAN_ICE_CONE), "kaleidoscope_twilight");
    public static final Item KITA_STUFFED_CRISP = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.KITA_STUFFED_CRISP), "kaleidoscope_twilight");
    public static final Item POCHI_PUDDING = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.POCHI_PUDDING), "kaleidoscope_twilight", Items.BOWL);
    public static final Item MAGIC_CRISPY_CORNER = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.MAGIC_CRISPY_CORNER), "kaleidoscope_twilight");

    public KTItems() {
    }

    public static void registerKTItems() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KT_MODID, "liangshan_ice_cone"), LIANGSHAN_ICE_CONE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KT_MODID, "kita_stuffed_crisp"), KITA_STUFFED_CRISP);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KT_MODID, "pochi_pudding"), POCHI_PUDDING);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(KT_MODID, "magic_crispy_corner"), MAGIC_CRISPY_CORNER);
    }
}
