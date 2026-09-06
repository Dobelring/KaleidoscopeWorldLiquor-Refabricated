package com.bmt.kaleidoscope_world_liquor.client.render.block;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 酒窖柜渲染状态（3x3 网格物品快照）。
 */
@Environment(EnvType.CLIENT)
public class BarCellarCabinetBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public List<ItemStack> items = new ArrayList<>();
}
