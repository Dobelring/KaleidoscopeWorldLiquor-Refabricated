package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class FreezerRenderer implements BlockEntityRenderer<FreezerBlockEntity> {
   private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

   public FreezerRenderer(Context context) {
   }

   public void render(
      FreezerBlockEntity be, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay
   ) {
      Level level = be.getLevel();
      if (level != null) {
         Direction facing = (Direction)be.getBlockState().getValue(FreezerBlock.FACING);
         if (!be.tank.getFluid().isEmpty()) {
            this.drawFluid(be, poseStack, buffer, packedLight, facing);
         }

         this.drawFloatingItems(be, poseStack, buffer, packedLight, facing);

         if (be.hasOutput() && be.getOutputTexture() != null) {
            this.drawResultTexture(be, poseStack, buffer, packedLight, facing);
         }
      }
   }

   private void drawFluid(FreezerBlockEntity be, PoseStack poseStack, MultiBufferSource source, int light, Direction facing) {
      try {
         Fluid fluid = be.tank.getFluid().getFluid();
         if (fluid == null || fluid == Fluids.EMPTY) {
            return;
         }

         // 查询流体自己的渲染 handler（果汁/岩浆/牛奶/水各有对应贴图与染色），无 handler 时退回水
         TextureAtlasSprite sprite;
         int color;
         FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
         if (handler != null) {
            FluidState fluidState = fluid.defaultFluidState();
            try {
               sprite = handler.getFluidSprites(null, null, fluidState)[0];
               color = handler.getFluidColor(null, null, fluidState);
            } catch (Exception var32) {
               sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse("minecraft:block/water_still"));
               color = 0x3F76E4;
            }
         } else {
            sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse("minecraft:block/water_still"));
            color = 0x3F76E4;
         }

         if (color == 0) {
            color = -1;
         }

         float y = 0.25F;
         float maxHeight = 0.375F;
         float x;
         float z;
         float width;
         float depth;
         switch (facing) {
            case NORTH:
            case SOUTH:
               x = 0.0625F;
               z = 0.125F;
               width = 0.9375F;
               depth = 0.75F;
               break;
            case EAST:
               x = 0.125F;
               z = 0.03125F;
               width = 0.75F;
               depth = 0.9375F;
               break;
            case WEST:
               x = 0.0625F;
               z = 0.03125F;
               width = 0.75F;
               depth = 0.9375F;
               break;
            default:
               x = 0.15625F;
               z = 0.0625F;
               width = 0.75F;
               depth = 0.8125F;
         }

         float height = maxHeight * ((float)be.tank.getFluidAmount() / be.tank.getCapacity());

         float r = (color >> 16 & 0xFF) / 255.0F;
         float g = (color >> 8 & 0xFF) / 255.0F;
         float b = (color & 0xFF) / 255.0F;
         VertexConsumer consumer = source.getBuffer(RenderType.translucent());
         Matrix4f matrix = poseStack.last().pose();
         float minU = sprite.getU0();
         float maxU = sprite.getU1();
         float minV = sprite.getV0();
         float maxV = sprite.getV1();
         consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, 1.0F).setUv(maxU, minV).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
         consumer.addVertex(matrix, x, y + height, z + depth).setColor(r, g, b, 1.0F).setUv(minU, minV).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
         consumer.addVertex(matrix, x + width, y + height, z + depth).setColor(r, g, b, 1.0F).setUv(minU, maxV).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
         consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, 1.0F).setUv(maxU, maxV).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
      } catch (Exception var27) {
      }
   }

   private void drawFloatingItems(FreezerBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Direction facing) {
      float baseY;
      if (be.tank.getFluid().isEmpty()) {
         baseY = 0.2F;
      } else {
         // 物品须浮在液面之上：柜满时液面 0.625 高于固定基准 0.55，物品会被半透明液面盖住看不见（液体桶正是柜满后才进槽，故"看不见"）
         float fluidSurface = 0.25F + 0.375F * ((float)be.tank.getFluidAmount() / (float)be.tank.getCapacity());
         baseY = Math.max(0.55F, fluidSurface + 0.02F);
      }

      // 整体绕方块中心水平旋转 90°（用户偏好布局，对原版象限布局整体旋转）
      poseStack.pushPose();
      poseStack.translate(0.5F, 0.0F, 0.5F);
      poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      poseStack.translate(-0.5F, 0.0F, -0.5F);

      float[][] quadrants = new float[][]{{0.25F, 0.5F, 0.25F, 0.5F}, {0.5F, 0.75F, 0.25F, 0.5F}, {0.25F, 0.5F, 0.5F, 0.75F}, {0.5F, 0.75F, 0.5F, 0.75F}};

      for (int slot = 0; slot < 4; slot++) {
         ItemStack stack = be.inputInventory.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            Random random = new Random(be.getBlockPos().hashCode() + slot * 999);
            poseStack.pushPose();
            float[] area = quadrants[slot];
            float minX = area[0];
            float maxX = area[1];
            float minZ = area[2];
            float maxZ = area[3];
            float x = minX + random.nextFloat() * (maxX - minX);
            float z = minZ + random.nextFloat() * (maxZ - minZ);
            float y = baseY + random.nextFloat() * 0.01F + slot * 0.03F;
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            float scale = 0.3F + random.nextFloat() * 0.05F;
            poseStack.scale(scale, scale, scale);
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, be.getLevel(), 0);
            poseStack.popPose();
         }
      }

      poseStack.popPose();
   }

   private void drawResultTexture(FreezerBlockEntity be, PoseStack poseStack, MultiBufferSource source, int light, Direction facing) {
      ResourceLocation textureLoc = be.getOutputTexture();
      int count = be.getOutputCount();
      if (textureLoc != null && count > 0) {
         TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(textureLoc);
         VertexConsumer consumer = source.getBuffer(RenderType.translucent());
         Matrix4f matrix = poseStack.last().pose();
         float x;
         float z;
         float width;
         float depth;
         switch (facing) {
            case NORTH:
            case SOUTH:
               x = 0.0625F;
               z = 0.125F;
               width = 0.9375F;
               depth = 0.75F;
               break;
            case EAST:
               x = 0.125F;
               z = 0.03125F;
               width = 0.75F;
               depth = 0.9375F;
               break;
            case WEST:
               x = 0.0625F;
               z = 0.03125F;
               width = 0.75F;
               depth = 0.9375F;
               break;
            default:
               x = 0.15625F;
               z = 0.0625F;
               width = 0.75F;
               depth = 0.8125F;
         }

         float baseY = 0.75F;
         float sinkOffset = (5 - count) * 0.08F;
         float y = baseY - sinkOffset;
         float u0 = sprite.getU0();
         float u1 = sprite.getU1();
         float v0 = sprite.getV0();
         float v1 = sprite.getV1();
         consumer.addVertex(matrix, x, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v0).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
         consumer.addVertex(matrix, x, y, z + depth).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v1).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
         consumer.addVertex(matrix, x + width, y, z + depth).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v1).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
         consumer.addVertex(matrix, x + width, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v0).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
      }
   }

   public boolean shouldRenderOffScreen(@NotNull FreezerBlockEntity be) {
      return true;
   }
}
