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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.CollisionRule;
import net.minecraft.world.scores.Team.Visibility;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class TreasureSenseRenderer {
   private static final CopyOnWriteArrayList<TreasureSenseRenderer.BlockPosWithColor> containersToRender = new CopyOnWriteArrayList<>();
   private static long lastScanTime = 0L;
   private static final int SCAN_INTERVAL = 20;
   private static final int SCAN_RADIUS = 24;
   private static final String GOLD_GLOW_TEAM = "kaleidoscope_gold_glow";
   private static final List<TreasureSenseRenderer.ContainerType> CONTAINERS = List.of(
      new TreasureSenseRenderer.ContainerType(TrappedChestBlockEntity.class, 16729156),
      new TreasureSenseRenderer.ContainerType(ChestBlockEntity.class, 16766720),
      new TreasureSenseRenderer.ContainerType(BarrelBlockEntity.class, 16766720)
   );

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES.register(context -> renderTreasures(context.matrixStack()));
      ClientTickEvents.END_CLIENT_TICK.register(mc -> {
         if (mc.player != null) {
            onClientTick();
         }
      });
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

            for (TreasureSenseRenderer.BlockPosWithColor container : containersToRender) {
               drawBox(poseStack, buffer, container.pos(), container.color(), 1.0F);
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
         double rangeSq = 576.0;
         List<MinecartChest> minecarts = mc.level
            .getEntitiesOfClass(MinecartChest.class, mc.player.getBoundingBox().inflate(29.0), minecartx -> !minecartx.isRemoved());
         Scoreboard scoreboard = mc.level.getScoreboard();
         PlayerTeam goldTeam = scoreboard.getPlayerTeam("kaleidoscope_gold_glow");
         if (goldTeam == null) {
            goldTeam = scoreboard.addPlayerTeam("kaleidoscope_gold_glow");
            goldTeam.setColor(ChatFormatting.GOLD);
            goldTeam.setCollisionRule(CollisionRule.NEVER);
            goldTeam.setNameTagVisibility(Visibility.NEVER);
         }

         for (MinecartChest minecart : minecarts) {
            IGlowingEntity glowingMinecart = (IGlowingEntity)minecart;
            double distanceSq = mc.player.distanceToSqr(minecart);
            boolean shouldGlow = hasTreasureSense && distanceSq <= rangeSq;
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
            for (MinecartChest minecartx : minecarts) {
               IGlowingEntity glowingMinecart = (IGlowingEntity)minecartx;
               if (glowingMinecart.isModGlowing()) {
                  glowingMinecart.setGlowing(false);
                  scoreboard.removePlayerFromTeam(minecartx.getStringUUID(), goldTeam);
               }
            }

            if (goldTeam.getPlayers().isEmpty()) {
               scoreboard.removePlayerTeam(goldTeam);
            }

            containersToRender.clear();
         } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastScanTime > 1000L) {
               lastScanTime = currentTime;
               scanForContainers(mc);
            }
         }
      }
   }

   private static void scanForContainers(Minecraft mc) {
      containersToRender.clear();
      BlockPos playerPos = mc.player.blockPosition();
      ChunkPos playerChunkPos = mc.player.chunkPosition();
      int chunkRadius = 2;

      for (int cx = playerChunkPos.x - chunkRadius; cx <= playerChunkPos.x + chunkRadius; cx++) {
         for (int cz = playerChunkPos.z - chunkRadius; cz <= playerChunkPos.z + chunkRadius; cz++) {
            LevelChunk chunk = mc.level.getChunk(cx, cz);
            if (chunk != null && !chunk.isEmpty()) {
               for (BlockEntity be : chunk.getBlockEntities().values()) {
                  if (!be.isRemoved() && !(be.getBlockPos().distSqr(playerPos) > 576.0)) {
                     for (TreasureSenseRenderer.ContainerType type : CONTAINERS) {
                        if (type.clazz.isInstance(be)) {
                           containersToRender.add(new TreasureSenseRenderer.BlockPosWithColor(be.getBlockPos(), type.color()));
                           break;
                        }
                     }
                  }
               }
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
      vertex(pose, buffer, x + size, y, z, r, g, b, opacity);
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

            for (MinecartChest minecart : mc.level.getEntitiesOfClass(MinecartChest.class, aabb, minecartx -> !minecartx.isRemoved())) {
               IGlowingEntity glowingMinecart = (IGlowingEntity)minecart;
               if (glowingMinecart.isModGlowing()) {
                  glowingMinecart.setGlowing(false);
               }
            }
         }
      }
   }

   private record BlockPosWithColor(BlockPos pos, int color) {
   }

   private record ContainerType(Class<? extends BlockEntity> clazz, int color) {
   }
}
