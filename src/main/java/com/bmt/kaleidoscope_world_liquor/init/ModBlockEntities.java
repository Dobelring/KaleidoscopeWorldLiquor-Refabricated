package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.block.entity.WallRecordBlockEntity;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;

public class ModBlockEntities {
   public static final BlockEntityType<FreezerBlockEntity> FREEZER_BE = Builder.of(FreezerBlockEntity::new, ModBlocks.FREEZER).build(null);
   public static final BlockEntityType<WallRecordBlockEntity> WALL_RECORD_BE = Builder.of(WallRecordBlockEntity::new, ModBlocks.WALL_RECORD).build(null);
   public static final BlockEntityType<BarCabinetBlockEntity> BAR_CABINET_BE = Builder.of(
         BarCabinetBlockEntity::new,
         ModBlocks.OAK_BAR_CABINET,
         ModBlocks.OAK_GLASS_BAR_CABINET,
         ModBlocks.BIRCH_BAR_CABINET,
         ModBlocks.SPRUCE_BAR_CABINET,
         ModBlocks.DARK_OAK_BAR_CABINET,
         ModBlocks.CHERRY_BAR_CABINET
      )
      .build(null);
   public static final BlockEntityType<BarCellarCabinetBlockEntity> BAR_CELLAR_CABINET_BE = Builder.of(
         BarCellarCabinetBlockEntity::new,
         ModBlocks.OAK_CELLAR_CABINET,
         ModBlocks.BIRCH_CELLAR_CABINET,
         ModBlocks.SPRUCE_CELLAR_CABINET,
         ModBlocks.DARK_OAK_CELLAR_CABINET,
         ModBlocks.CHERRY_CELLAR_CABINET
      )
      .build(null);

   public static void registerBlockEntities() {
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "freezer"), FREEZER_BE);
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "wall_record"), WALL_RECORD_BE);
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "oak_bar_cabinet"), BAR_CABINET_BE);
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, "bar_cellar_cabinet"), BAR_CELLAR_CABINET_BE);
   }
}
