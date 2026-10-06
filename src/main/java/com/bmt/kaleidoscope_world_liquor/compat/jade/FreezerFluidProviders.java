package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

public final class FreezerFluidProviders {
   // Fabric：官方从 FreezerRenderer.MILK_STILL_TEXTURE 取贴图；自有奶流体直接用本模组贴图
   private static final ResourceLocation MILK_STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "block/milk_still");
   private FreezerFluidProviders() {
   }

   private static boolean isMilk(BlockAccessor accessor) {
      if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
         return !be.tank.isEmpty() && be.tank.getFluid().getFluid() == com.bmt.kaleidoscope_world_liquor.init.ModFluids.MILK_STILL;
      } else {
         return false;
      }
   }

   private static boolean isStaleMilkData(BlockAccessor accessor) {
      if (!(accessor.getBlockEntity() instanceof FreezerBlockEntity be)) {
         return false;
      } else if (!be.tank.isEmpty()) {
         return false;
      } else {
         CompoundTag data = accessor.getServerData();
         if (!data.contains("JadeFluidStorage", 9)) {
            return false;
         } else {
            String milkId = BuiltInRegistries.FLUID.getKey(com.bmt.kaleidoscope_world_liquor.init.ModFluids.MILK_STILL).toString();

            for (Tag groupTag : data.getList("JadeFluidStorage", 10)) {
               for (Tag viewTag : ((CompoundTag)groupTag).getList("Views", 10)) {
                  CompoundTag view = (CompoundTag)viewTag;
                  if (view.contains("fluid", 10)) {
                     CompoundTag fluid = view.getCompound("fluid");
                     if (fluid.getLong("amount") > 0L && milkId.equals(fluid.getString("type"))) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   public static final class CustomFluidElement extends Element {
      private static final Vec2 DEFAULT_SIZE = new Vec2(16.0F, 16.0F);
      private static final int TEX_WIDTH = 16;

      public Vec2 getSize() {
         return DEFAULT_SIZE;
      }

      public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
         Vec2 size = this.getCachedSize();
         float width = size.x;
         float height = size.y;
         if (!(width <= 0.0F) && !(height <= 0.0F)) {
            TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance()
               .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
               .apply(MILK_STILL_TEXTURE);
            if (sprite != null) {
               drawTiledSprite(guiGraphics, x, y, width, height, -1, height, sprite);
            }
         }
      }

      public String getMessage() {
         return "";
      }

      private static void drawTiledSprite(
         GuiGraphics guiGraphics,
         float xPosition,
         float yPosition,
         float tiledWidth,
         float tiledHeight,
         int color,
         float scaledAmount,
         TextureAtlasSprite sprite
      ) {
         RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
         Matrix4f matrix = guiGraphics.pose().last().pose();
         setGLColorFromInt(color);
         RenderSystem.enableBlend();
         int xTileCount = (int)(tiledWidth / 16.0F);
         float xRemainder = tiledWidth - xTileCount * 16;
         int yTileCount = (int)(scaledAmount / 16.0F);
         float yRemainder = scaledAmount - yTileCount * 16;
         float yStart = yPosition + tiledHeight;

         for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
               float width = xTile == xTileCount ? xRemainder : 16.0F;
               float height = yTile == yTileCount ? yRemainder : 16.0F;
               float x = xPosition + xTile * 16;
               float y = yStart - (yTile + 1) * 16;
               if (width > 0.0F && height > 0.0F) {
                  float maskTop = 16.0F - height;
                  float maskRight = 16.0F - width;
                  drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight);
               }
            }
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
      }

      private static void drawTextureWithMasking(Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, float maskTop, float maskRight) {
         float uMin = textureSprite.getU0();
         float uMax = textureSprite.getU1();
         float vMin = textureSprite.getV0();
         float vMax = textureSprite.getV1();
         uMax -= maskRight / 16.0F * (uMax - uMin);
         vMax -= maskTop / 16.0F * (vMax - vMin);
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         BufferBuilder bufferBuilder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         bufferBuilder.addVertex(matrix, xCoord, yCoord + 16.0F, 0.0F).setUv(uMin, vMax);
         bufferBuilder.addVertex(matrix, xCoord + 16.0F - maskRight, yCoord + 16.0F, 0.0F).setUv(uMax, vMax);
         bufferBuilder.addVertex(matrix, xCoord + 16.0F - maskRight, yCoord + maskTop, 0.0F).setUv(uMax, vMin);
         bufferBuilder.addVertex(matrix, xCoord, yCoord + maskTop, 0.0F).setUv(uMin, vMin);
         MeshData meshData = bufferBuilder.buildOrThrow();

         try {
            BufferUploader.drawWithShader(meshData);
         } catch (Throwable var15) {
            if (meshData != null) {
               try {
                  meshData.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }
            }

            throw var15;
         }

         if (meshData != null) {
            meshData.close();
         }
      }

      private static void setGLColorFromInt(int color) {
         float r = (color >> 16 & 0xFF) / 255.0F;
         float g = (color >> 8 & 0xFF) / 255.0F;
         float b = (color & 0xFF) / 255.0F;
         float a = (color >> 24 & 0xFF) / 255.0F;
         if (a == 0.0F) {
            a = 1.0F;
         }

         RenderSystem.setShaderColor(r, g, b, a);
      }
   }

   public static enum TooltipProvider implements IBlockComponentProvider {
      INSTANCE;

      public ResourceLocation getUid() {
         return ModPlugin.FREEZER_FLUID_UID;
      }

      public int getDefaultPriority() {
         return 1001;
      }

      public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
         boolean milk = FreezerFluidProviders.isMilk(accessor);
         boolean stale = FreezerFluidProviders.isStaleMilkData(accessor);
         if (milk || stale) {
            tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
         }

         if (milk) {
            if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
               FluidStack fluidStack = be.tank.getFluid();
               int amount = fluidStack.getAmount();
               int capacity = be.tank.getCapacity();
               if (capacity > 0) {
                  float ratio = (float)amount / capacity;
                  // 该分支仅在 KWL_IsMilk=true 时执行，直接用本模组奶流体的既有语言键
            Component name = Component.translatable("fluid.kaleidoscope_world_liquor.milk");
                  Component text = accessor.showDetails()
                     ? Component.translatable(
                           "jade.fluid2",
                           new Object[]{
                              Component.literal(name.getString()).withStyle(ChatFormatting.WHITE),
                              Component.literal(amount + "mB").withStyle(ChatFormatting.WHITE),
                              Component.literal(capacity + "mB")
                           }
                        )
                        .withStyle(ChatFormatting.GRAY)
                     : Component.translatable("jade.fluid", new Object[]{name, Component.literal(amount + "mB")});
                  IElementHelper helper = IElementHelper.get();
                  ProgressStyle progressStyle = helper.progressStyle().overlay(new FreezerFluidProviders.CustomFluidElement());
                  tooltip.add(helper.progress(ratio, text, progressStyle, BoxStyle.getNestedBox(), true));
               }
            }
         }
      }
   }
}
