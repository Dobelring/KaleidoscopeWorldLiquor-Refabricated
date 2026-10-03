package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.block.entity.BarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.block.entity.BarCellarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.block.entity.WallRecordBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;

public class ModBlockEntities {
    public static final BlockEntityType<WallRecordBlockEntity> WALL_RECORD = Builder.of(
            WallRecordBlockEntity::new,
            new Block[]{ModBlocks.WALL_RECORD}
        )
        .build(null);
    public static final BlockEntityType<BarCabinetBlockEntity> BAR_CABINET_BE = Builder.of(
            BarCabinetBlockEntity::new,
            new Block[]{
                ModBlocks.OAK_BAR_CABINET,
                ModBlocks.OAK_GLASS_BAR_CABINET,
                ModBlocks.BIRCH_BAR_CABINET,
                ModBlocks.SPRUCE_BAR_CABINET,
                ModBlocks.DARK_OAK_BAR_CABINET,
                ModBlocks.CHERRY_BAR_CABINET,
                ModBlocks.JUNGLE_BAR_CABINET,
                ModBlocks.ACACIA_BAR_CABINET,
                ModBlocks.MANGROVE_BAR_CABINET,
                ModBlocks.BAMBOO_BAR_CABINET,
                ModBlocks.CRIMSON_BAR_CABINET,
                ModBlocks.WARPED_BAR_CABINET,
            }
        )
        .build(null);
    public static final BlockEntityType<BarCellarCabinetBlockEntity> BAR_CELLAR_CABINET_BE = Builder.of(
            BarCellarCabinetBlockEntity::new,
            new Block[]{
                ModBlocks.OAK_CELLAR_CABINET,
                ModBlocks.BIRCH_CELLAR_CABINET,
                ModBlocks.SPRUCE_CELLAR_CABINET,
                ModBlocks.DARK_OAK_CELLAR_CABINET,
                ModBlocks.CHERRY_CELLAR_CABINET,
                ModBlocks.JUNGLE_CELLAR_CABINET,
                ModBlocks.ACACIA_CELLAR_CABINET,
                ModBlocks.MANGROVE_CELLAR_CABINET,
                ModBlocks.BAMBOO_CELLAR_CABINET,
                ModBlocks.CRIMSON_CELLAR_CABINET,
                ModBlocks.WARPED_CELLAR_CABINET,
            }
        )
        .build(null);
    public static final BlockEntityType<FreezerBlockEntity> FREEZER_BE = Builder.of(FreezerBlockEntity::new, new Block[]{ModBlocks.FREEZER}).build(null);

    public ModBlockEntities() {
    }

    public static void registerBlockEntities() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, KaleidoscopeWorldLiquor.id("wall_record"), WALL_RECORD);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, KaleidoscopeWorldLiquor.id("oak_bar_cabinet"), BAR_CABINET_BE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, KaleidoscopeWorldLiquor.id("bar_cellar_cabinet"), BAR_CELLAR_CABINET_BE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, KaleidoscopeWorldLiquor.id("freezer"), FREEZER_BE);
    }
}
