package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.item.CompatFoodItem;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 兼容物品：注册在 kaleidoscope_twilight / smc 命名空间下，供联动模组缺失时使用。
 * 凉山甜筒、喜多夹心脆、波奇布丁、妙脆角 + 冰红茶（方块+物品）。
 */
public final class ModCompatItems {
   private ModCompatItems() {
   }

   // ============ kaleidoscope_twilight ============
   public static final Item LIANGSHAN_ICE_CONE = new CompatFoodItem(
      new Item.Properties().stacksTo(64).food(ModFoods.LIANGSHAN_ICE_CONE), "kaleidoscope_twilight"
   );
   public static final Item KITA_STUFFED_CRISP = new CompatFoodItem(
      new Item.Properties().stacksTo(64).food(ModFoods.KITA_STUFFED_CRISP), "kaleidoscope_twilight"
   );
   public static final Item POCHI_PUDDING = new CompatFoodItem(
      new Item.Properties().stacksTo(64).food(ModFoods.POCHI_PUDDING), "kaleidoscope_twilight", Items.BOWL
   );
   public static final Item MAGIC_CRISPY_CORNER = new CompatFoodItem(
      new Item.Properties().stacksTo(64).food(ModFoods.MAGIC_CRISPY_CORNER), "kaleidoscope_twilight"
   );

   // ============ smc：冰红茶 ============
   public static final Block ICE_TEA_BLOCK = DrinkBlock.create()
      .maxCount(4)
      .shapes(
         new VoxelShape[]{
            Block.box(4.0, 0.0, 5.0, 12.0, 14.0, 11.0),
            Block.box(0.5, 0.0, 4.0, 15.5, 14.0, 12.0),
            Shapes.or(Block.box(0.5, 0.0, 7.0, 15.5, 14.0, 15.5), Block.box(4.0, 0.0, 0.5, 12.0, 14.0, 7.0)),
            Block.box(0.5, 0.0, 0.5, 15.5, 14.0, 15.5)
         }
      )
      .build();
   public static final Item ICE_TEA = new DrinkBlockItem(ICE_TEA_BLOCK);

   public static void register() {
      // kaleidoscope_twilight 命名空间
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_twilight", "liangshan_ice_cone"), LIANGSHAN_ICE_CONE);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_twilight", "kita_stuffed_crisp"), KITA_STUFFED_CRISP);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_twilight", "pochi_pudding"), POCHI_PUDDING);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_twilight", "magic_crispy_corner"), MAGIC_CRISPY_CORNER);
      // smc 命名空间：冰红茶方块 + 物品
      Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("smc", "ice_tea"), ICE_TEA_BLOCK);
      Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("smc", "ice_tea"), ICE_TEA);
   }
}
