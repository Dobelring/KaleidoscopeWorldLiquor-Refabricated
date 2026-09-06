package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.StorageBlockEntityRender;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BarCellarCabinetBlockEntityRender extends StorageBlockEntityRender<BarCellarCabinetBlockEntity> {
   public BarCellarCabinetBlockEntityRender(Context context) {
      super(context);
   }

   public void render(BarCellarCabinetBlockEntity rack, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Direction direction = (Direction)rack.getBlockState().getValue(BarCellarCabinetBlock.FACING);
      ItemStackHandler items = rack.getItems();
      poseStack.pushPose();
      this.applyFacingRotation(direction, poseStack);

      for (int i = 0; i < items.getSlots(); i++) {
         ItemStack stack = items.getStackInSlot(i);
         if (!stack.isEmpty()) {
            int row = i / 3;
            int column = i % 3;
            double x = 0.825 - column * 0.325;
            double y = 0.78 - row * 0.29;
            this.renderStack(stack, poseStack, buffer, packedLight, packedOverlay, x, y, 0.875, 1.0F, 0.0, -90.0);
         }
      }

      poseStack.popPose();
   }

   protected void renderStack(
      ItemStack stack,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay,
      double x,
      double y,
      double z,
      float scale,
      double yRot,
      double xRot
   ) {
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
         poseStack.pushPose();
         BlockState var19 = this.fixVineryBlockState(blockItem.getBlock().defaultBlockState());
         poseStack.translate(x, y, z);
         poseStack.mulPose(Axis.YP.rotationDegrees((float)yRot));
         poseStack.mulPose(Axis.XP.rotationDegrees((float)xRot));
         poseStack.scale(scale, scale, scale);
         poseStack.translate(-0.5, 0.0, -0.5);
         this.blockRender.renderSingleBlock(var19, poseStack, buffer, packedLight, packedOverlay);
         poseStack.popPose();
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
