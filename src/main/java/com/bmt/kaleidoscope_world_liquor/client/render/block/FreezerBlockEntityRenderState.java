package com.bmt.kaleidoscope_world_liquor.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * 冰柜渲染状态（两段式缓存）。
 */
@Environment(EnvType.CLIENT)
public class FreezerBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean hasFluid = false;
    public float fluidPercent = 0.0F;
    @org.jspecify.annotations.Nullable
    public TextureAtlasSprite fluidSprite = null;
    public int fluidColor = 0xFFFFFFFF;
    public final ItemStack[] items = new ItemStack[4];
    public final ItemStackRenderState[] itemStates = new ItemStackRenderState[4];
    public boolean hasOutput = false;
    @org.jspecify.annotations.Nullable
    public Identifier outputTexture = null;
    public int outputCount = 0;

    public FreezerBlockEntityRenderState() {
        for (int i = 0; i < 4; i++) {
            items[i] = ItemStack.EMPTY;
            itemStates[i] = new ItemStackRenderState();
        }
    }
}
