package com.bmt.kaleidoscope_world_liquor.client.render.block;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * 酒柜 BER：左/右槽各摆一瓶（isSingle 时中央摆一瓶）。
 * 26.1.2 范式：extract 期 BlockModelResolver.update 填 BlockModelRenderState，
 * submit 期 renderState.submit（BlockRenderDispatcher 已删除）。
 * vinery 的 fake_model 属性修正保留（原版行为）。
 */
@Environment(EnvType.CLIENT)
public class BarCabinetBlockEntityRender implements BlockEntityRenderer<BarCabinetBlockEntity, BarCabinetBlockEntityRenderState> {
    protected static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver blockModelResolver;

    public BarCabinetBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public @NotNull BarCabinetBlockEntityRenderState createRenderState() {
        return new BarCabinetBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull BarCabinetBlockEntity blockEntity, @NotNull BarCabinetBlockEntityRenderState state,
                                   float f, @NotNull Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, vec3, crumblingOverlay);
        state.facing = blockEntity.getBlockState().getValue(BarCabinetBlock.FACING);
        state.single = blockEntity.isSingle();
        state.leftModel.clear();
        state.rightModel.clear();
        ItemStack left = blockEntity.getLeftItem();
        ItemStack right = blockEntity.getRightItem();
        if (!left.isEmpty() && left.getItem() instanceof BlockItem blockItem) {
            this.blockModelResolver.update(state.leftModel, fixVineryBlockState(blockItem.getBlock().defaultBlockState()), BLOCK_DISPLAY_CONTEXT);
        }
        if (!right.isEmpty() && right.getItem() instanceof BlockItem blockItem) {
            this.blockModelResolver.update(state.rightModel, fixVineryBlockState(blockItem.getBlock().defaultBlockState()), BLOCK_DISPLAY_CONTEXT);
        }
    }

    @Override
    public void submit(BarCabinetBlockEntityRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        float scale = 0.9F;
        float angle = 180.0F - state.facing.get2DDataValue() * 90.0F;
        if (state.single) {
            float singleAngle = 0.0F - state.facing.get2DDataValue() * 90.0F;
            if (!state.leftModel.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(singleAngle));
                poseStack.translate(0.0, 0.0625, 0.0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
                state.leftModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        } else {
            if (!state.leftModel.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(state.facing.getAxis() == Direction.Axis.Z ? 0.25 : -0.25, 0.0625, 0.0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
                state.leftModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
            if (!state.rightModel.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(state.facing.getAxis() == Direction.Axis.Z ? -0.25 : 0.25, 0.0625, 0.0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
                state.rightModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }

    private BlockState fixVineryBlockState(BlockState state) {
        // vinery 的 fake_model 属性：置 false 避免双模型
        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
            if ("fake_model".equals(property.getName())) {
                if (property.getValueClass() == Boolean.class) {
                    @SuppressWarnings("unchecked")
                    net.minecraft.world.level.block.state.properties.Property<Boolean> boolProp = (net.minecraft.world.level.block.state.properties.Property<Boolean>) property;
                    return state.setValue(boolProp, false);
                }
            }
        }
        return state;
    }
}
