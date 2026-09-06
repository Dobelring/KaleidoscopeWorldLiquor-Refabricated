package com.bmt.kaleidoscope_world_liquor.client.render.block;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 酒窖柜 BER：3x3 网格展示（照 tavern StorageBlockEntityRender 的
 * submitBlockModel 范式 + 朝向旋转；坐标布局与原版一致）。
 */
@Environment(EnvType.CLIENT)
public class BarCellarCabinetBlockEntityRender implements BlockEntityRenderer<BarCellarCabinetBlockEntity, BarCellarCabinetBlockEntityRenderState> {
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blockRender;

    public BarCellarCabinetBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.blockRender = context.blockRenderDispatcher();
    }

    @Override
    public @NotNull BarCellarCabinetBlockEntityRenderState createRenderState() {
        return new BarCellarCabinetBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull BarCellarCabinetBlockEntity blockEntity, @NotNull BarCellarCabinetBlockEntityRenderState state,
                                   float f, @NotNull Vec3 vec3, net.minecraft.client.renderer.feature.ModelFeatureRenderer.@org.jetbrains.annotations.Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, vec3, crumblingOverlay);
        state.facing = blockEntity.getBlockState().getValue(BarCellarCabinetBlock.FACING);
        state.items.clear();
        for (int i = 0; i < blockEntity.getItems().getSlots(); i++) {
            state.items.add(blockEntity.getItems().getStackInSlot(i));
        }
    }

    @Override
    public void submit(BarCellarCabinetBlockEntityRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        applyFacingRotation(state.facing, poseStack);
        for (int i = 0; i < state.items.size(); i++) {
            ItemStack stack = state.items.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            int row = i / 3;
            int column = i % 3;
            double x = 0.825 - column * 0.325;
            double y = 0.78 - row * 0.29;
            renderStack(stack, poseStack, submitNodeCollector, state.lightCoords, x, y, 0.875, 1.0F, 0.0, -90.0);
        }
        poseStack.popPose();
    }

    protected void applyFacingRotation(Direction direction, PoseStack poseStack) {
        float angle = 180 - direction.get2DDataValue() * 90f;
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, 0, -0.5);
    }

    protected void renderStack(ItemStack stack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                               double x, double y, double z, float scale, double yRot, double xRot) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        poseStack.pushPose();
        BlockStateModel model = this.blockRender.getBlockModel(blockItem.getBlock().defaultBlockState());
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) xRot));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, 0, -0.5);
        submitNodeCollector.submitBlockModel(
                poseStack,
                RenderTypes.entityCutoutNoCullZOffset(TextureAtlas.LOCATION_BLOCKS),
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();
    }
}
