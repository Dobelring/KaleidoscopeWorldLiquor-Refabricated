package com.bmt.kaleidoscope_world_liquor.client.render.block;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * 酒柜渲染状态。
 */
@Environment(EnvType.CLIENT)
public class BarCabinetBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean single = false;
    public ItemStack left = ItemStack.EMPTY;
    public ItemStack right = ItemStack.EMPTY;
}
