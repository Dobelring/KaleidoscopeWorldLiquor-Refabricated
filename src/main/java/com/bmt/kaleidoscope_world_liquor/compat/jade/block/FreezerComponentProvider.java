package com.bmt.kaleidoscope_world_liquor.compat.jade.block;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.compat.jade.ModPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FreezerComponentProvider implements IBlockComponentProvider {
   INSTANCE;

   public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig pluginConfig) {
      if ((Boolean)accessor.getBlockState().getValue(FreezerBlock.WORKING)) {
         if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
            int remainingTicks = Math.max(0, be.getMaxProgress() - be.getProgress());
            MutableComponent timeText = Component.literal(StringUtil.formatTickDuration(remainingTicks, 20.0F));
            tooltip.add(Component.translatable("jade.kaleidoscope_world_liquor.freezer.remaining_time", new Object[]{timeText}));
         }
      }
   }

   public ResourceLocation getUid() {
      return ModPlugin.FREEZER;
   }
}
