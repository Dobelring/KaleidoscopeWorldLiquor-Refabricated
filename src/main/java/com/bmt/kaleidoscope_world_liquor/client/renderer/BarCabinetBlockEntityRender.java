package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BarCabinetBlockEntityRender implements BlockEntityRenderer<BarCabinetBlockEntity> {
   private final BlockRenderDispatcher blockRender;

   public BarCabinetBlockEntityRender(Context context) {
      this.blockRender = context.getBlockRenderDispatcher();
   }

   public void render(BarCabinetBlockEntity barCabinet, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Direction direction = (Direction)barCabinet.getBlockState().getValue(BarCabinetBlock.FACING);
      ItemStack leftStack = barCabinet.getLeftItem();
      ItemStack rightStack = barCabinet.getRightItem();
      float scale = 0.9F;
      float angle = 180.0F - direction.get2DDataValue() * 90.0F;
      if (barCabinet.isSingle()) {
         float singleAngle = 0.0F - direction.get2DDataValue() * 90.0F;
         if (!leftStack.isEmpty() && leftStack.getItem() instanceof BlockItem blockItem) {
            poseStack.pushPose();
            BlockState state = this.fixVineryBlockState(blockItem.getBlock().defaultBlockState());
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(singleAngle));
            poseStack.translate(0.0, 0.0625, 0.0);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, 0.0, -0.5);
            this.blockRender.renderSingleBlock(state, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
         }
      } else {
         if (!leftStack.isEmpty() && leftStack.getItem() instanceof BlockItem blockItem) {
            poseStack.pushPose();
            BlockState state = this.fixVineryBlockState(blockItem.getBlock().defaultBlockState());
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.translate(direction.getAxis() == net.minecraft.core.Direction.Axis.Z ? 0.25 : -0.25, 0.0625, 0.0);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, 0.0, -0.5);
            this.blockRender.renderSingleBlock(state, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
         }

         if (!rightStack.isEmpty() && rightStack.getItem() instanceof BlockItem blockItem) {
            poseStack.pushPose();
            BlockState state = this.fixVineryBlockState(blockItem.getBlock().defaultBlockState());
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.translate(direction.getAxis() == net.minecraft.core.Direction.Axis.Z ? -0.25 : 0.25, 0.0625, 0.0);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, 0.0, -0.5);
            this.blockRender.renderSingleBlock(state, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
         }
      }
   }

   private BlockState fixVineryBlockState(BlockState state) {
      ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
      if (!"vinery".equals(key.getNamespace())) {
         return state;
      } else {
         for (Property<?> property : state.getProperties()) {
            if ("fake_model".equals(property.getName())) {
               return this.setFakeModelFalse(state, property);
            }
         }

         return state;
      }
   }

   private <T extends Comparable<T>> BlockState setFakeModelFalse(BlockState state, Property<T> property) {
      return property.getValueClass() == Boolean.class ? (BlockState)state.setValue(property, (T)(Object)false) : state;
   }
}
