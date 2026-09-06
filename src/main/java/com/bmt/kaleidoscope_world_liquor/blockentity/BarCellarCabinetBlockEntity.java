package com.bmt.kaleidoscope_world_liquor.blockentity;

import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BarCellarCabinetBlockEntity extends StorageBlockEntity {
   public BarCellarCabinetBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.BAR_CELLAR_CABINET_BE, pos, state, 9);
   }
}
