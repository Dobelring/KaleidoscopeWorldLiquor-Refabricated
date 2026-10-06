package com.bmt.kaleidoscope_world_liquor.client.render.block;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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
        state.maxOutputCount = blockEntity.getMaxOutputCount();
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

    /**
     * 官方 1.1.11「重构冰柜渲染」：漂浮物改 2×2 网格随朝向排布（NORTH 180/EAST 90/WEST 270）、
     * 方块物品 0.4 缩放、其余 0.25；无液 0.28，有液方块 0.55/非方块 0.65。
     * 保留本分支规格守卫：有液时物品抬到液面 +0.05 之上（二维平铺物品会被半透明液面盖住——
     * 液体桶正是柜满后才进槽，故"看不见"）。
     */
    private void drawFloatingItems(FreezerBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        boolean hasFluid = state.hasFluid;
        float liquidTop = hasFluid ? 0.25F + 0.375F * Math.min(state.fluidPercent, 1.0F) : 0.0F;
        for (int slot = 0; slot < 4; slot++) {
            if (state.items[slot].isEmpty() || state.itemStates[slot].isEmpty()) {
                continue;
            }
            boolean isBlockItem = state.items[slot].getItem() instanceof net.minecraft.world.item.BlockItem;
            float itemY;
            if (hasFluid) {
                itemY = isBlockItem ? 0.55F : 0.65F;
                // 本分支液面守卫（保留原注释与 +0.05 规格）：物品不得沉到液面之下
                itemY = Math.max(itemY, liquidTop + 0.05F);
            } else {
                itemY = 0.28F;
            }

            RandomSource random = RandomSource.create(state.blockPos.hashCode() + slot * 999L);
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            float yaw = switch (state.facing) {
                case NORTH -> 180.0F;
                case EAST -> 90.0F;
                case WEST -> 270.0F;
                default -> 0.0F;
            };
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.translate(-0.5F, 0.0F, -0.5F);
            float regionMinX = 0.15625F;
            float regionMinZ = 0.1875F;
            float cellW = 0.34375F;
            float cellD = 0.28125F;
            int col = slot % 2;
            int row = slot / 2;
            float cellMinX = regionMinX + col * cellW;
            float cellMinZ = regionMinZ + row * cellD;
            float pad = 0.075F;
            float x = cellMinX + pad + random.nextFloat() * (cellW - 2.0F * pad);
            float z = cellMinZ + pad + random.nextFloat() * (cellD - 2.0F * pad);
            poseStack.translate(x, itemY, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            float scale = isBlockItem ? 0.4F : 0.25F;
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
            // 官方 1.1.11：成品贴图按 count/maxOutputCount 比例从底部 0.3125 升到顶 0.625
            float topY = 0.625F;
            float bottomY = 0.3125F;
            float dropRange = topY - bottomY;
            int maxCount = state.maxOutputCount;
            float ratio = maxCount > 0 ? Math.min(1.0F, (float) state.outputCount / maxCount) : 1.0F;
            float y = bottomY + dropRange * ratio;
            consumer.addVertex(matrix, x, y, z).setColor(color).setUv(u1, v0).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x, y, z + depth).setColor(color).setUv(u1, v1).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x + width, y, z + depth).setColor(color).setUv(u0, v1).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, x + width, y, z).setColor(color).setUv(u0, v0).setOverlay(0).setLight(state.lightCoords).setNormal(pose, 0, 1, 0);
        });
    }
}
