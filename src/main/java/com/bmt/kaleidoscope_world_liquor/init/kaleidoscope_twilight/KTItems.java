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
    public static final ResourceLocation LIANGSHAN_ICE_CONE_ID = new ResourceLocation("kaleidoscope_twilight", "liangshan_ice_cone");
    public static final ResourceLocation KITA_STUFFED_CRISP_ID = new ResourceLocation("kaleidoscope_twilight", "kita_stuffed_crisp");
    public static final ResourceLocation POCHI_PUDDING_ID = new ResourceLocation("kaleidoscope_twilight", "pochi_pudding");
    public static final ResourceLocation MAGIC_CRISPY_CORNER_ID = new ResourceLocation("kaleidoscope_twilight", "magic_crispy_corner");
    public static final Item LIANGSHAN_ICE_CONE = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.LIANGSHAN_ICE_CONE), "kaleidoscope_twilight");
    public static final Item KITA_STUFFED_CRISP = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.KITA_STUFFED_CRISP), "kaleidoscope_twilight");
    public static final Item POCHI_PUDDING = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.POCHI_PUDDING), "kaleidoscope_twilight", Items.BOWL);
    public static final Item MAGIC_CRISPY_CORNER = new CompatFoodItem(new Properties().stacksTo(64).food(ModFoods.MAGIC_CRISPY_CORNER), "kaleidoscope_twilight");

    public KTItems() {
    }

    public static void registerKTItems() {
        Registry.register(BuiltInRegistries.ITEM, LIANGSHAN_ICE_CONE_ID, LIANGSHAN_ICE_CONE);
        Registry.register(BuiltInRegistries.ITEM, KITA_STUFFED_CRISP_ID, KITA_STUFFED_CRISP);
        Registry.register(BuiltInRegistries.ITEM, POCHI_PUDDING_ID, POCHI_PUDDING);
        Registry.register(BuiltInRegistries.ITEM, MAGIC_CRISPY_CORNER_ID, MAGIC_CRISPY_CORNER);
    }
}
