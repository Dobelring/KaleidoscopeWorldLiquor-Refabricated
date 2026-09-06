package com.bmt.kaleidoscope_world_liquor.client.render.block;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 冰柜 BER（两段式）。液体与输出贴图平面按 1.20.1 原版几何手绘顶点
 * （facing 决定 x/z/宽/深，translucentBlockItemSheet 缓冲），浮空输入物品用 ItemStackRenderState.submit。
 */
@Environment(EnvType.CLIENT)
public class FreezerBlockEntityRender implements BlockEntityRenderer<FreezerBlockEntity, FreezerBlockEntityRenderState> {
    private final ItemModelResolver itemModelResolver;

    public FreezerBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public @NotNull FreezerBlockEntityRenderState createRenderState() {
        return new FreezerBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull FreezerBlockEntity blockEntity, @NotNull FreezerBlockEntityRenderState state,
                                   float f, @NotNull Vec3 vec3, net.minecraft.client.renderer.feature.ModelFeatureRenderer.@org.jetbrains.annotations.Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, vec3, crumblingOverlay);
        state.facing = blockEntity.getBlockState().getValue(FreezerBlock.FACING);
        state.hasFluid = blockEntity.tank.getFluidAmountMb() > 0;
        state.fluidPercent = state.hasFluid ? (float) blockEntity.tank.getFluidAmountMb() / 1000.0F : 0.0F;
        state.fluid = state.hasFluid ? blockEntity.tank.getFluidVariant().getFluid() : null;
        if (state.fluid != null) {
            var handler = net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.get(state.fluid);
            if (handler != null) {
                var fluidState = state.fluid.defaultFluidState();
                state.fluidSprite = handler.getFluidSprites(blockEntity.getLevel(), blockEntity.getBlockPos(), fluidState)[0];
                int tint = handler.getFluidColor(blockEntity.getLevel(), blockEntity.getBlockPos(), fluidState);
                state.fluidColor = 0xFF000000 | (tint & 0xFFFFFF);
            } else {
                state.fluidSprite = null;
                state.fluidColor = 0xFFFFFFFF;
            }
        } else {
            state.fluidSprite = null;
            state.fluidColor = 0xFFFFFFFF;
        }
        Level level = blockEntity.getLevel();
        for (int i = 0; i < 4; i++) {
            ItemStack stack = blockEntity.inputInventory.getStackInSlot(i);
            state.items[i] = stack;
            if (!stack.isEmpty() && level != null) {
                // updateForNonLiving 内部即 updateForTopItem(..., entity.level(), null, entity.getId())；
                // BER 无实体可传，直接走 TopItem 形式，Level 取自 BE
                this.itemModelResolver.updateForTopItem(state.itemStates[i], stack, ItemDisplayContext.FIXED, level, null, 0);
            } else {
                state.itemStates[i].clear();
            }
        }
        state.hasOutput = blockEntity.hasOutput();
        state.outputTexture = blockEntity.getOutputTexture();
        state.outputCount = blockEntity.getOutputCount();
    }

    @Override
    public void submit(FreezerBlockEntityRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        if (state.hasFluid && state.fluidPercent > 0.0F) {
            drawFluid(state, poseStack);
        }
        drawFloatingItems(state, poseStack, submitNodeCollector);
        if (state.hasOutput && state.outputTexture != null && state.outputCount > 0) {
            drawResultTexture(state, poseStack);
        }
    }

    /** 与 1.20.1 原版一致的液体平面几何：{x, z, width, depth}（北/南共用一组，东/西/默认各一组） */
    private float[] fluidDims(Direction facing) {
        return switch (facing) {
            case NORTH, SOUTH -> new float[]{0.0625F, 0.125F, 0.9375F, 0.75F};
            case EAST -> new float[]{0.125F, 0.03125F, 0.75F, 0.9375F};
            case WEST -> new float[]{0.0625F, 0.03125F, 0.75F, 0.9375F};
            default -> new float[]{0.15625F, 0.0625F, 0.75F, 0.8125F};
        };
    }

    private void drawFluid(FreezerBlockEntityRenderState state, PoseStack poseStack) {
        // sprite/color 已在 extractRenderState 用真实 level/pos 算好——原版水 handler 对 null level
        // 取群系色调会 NPE，曾中断整个 submit 导致液面完全消失
        var sprite = state.fluidSprite;
        if (sprite == null) {
            return;
        }
        int color = state.fluidColor;
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(Sheets.translucentBlockItemSheet());
        var matrix = poseStack.last().pose();
        float[] dims = fluidDims(state.facing);
        float x = dims[0], z = dims[1], width = dims[2], depth = dims[3];
        float y = 0.25F + 0.375F * Math.min(state.fluidPercent, 1.0F);
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        consumer.addVertex(matrix, x, y, z).setColor(color).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x, y, z + depth).setColor(color).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x + width, y, z + depth).setColor(color).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x + width, y, z).setColor(color).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
    }

    private void drawFloatingItems(FreezerBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        // 有液体时物品整体抬到液面之上（二维平铺物品会被液面盖住）；液面最高 0.625，
        // 基准 0.675 + 槽位 0.02 递增（封顶 ~0.745，仍在柜沿 0.75 之内）
        float fluidY = 0.25F + 0.375F * Math.min(state.fluidPercent, 1.0F);
        float baseY = state.hasFluid ? Math.max(0.55F, fluidY + 0.05F) : 0.2F;
        float[][] quadrants = {{0.25F, 0.5F, 0.25F, 0.5F}, {0.5F, 0.75F, 0.25F, 0.5F},
                {0.25F, 0.5F, 0.5F, 0.75F}, {0.5F, 0.75F, 0.5F, 0.75F}};
        for (int slot = 0; slot < 4; slot++) {
            if (state.items[slot].isEmpty()) {
                continue;
            }
            RandomSource random = RandomSource.create(state.blockPos.hashCode() + slot * 999L);
            float[] area = quadrants[slot];
            float x = area[0] + random.nextFloat() * (area[1] - area[0]);
            float z = area[2] + random.nextFloat() * (area[3] - area[2]);
            float y = baseY + random.nextFloat() * 0.01F + slot * 0.02F;
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            float scale = 0.3F + random.nextFloat() * 0.05F;
            poseStack.scale(scale, scale, scale);
            state.itemStates[slot].submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private void drawResultTexture(FreezerBlockEntityRenderState state, PoseStack poseStack) {
        var sprite = Minecraft.getInstance().getModelManager().atlasManager
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(state.outputTexture);
        int color = 0xFFFFFFFF;
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(Sheets.translucentBlockItemSheet());
        var matrix = poseStack.last().pose();
        float[] dims = fluidDims(state.facing);
        float x = dims[0], z = dims[1], width = dims[2], depth = dims[3];
        int renderCount = Math.min(state.outputCount, 3);
        float y = 0.75F - (5.0F - renderCount) * 0.08F;
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        consumer.addVertex(matrix, x, y, z).setColor(color).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x, y, z + depth).setColor(color).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x + width, y, z + depth).setColor(color).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x + width, y, z).setColor(color).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(0, 1, 0);
    }
}
