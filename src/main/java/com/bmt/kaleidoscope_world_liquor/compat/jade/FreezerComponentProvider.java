package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Jade：冰柜信息（冷冻剩余时间）。照 tavern/cookery 1.21.11 的 jade 插件范式。
 */
public class FreezerComponentProvider implements IBlockComponentProvider {
    public static final FreezerComponentProvider INSTANCE = new FreezerComponentProvider();
    public static final Identifier UID = Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "freezer");

    @Override
    public void appendTooltip(@NotNull ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof FreezerBlockEntity be
                && accessor.getBlockState().getValue(FreezerBlock.WORKING)) {
            int remaining = Math.max(0, (be.getMaxProgress() - be.getProgress()) / 20);
            tooltip.add(Component.translatable("jade.kaleidoscope_world_liquor.freezer.remaining_time", remaining));
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
