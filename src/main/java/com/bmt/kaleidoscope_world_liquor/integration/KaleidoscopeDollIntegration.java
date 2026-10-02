package com.bmt.kaleidoscope_world_liquor.integration;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 森罗物语：玩偶（kaleidoscope_doll）联动门面。
 * 当玩偶模组加载时，注册 doll_0..5 作者玩偶方块/物品到本模组命名空间，
 * 并登记 SPECIAL_TOOLTIPS 供玩偶模组显示贡献者提示。
 *
 * <p>与官方 Forge/NeoForge 一致拆分为门面 + 实现两类：玩偶类（DollBlock/DollItem）
 * 只出现在 {@link KaleidoscopeDollIntegrationImpl}，守卫通过后才首次加载该类。
 * 若两类合一，JVM 在调用本类方法前会校验全部方法体，玩偶模组缺席时将因
 * NoClassDefFoundError 在调用点直接崩溃，守卫永远无法生效。
 */
public class KaleidoscopeDollIntegration {
   private static final Map<ResourceLocation, Block> DOLL_BLOCKS = new LinkedHashMap<>();
   private static final Map<ResourceLocation, Item> DOLL_ITEMS = new LinkedHashMap<>();

   private KaleidoscopeDollIntegration() {
   }

   public static void register() {
      if (!FabricLoader.getInstance().isModLoaded("kaleidoscope_doll")) {
         return;
      }
      KaleidoscopeDollIntegrationImpl.register(DOLL_BLOCKS, DOLL_ITEMS);
   }

   public static Map<ResourceLocation, Item> getDollItems() {
      return DOLL_ITEMS;
   }
}
