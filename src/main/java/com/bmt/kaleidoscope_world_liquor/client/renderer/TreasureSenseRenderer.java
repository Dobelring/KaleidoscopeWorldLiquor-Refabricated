package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.CollisionRule;
import net.minecraft.world.scores.Team.Visibility;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * 宝藏感知：穿透线框 + 容器/矿车发光队伍。
 * <p>
 * 官方 1.1.11 重构：目标列表改由服务端扫描后经 TreasureSensePayload 下发
 * （原客户端 5×5 区块自行扫描箱子/木桶 + 近身矿车距离判定全部移除），
 * 客户端只负责按包内坐标画线框、按包内实体 id 点亮矿车。
 */
@Environment(EnvType.CLIENT)
public class TreasureSenseRenderer {
   private static final CopyOnWriteArrayList<BlockPos> containersToRender = new CopyOnWriteArrayList<>();
   private static volatile Set<Integer> lootMinecartIds = Set.of();
   private static final int GLOW_COLOR = 16766720;
   private static final String GOLD_GLOW_TEAM = "kaleidoscope_gold_glow";

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES.register(context -> renderTreasures(context.matrixStack()));
      ClientTickEvents.END_CLIENT_TICK.register(mc -> {
         if (mc.player != null) {
            onClientTick();
         }
      });
   }

   /** 服务端 TreasureSensePayload 到达后由 ClientPacketHandler 调用。 */
   public static void updateLootTargets(List<BlockPos> positions, List<Integer> minecartIds) {
      containersToRender.clear();
      containersToRender.addAll(positions);
      lootMinecartIds = Set.copyOf(minecartIds);
   }

   public static void renderTreasures(PoseStack poseStack) {
      if (poseStack == null) {
         return;
      }

      if (RenderSystem.isOnRenderThread()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null || !mc.player.hasEffect(ModEffects.TREASURE_SENSE_EFFECT)) {
            containersToRender.clear();
         } else if (!containersToRender.isEmpty()) {
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GL11.glEnable(2848);
            RenderSystem.depthFunc(519);
            RenderSystem.polygonOffset(-1.0F, -1.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.lineWidth(2.0F);
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            for (BlockPos pos : containersToRender) {
               drawBox(poseStack, buffer, pos, GLOW_COLOR, 1.0F);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
            RenderSystem.depthFunc(515);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            GL11.glDisable(2848);
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
         }
      }
   }

   public static void onClientTick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && !mc.isPaused()) {
         boolean hasTreasureSense = mc.player.hasEffect(ModEffects.TREASURE_SENSE_EFFECT);
         Set<Integer> targetIds = hasTreasureSense ? lootMinecartIds : Collections.emptySet();
         Scoreboard scoreboard = mc.level.getScoreboard();
         PlayerTeam goldTeam = scoreboard.getPlayerTeam("kaleidoscope_gold_glow");
         if (goldTeam == null) {
            goldTeam = scoreboard.addPlayerTeam("kaleidoscope_gold_glow");
            goldTeam.setColor(ChatFormatting.GOLD);
            goldTeam.setCollisionRule(CollisionRule.NEVER);
            goldTeam.setNameTagVisibility(Visibility.NEVER);
         }

         for (AbstractMinecartContainer minecart : mc.level
            .getEntitiesOfClass(AbstractMinecartContainer.class, mc.player.getBoundingBox().inflate(64.0), minecartx -> !minecartx.isRemoved())) {
            IGlowingEntity glowingMinecart = (IGlowingEntity)minecart;
            boolean shouldGlow = targetIds.contains(minecart.getId());
            boolean isCurrentlyModGlowing = glowingMinecart.isModGlowing();
            if (isCurrentlyModGlowing != shouldGlow) {
               if (shouldGlow) {
                  scoreboard.addPlayerToTeam(minecart.getStringUUID(), goldTeam);
                  glowingMinecart.setGlowing(true);
               } else {
                  glowingMinecart.setGlowing(false);
                  scoreboard.removePlayerFromTeam(minecart.getStringUUID(), goldTeam);
               }
            }
         }

         if (!hasTreasureSense) {
            containersToRender.clear();
            lootMinecartIds = Set.of();
            if (goldTeam.getPlayers().isEmpty()) {
               scoreboard.removePlayerTeam(goldTeam);
            }
         }
      }
   }

   private static void drawBox(Matrix4f pose, BufferBuilder buffer, BlockPos pos, int color, float opacity) {
      float x = pos.getX();
      float y = pos.getY();
      float z = pos.getZ();
      float size = 1.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      vertex(pose, buffer, x, y + size, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y + size, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y + size, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y + size, z + size, r, g, b, opacity);
      vertex(pose, buffer, x + size, y + size, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y + size, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y + size, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y + size, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y, z + size, r, g, b, opacity);
      vertex(pose, buffer, x + size, y, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y, z, r, g, b, opacity);
      vertex(pose, buffer, x, y, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y, z + size, r, g, b, opacity);
      vertex(pose, buffer, x + size, y + size, z + size, r, g, b, opacity);
      vertex(pose, buffer, x + size, y + size, z, r, g, b, opacity);
      vertex(pose, buffer, x + size, y + size, z, r, g, b, opacity);
      vertex(pose, buffer, x, y, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y + size, z + size, r, g, b, opacity);
      vertex(pose, buffer, x, y, z, r, g, b, opacity);
      vertex(pose, buffer, x, y + size, z, r, g, b, opacity);
   }

   private static void drawBox(PoseStack poseStack, BufferBuilder buffer, BlockPos pos, int color, float opacity) {
      drawBox(poseStack.last().pose(), buffer, pos, color, opacity);
   }

   private static void vertex(Matrix4f matrix, BufferBuilder buffer, float x, float y, float z, float r, float g, float b, float a) {
      buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
   }

   public static void freeBuffer() {
      if (RenderSystem.isOnRenderThread()) {
         containersToRender.clear();
         lootMinecartIds = Set.of();
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            Scoreboard scoreboard = mc.level.getScoreboard();
            PlayerTeam goldTeam = scoreboard.getPlayerTeam("kaleidoscope_gold_glow");
            if (goldTeam != null) {
               for (String player : goldTeam.getPlayers()) {
                  scoreboard.removePlayerFromTeam(player, goldTeam);
               }

               scoreboard.removePlayerTeam(goldTeam);
            }

            AABB aabb = mc.player != null ? mc.player.getBoundingBox().inflate(1000.0) : new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

            for (AbstractMinecartContainer minecart : mc.level.getEntitiesOfClass(AbstractMinecartContainer.class, aabb, minecartx -> !minecartx.isRemoved())) {
               IGlowingEntity glowingMinecart = (IGlowingEntity)minecart;
               if (glowingMinecart.isModGlowing()) {
                  glowingMinecart.setGlowing(false);
               }
            }
         }
      }
   }
}
