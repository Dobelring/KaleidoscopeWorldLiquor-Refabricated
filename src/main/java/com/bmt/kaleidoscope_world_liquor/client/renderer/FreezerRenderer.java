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

   /** 冰柜内壁（液体/产物贴图）的绘制范围，按朝向取。 */
   private static PanelBounds panelBounds(Direction facing) {
      return switch (facing) {
         case NORTH, SOUTH -> new PanelBounds(0.0625F, 0.125F, 0.9375F, 0.75F);
         case EAST -> new PanelBounds(0.125F, 0.03125F, 0.75F, 0.9375F);
         case WEST -> new PanelBounds(0.0625F, 0.03125F, 0.75F, 0.9375F);
         default -> new PanelBounds(0.15625F, 0.0625F, 0.75F, 0.8125F);
      };
   }

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
         PanelBounds bounds = panelBounds(facing);
         float x = bounds.x();
         float z = bounds.z();
         float width = bounds.width();
         float depth = bounds.depth();

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

   // 官方 1.1.11「重构冰柜渲染」：漂浮物改 2×2 网格随朝向排布、方块物品 0.4 缩放。
   // 保留本分支规格守卫：有液时物品抬到液面 +0.02 之上。
   private void drawFloatingItems(FreezerBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Direction facing) {
      boolean hasFluid = !be.tank.getFluid().isEmpty();
      float liquidTop = hasFluid ? 0.25F + 0.375F * ((float)be.tank.getFluidAmount() / (float)be.tank.getCapacity()) : 0.0F;

      for (int slot = 0; slot < 4; slot++) {
         ItemStack stack = be.inputInventory.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            boolean isBlockItem = stack.getItem() instanceof net.minecraft.world.item.BlockItem;
            float itemY;
            if (hasFluid) {
               itemY = isBlockItem ? 0.55F : 0.65F;
               itemY = Math.max(itemY, liquidTop + 0.02F);
            } else {
               itemY = 0.28F;
            }

            Random random = new Random(be.getBlockPos().hashCode() + slot * 999L);
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            float yaw = switch (facing) {
               case NORTH -> 180.0F;
               default -> 0.0F;
               case EAST -> 90.0F;
               case WEST -> 270.0F;
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
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, be.getLevel(), 0);
            poseStack.popPose();
         }
      }
   }

   private void drawResultTexture(FreezerBlockEntity be, PoseStack poseStack, MultiBufferSource source, int light, Direction facing) {
      ResourceLocation textureLoc = be.getOutputTexture();
      int count = be.getOutputCount();
      if (textureLoc != null && count > 0) {
         TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(textureLoc);
         VertexConsumer consumer = source.getBuffer(RenderType.translucent());
         Matrix4f matrix = poseStack.last().pose();
         PanelBounds bounds = panelBounds(facing);
         float x = bounds.x();
         float z = bounds.z();
         float width = bounds.width();
         float depth = bounds.depth();

         // 官方 1.1.11：成品贴图按 count/maxOutputCount 比例从底部升到顶
         float topY = 0.625F;
         float bottomY = 0.3125F;
         float dropRange = topY - bottomY;
         int maxCount = be.getMaxOutputCount();
         float ratio = maxCount > 0 ? Math.min(1.0F, (float)count / maxCount) : 1.0F;
         float y = bottomY + dropRange * ratio;
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

   private record PanelBounds(float x, float z, float width, float depth) {
   }
}
