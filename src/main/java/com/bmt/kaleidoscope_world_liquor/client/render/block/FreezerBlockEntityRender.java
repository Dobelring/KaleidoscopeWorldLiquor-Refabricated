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
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * 冰柜 BER（两段式）。液体与输出贴图平面按 1.20.1 原版几何经
 * submitCustomGeometry 手绘顶点（26.1.2 BER 内不再直接拿 bufferSource），
 * 浮空输入物品用 ItemStackRenderState.submit。
 * 贴图/颜色：液体取 ModelManager.getFluidStateModelSet()（26.1.2 流体模型烘焙集），
 * 输出贴图经 atlasManager（access widener）按 SpriteId 查。
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
                                   float f, @NotNull Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, vec3, crumblingOverlay);
        state.facing = blockEntity.getBlockState().getValue(FreezerBlock.FACING);
        state.hasFluid = blockEntity.tank.getFluidAmountMb() > 0;
        state.fluidPercent = state.hasFluid ? (float) blockEntity.tank.getFluidAmountMb() / 1000.0F : 0.0F;
        Fluid fluid = state.hasFluid ? blockEntity.tank.getFluidVariant().getFluid() : null;
        state.fluidSprite = null;
        state.fluidColor = 0xFFFFFFFF;
        if (fluid != null) {
            // 26.1.2：贴图统一在 FluidStateModelSet（烘焙期已自动缝合进图集）；
            // 颜色照 tavern RenderUtils.getFluidColor：水特判原版纯水色，其余走 Transfer API handler
            FluidStateModelSet modelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
            var model = modelSet.get(fluid.defaultFluidState());
            if (model != null && model.stillMaterial() != null) {
                state.fluidSprite = model.stillMaterial().sprite();
                Level level = blockEntity.getLevel();
                int color;
                if (fluid == net.minecraft.world.level.material.Fluids.WATER
                        || fluid == net.minecraft.world.level.material.Fluids.FLOWING_WATER) {
                    color = -12618012;
                } else if (level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel) {
                    var handler = net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering.getHandler(fluid);
                    color = handler != null
                            ? handler.getColor(net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant.of(fluid), clientLevel, blockEntity.getBlockPos())
                            : 0xFFFFFFFF;
                } else {
                    color = 0xFFFFFFFF;
                }
                // ARGB 补 alpha（tavern ensureAlpha 同款）
                state.fluidColor = (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
            }
        }
        Level level = blockEntity.getLevel();
        for (int i = 0; i < 4; i++) {
            ItemStack stack = blockEntity.inputInventory.getStackInSlot(i);
            state.items[i] = stack;
            if (!stack.isEmpty() && level != null) {
                // BER 无实体可传，走 TopItem 形式，Level 取自 BE
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
        if (state.hasFluid && state.fluidPercent > 0.0F && state.fluidSprite != null) {
            drawFluid(state, poseStack, submitNodeCollector);
        }
        drawFloatingItems(state, poseStack, submitNodeCollector);
        if (state.hasOutput && state.outputTexture != null && state.outputCount > 0) {
            drawResultTexture(state, poseStack, submitNodeCollector);
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

    private void drawFluid(FreezerBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        var sprite = state.fluidSprite;
        if (sprite == null) {
            return;
        }
        int color = state.fluidColor;
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        submitNodeCollector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(), (pose, consumer) -> {
            var matrix = pose.pose();
            float[] dims = fluidDims(state.facing);
            float x = dims[0], z = dims[1], width = dims[2], depth = dims[3];
            float y = 0.25F + 0.375F * Math.min(state.fluidPercent, 1.0F);
            consumer.addVertex(matrix, x, y, z).setColor(color).setUv(u1, v0).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x, y, z + depth).setColor(color).setUv(u0, v0).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x + width, y, z + depth).setColor(color).setUv(u0, v1).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x + width, y, z).setColor(color).setUv(u1, v1).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
        });
    }

    private void drawFloatingItems(FreezerBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        // 有液体时物品整体抬到液面之上（二维平铺物品会被液面盖住）；液面最高 0.625，
        // 基准 0.675 + 槽位 0.02 递增（封顶 ~0.745，仍在柜沿 0.75 之内）
        float fluidY = 0.25F + 0.375F * Math.min(state.fluidPercent, 1.0F);
        float baseY = state.hasFluid ? Math.max(0.55F, fluidY + 0.05F) : 0.2F;
        float[][] quadrants = {{0.25F, 0.5F, 0.25F, 0.5F}, {0.5F, 0.75F, 0.25F, 0.5F},
                {0.25F, 0.5F, 0.5F, 0.75F}, {0.5F, 0.75F, 0.5F, 0.75F}};
        for (int slot = 0; slot < 4; slot++) {
            if (state.items[slot].isEmpty() || state.itemStates[slot].isEmpty()) {
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

    private void drawResultTexture(FreezerBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        // 输出贴图 id（ns:path）→ 图集 sprite（26.1.2：AtlasManager 在 model/sprite 包，access widener 开放）
        // AtlasManager 以图集贴图路径（textures/atlas/blocks.png）为键——AtlasIds.BLOCKS 是资源注册 id（minecraft:blocks），传入即崩
        SpriteId spriteId = new SpriteId(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, state.outputTexture);
        var sprite = Minecraft.getInstance().getModelManager().atlasManager.get(spriteId);
        if (sprite == null) {
            return;
        }
        int color = 0xFFFFFFFF;
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        submitNodeCollector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(), (pose, consumer) -> {
            var matrix = pose.pose();
            float[] dims = fluidDims(state.facing);
            float x = dims[0], z = dims[1], width = dims[2], depth = dims[3];
            int renderCount = Math.min(state.outputCount, 3);
            float y = 0.75F - (5.0F - renderCount) * 0.08F;
            consumer.addVertex(matrix, x, y, z).setColor(color).setUv(u1, v0).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x, y, z + depth).setColor(color).setUv(u1, v1).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x + width, y, z + depth).setColor(color).setUv(u0, v1).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x + width, y, z).setColor(color).setUv(u0, v0).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
        });
    }
}
