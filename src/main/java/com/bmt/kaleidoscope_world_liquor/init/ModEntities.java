package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.entity.ChairEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
   public static final EntityType<ChairEntity> OAK_LOG_STOOL = EntityType.Builder.of(ChairEntity::new, MobCategory.MISC)
      .sized(0.0F, 0.0F)
      .clientTrackingRange(10)
      .updateInterval(20)
      .build("kaleidoscope_world_liquor:chair");

   public static void registerEntities() {
      Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "chair"), OAK_LOG_STOOL);
   }
}
