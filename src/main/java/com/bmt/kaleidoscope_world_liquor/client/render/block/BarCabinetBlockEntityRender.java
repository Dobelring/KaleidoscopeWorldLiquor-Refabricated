package com.bmt.kaleidoscope_world_liquor.client.render.block;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.PositionType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 酒柜 BER：左/右槽各摆一瓶（isSingle 时中央摆一瓶）。
 * 照 tavern StorageBlockEntityRender 的 submitBlockModel 范式；
 * vinery 的 fake_model 属性修正保留（原版行为）。
 */
@Environment(EnvType.CLIENT)
public class BarCabinetBlockEntityRender implements BlockEntityRenderer<BarCabinetBlockEntity, BarCabinetBlockEntityRenderState> {
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blockRender;

    public BarCabinetBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.blockRender = context.blockRenderDispatcher();
    }

    @Override
    public @NotNull BarCabinetBlockEntityRenderState createRenderState() {
        return new BarCabinetBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull BarCabinetBlockEntity blockEntity, @NotNull BarCabinetBlockEntityRenderState state,
                                   float f, @NotNull Vec3 vec3, net.minecraft.client.renderer.feature.ModelFeatureRenderer.@org.jetbrains.annotations.Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, vec3, crumblingOverlay);
        state.facing = blockEntity.getBlockState().getValue(BarCabinetBlock.FACING);
        state.single = blockEntity.isSingle();
        state.left = blockEntity.getLeftItem();
        state.right = blockEntity.getRightItem();
    }

    @Override
    public void submit(BarCabinetBlockEntityRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        float scale = 0.9F;
        float angle = 180.0F - state.facing.get2DDataValue() * 90.0F;
        if (state.single) {
            float singleAngle = 0.0F - state.facing.get2DDataValue() * 90.0F;
            if (!state.left.isEmpty() && state.left.getItem() instanceof BlockItem blockItem) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(singleAngle));
                poseStack.translate(0.0, 0.0625, 0.0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
                this.submitBlock(blockItem, poseStack, submitNodeCollector, state.lightCoords);
                poseStack.popPose();
            }
        } else {
            if (!state.left.isEmpty() && state.left.getItem() instanceof BlockItem blockItem) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(state.facing.getAxis() == Direction.Axis.Z ? 0.25 : -0.25, 0.0625, 0.0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
                this.submitBlock(blockItem, poseStack, submitNodeCollector, state.lightCoords);
                poseStack.popPose();
            }
            if (!state.right.isEmpty() && state.right.getItem() instanceof BlockItem blockItem) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(state.facing.getAxis() == Direction.Axis.Z ? -0.25 : 0.25, 0.0625, 0.0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
                this.submitBlock(blockItem, poseStack, submitNodeCollector, state.lightCoords);
                poseStack.popPose();
            }
        }
    }

    private void submitBlock(BlockItem blockItem, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        BlockState state = fixVineryBlockState(blockItem.getBlock().defaultBlockState());
        BlockStateModel model = this.blockRender.getBlockModel(state);
        submitNodeCollector.submitBlockModel(
                poseStack,
                Sheets.cutoutBlockSheet(),
                model,
                1.0F, 1.0F, 1.0F,
                light,
                OverlayTexture.NO_OVERLAY,
                0
        );
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
