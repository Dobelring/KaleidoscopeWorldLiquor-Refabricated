package com.bmt.kaleidoscope_world_liquor.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

/**
 * 酒柜渲染状态（26.1.2：模型快照走 BlockModelRenderState）。
 */
@Environment(EnvType.CLIENT)
public class BarCabinetBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean single = false;
    public final BlockModelRenderState leftModel = new BlockModelRenderState();
    public final BlockModelRenderState rightModel = new BlockModelRenderState();
}
