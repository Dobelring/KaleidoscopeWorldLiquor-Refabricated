package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;

/**
 * 官方 1.1.12 新增：冰柜牛奶流体的 Jade 进度条式 tooltip（服务端同步是否为奶 + 客户端贴图绘制）。
 * <p>
 * Fabric 适配：官方判定 Forge 内置奶流体（ForgeMod.MILK）→ 改判本模组自有奶流体；
 * 官方从 FreezerRender.MILK_STILL_TEXTURE 取贴图 → 直接引用本模组 block/milk_still；
 * FluidStack → tavern CustomFluidTank（transfer API）。
 */
public final class FreezerFluidProviders {
    // Fabric：官方从 FreezerRender.MILK_STILL_TEXTURE 取原版奶贴图；自有奶流体直接用本模组贴图
    private static final ResourceLocation MILK_STILL_TEXTURE = new ResourceLocation("kaleidoscope_world_liquor", "block/milk_still");

    private FreezerFluidProviders() {
    }

    private static int capacityMb(CustomFluidTank tank) {
        return (int) (tank.getCapacityTransfer() * CustomFluidTank.MB_PER_BUCKET / FluidConstants.BUCKET);
    }

    public static final class CustomFluidElement extends Element {
        private static final Vec2 DEFAULT_SIZE = new Vec2(16.0F, 16.0F);

        public Vec2 getSize() {
            return DEFAULT_SIZE;
        }

        public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
            Vec2 size = this.getCachedSize();
            float width = size.x;
            float height = size.y;
            if (!(width <= 0.0F) && !(height <= 0.0F)) {
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MILK_STILL_TEXTURE);
                if (sprite != null) {
                    drawTiledSprite(guiGraphics, x, y, width, height, -1, height, sprite);
                }
            }
        }

        public String getMessage() {
            return "";
        }

        private static void drawTiledSprite(
            GuiGraphics guiGraphics, float xPosition, float yPosition, float tiledWidth, float tiledHeight, int color, float scaledAmount, TextureAtlasSprite sprite
        ) {
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            Matrix4f matrix = guiGraphics.pose().last().pose();
            setGLColorFromInt(color);
            RenderSystem.enableBlend();
            int xTileCount = (int) (tiledWidth / 16.0F);
            float xRemainder = tiledWidth - xTileCount * 16;
            int yTileCount = (int) (scaledAmount / 16.0F);
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
                        drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight, 0.0F);
                    }
                }
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        private static void drawTextureWithMasking(
            Matrix4f matrix, float xCoord, float yCoord, TextureAtlasSprite textureSprite, float maskTop, float maskRight, float zLevel
        ) {
            float uMin = textureSprite.getU0();
            float uMax = textureSprite.getU1();
            float vMin = textureSprite.getV0();
            float vMax = textureSprite.getV1();
            uMax -= maskRight / 16.0F * (uMax - uMin);
            vMax -= maskTop / 16.0F * (vMax - vMin);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix, xCoord, yCoord + 16.0F, zLevel).uv(uMin, vMax).endVertex();
            bufferBuilder.vertex(matrix, xCoord + 16.0F - maskRight, yCoord + 16.0F, zLevel).uv(uMax, vMax).endVertex();
            bufferBuilder.vertex(matrix, xCoord + 16.0F - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
            bufferBuilder.vertex(matrix, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());
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

    public enum ServerDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
                if (!be.tank.isResourceBlank()) {
                    data.putBoolean("KWL_IsMilk", be.tank.getFluid() == ModFluids.MILK_STILL);
                }
            }
        }

        public ResourceLocation getUid() {
            return FreezerJadePlugin.FREEZER_FLUID_UID;
        }
    }

    public enum TooltipProvider implements IBlockComponentProvider {
        INSTANCE;

        public ResourceLocation getUid() {
            return FreezerJadePlugin.FREEZER_FLUID_UID;
        }

        public int getDefaultPriority() {
            return 1001;
        }

        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getServerData().getBoolean("KWL_IsMilk")) {
                if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
                    if (!be.tank.isResourceBlank()) {
                        tooltip.remove(Identifiers.UNIVERSAL_FLUID_STORAGE);
                        int amount = be.tank.getFluidAmountMb();
                        int capacity = capacityMb(be.tank);
                        if (capacity > 0) {
                            float ratio = (float) amount / capacity;
                            // 该分支仅在 KWL_IsMilk=true 时执行，直接用本模组奶流体的既有语言键
                            Component name = Component.translatable("fluid.kaleidoscope_world_liquor.milk");
                            Component text = accessor.showDetails()
                                ? Component.translatable(
                                          "jade.fluid2",
                                          Component.literal(name.getString()).withStyle(ChatFormatting.WHITE),
                                          Component.literal(amount + "mB").withStyle(ChatFormatting.WHITE),
                                          Component.literal(capacity + "mB")
                                      )
                                      .withStyle(ChatFormatting.GRAY)
                                : Component.translatable("jade.fluid", name, Component.literal(amount + "mB"));
                            IElementHelper helper = tooltip.getElementHelper();
                            IProgressStyle progressStyle = helper.progressStyle().overlay(new CustomFluidElement());
                            tooltip.add(helper.progress(ratio, text, progressStyle, BoxStyle.DEFAULT, true));
                        }
                    }
                }
            }
        }
    }
}
