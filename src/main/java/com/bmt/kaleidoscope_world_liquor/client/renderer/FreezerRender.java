package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class FreezerRender implements BlockEntityRenderer<FreezerBlockEntity> {
    private static final float P = 0.0625F;
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    private static FreezerRender.PanelBounds panelBounds(Direction facing) {
        return switch (facing) {
            case NORTH, SOUTH -> new FreezerRender.PanelBounds(0.0625F, 0.125F, 0.9375F, 0.75F);
            case EAST -> new FreezerRender.PanelBounds(0.125F, 0.03125F, 0.75F, 0.9375F);
            case WEST -> new FreezerRender.PanelBounds(0.0625F, 0.03125F, 0.75F, 0.9375F);
            default -> new FreezerRender.PanelBounds(0.15625F, 0.0625F, 0.75F, 0.8125F);
        };
    }

    public FreezerRender(Context context) {
    }

    public void render(
        FreezerBlockEntity be, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay
    ) {
        Level level = be.getLevel();
        if (level != null) {
            Direction facing = (Direction)be.getBlockState().getValue(FreezerBlock.FACING);
            if (!be.tank.isResourceBlank()) {
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
            // 原 Forge：be.tank.getFluid().getFluid() 取 FluidStack 里的 Fluid；
            // Fabric 上 FreezerBlockEntity.tank 是 tavern 的 CustomFluidTank（transfer API），
            // getFluid() 直接返回 Fluid（与已落地的 mixins/TapBlockMixin 用法一致）。
            Fluid fluid = be.tank.getFluid();
            if (fluid == null) {
                return;
            }

            // 原 Forge：IClientFluidTypeExtensions.of(fluid) 取静止贴图与染色；Fabric 没有
            // FluidType 扩展，改走 FluidRenderHandlerRegistry（行为等价：贴图 + 染色）。
            Level level = be.getLevel();
            FluidState fluidState = fluid.defaultFluidState();
            FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
            TextureAtlasSprite sprite;
            int color;
            if (handler != null) {
                sprite = handler.getFluidSprites(level, be.getBlockPos(), fluidState)[0];
                color = handler.getFluidColor(level, be.getBlockPos(), fluidState);
            } else {
                // 原 Forge：getStillTexture() 为 null 时回退 minecraft:block/water_still
                sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(new ResourceLocation("minecraft:block/water_still"));
                color = -1;
            }

            if (sprite == null) {
                return;
            }

            if (color == 0) {
                color = -1;
            }

            FreezerRender.PanelBounds bounds = panelBounds(facing);
            float x = bounds.x();
            float z = bounds.z();
            float width = bounds.width();
            float depth = bounds.depth();
            float y = 0.25F;
            float maxHeight = 0.375F;
            float height = maxHeight * ((float)be.tank.getAmount() / be.tank.getCapacity());

            float r = (color >> 16 & 0xFF) / 255.0F;
            float g = (color >> 8 & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            VertexConsumer consumer = source.getBuffer(RenderType.translucent());
            Matrix4f matrix = poseStack.last().pose();
            float minU = sprite.getU0();
            float maxU = sprite.getU1();
            float minV = sprite.getV0();
            float maxV = sprite.getV1();
            consumer.vertex(matrix, x, y + height, z).color(r, g, b, 1.0F).uv(maxU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix, x, y + height, z + depth).color(r, g, b, 1.0F).uv(minU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix, x + width, y + height, z + depth)
                .color(r, g, b, 1.0F)
                .uv(minU, maxV)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            consumer.vertex(matrix, x + width, y + height, z).color(r, g, b, 1.0F).uv(maxU, maxV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        } catch (Exception var28) {
        }
    }

    // 官方 1.1.12「重构冰柜渲染」：漂浮物改 2×2 网格随朝向排布、方块物品 0.4 缩放。
    // 保留行为规格 §25：有液时物品必须始终浮在液面之上（官方新布局的 0.55 在满液时会被淹没，
    // 故在官方基准高度上再做 max(液面+0.05) 抬升）。
    private void drawFloatingItems(FreezerBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Direction facing) {
        boolean hasFluid = !be.tank.isResourceBlank();
        float liquidTop = 0.25F + 0.375F * ((float) be.tank.getFluidAmountTransfer() / (float) be.tank.getCapacityTransfer());

        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = be.inputInventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                boolean isBlockItem = stack.getItem() instanceof BlockItem;
                float itemY;
                if (hasFluid) {
                    itemY = isBlockItem ? 0.55F : 0.65F;
                    itemY = Math.max(itemY, liquidTop + 0.05F);
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
            FreezerRender.PanelBounds bounds = panelBounds(facing);
            float x = bounds.x();
            float z = bounds.z();
            float width = bounds.width();
            float depth = bounds.depth();
            // 官方 1.1.12：成品贴图按 count/maxOutputCount 比例从底部升到顶（替代固定 5 级台阶）
            float topY = 0.625F;
            float bottomY = 0.3125F;
            float dropRange = topY - bottomY;
            int maxCount = be.getMaxOutputCount();
            float ratio = maxCount > 0 ? Math.min(1.0F, (float) count / maxCount) : 1.0F;
            float y = bottomY + dropRange * ratio;
            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();
            consumer.vertex(matrix, x, y, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u1, v0).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix, x, y, z + depth).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u1, v1).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix, x + width, y, z + depth)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(u0, v1)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
            consumer.vertex(matrix, x + width, y, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u0, v0).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        }
    }

    public boolean shouldRenderOffScreen(@NotNull FreezerBlockEntity be) {
        return true;
    }

    private record PanelBounds(float x, float z, float width, float depth) {
    }
}
