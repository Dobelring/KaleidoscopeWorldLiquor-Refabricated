package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.blockentity.WallRecordBlockEntity;
import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private ModBlockEntities() {
    }

    public static BlockEntityType<FreezerBlockEntity> FREEZER_BE;
    public static BlockEntityType<WallRecordBlockEntity> WALL_RECORD_BE;
    public static BlockEntityType<BarCabinetBlockEntity> BAR_CABINET_BE;
    public static BlockEntityType<BarCellarCabinetBlockEntity> BAR_CELLAR_CABINET_BE;

    public static void register() {
        FREEZER_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PortHelper.id("freezer"),
                FabricBlockEntityTypeBuilder.create(FreezerBlockEntity::new, ModBlocks.FREEZER).build());
        WALL_RECORD_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PortHelper.id("wall_record"),
                FabricBlockEntityTypeBuilder.create(WallRecordBlockEntity::new, ModBlocks.WALL_RECORD).build());
        BAR_CABINET_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PortHelper.id("oak_bar_cabinet"),
                FabricBlockEntityTypeBuilder.create(BarCabinetBlockEntity::new,
                        ModBlocks.OAK_BAR_CABINET, ModBlocks.OAK_GLASS_BAR_CABINET, ModBlocks.BIRCH_BAR_CABINET,
                        ModBlocks.SPRUCE_BAR_CABINET, ModBlocks.DARK_OAK_BAR_CABINET, ModBlocks.CHERRY_BAR_CABINET).build());
        BAR_CELLAR_CABINET_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PortHelper.id("bar_cellar_cabinet"),
                FabricBlockEntityTypeBuilder.create(BarCellarCabinetBlockEntity::new,
                        ModBlocks.OAK_CELLAR_CABINET, ModBlocks.BIRCH_CELLAR_CABINET,
                        ModBlocks.SPRUCE_CELLAR_CABINET, ModBlocks.DARK_OAK_CELLAR_CABINET, ModBlocks.CHERRY_CELLAR_CABINET).build());
    }
}
