package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
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

/**
 * 宝物感知：穿透线框（关深度测试的 DEBUG_LINES 顶点缓冲）+ 矿车发光队伍。
 *
 * <p>原 Forge：{@code @EventBusSubscriber(Bus.FORGE, Dist.CLIENT)} 下三个 @SubscribeEvent——
 * {@code RenderLevelStageEvent(AFTER_SOLID_BLOCKS)} 画线框、{@code RenderTickEvent(Phase.END)}
 * 更新发光队伍、以及 {@link #freeBuffer()}。Fabric 映射：
 * <ul>
 *   <li>画线框 → {@code WorldRenderEvents.BEFORE_ENTITIES}（见 {@link #register()}）；
 *       原 Forge 下 ClientForgeEvents 的 AFTER_CUTOUT_BLOCKS 钩子也会调用 renderTreasures，
 *       两个阶段各画一次，这里两个钩子都挂到同一回调点，保留每帧两次绘制的原语义。</li>
 *   <li>RenderTickEvent(Phase.END) → {@code ClientTickEvents.END_CLIENT_TICK}（行为等价，
 *       频率由每渲染帧改为每客户端 tick 20Hz；发光队伍/计分板更新不需要逐帧）。</li>
 * </ul>
 */
@Environment(EnvType.CLIENT)
public class TreasureSenseRender {
    private static VertexBuffer vertexBuffer;
    private static boolean requestedRefresh = false;
    private static long lastScanTime = 0L;
    private static final int SCAN_INTERVAL = 20;
    private static final int SCAN_RADIUS = 24;
    private static final String GOLD_GLOW_TEAM = "kaleidoscope_gold_glow";
    private static final List<TreasureSenseRender.ContainerType> CONTAINERS = List.of(
        new TreasureSenseRender.ContainerType(TrappedChestBlockEntity.class, 16729156),
        new TreasureSenseRender.ContainerType(ChestBlockEntity.class, 16766720),
        new TreasureSenseRender.ContainerType(BarrelBlockEntity.class, 16766720)
    );

    public TreasureSenseRender() {
    }

    // 幂等守卫：ClientForgeEvents.register() 与客户端入口可能都调用本方法
    private static boolean registered = false;

    /**
     * 原 Forge 的 @SubscribeEvent 注册；由主线的客户端入口类（或 ClientForgeEvents.register()）调用。
     */
    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        WorldRenderEvents.BEFORE_ENTITIES.register(TreasureSenseRender::onRenderLevelStage);
        ClientTickEvents.END_CLIENT_TICK.register(client -> onRenderTick());
    }

    // 原签名 onRenderLevelStage(RenderLevelStageEvent)，判定 stage == AFTER_SOLID_BLOCKS；
    // Fabric 的 WorldRenderEvents 无分段，直接绘制（renderTreasures 内部自带效果判定）。
    public static void onRenderLevelStage(WorldRenderContext event) {
        renderTreasures(event);
    }

    public static void renderTreasures(WorldRenderContext event) {
        if (RenderSystem.isOnRenderThread()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.hasEffect(ModEffects.TREASURE_SENSE_EFFECT)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastScanTime > 1000L) {
                    requestedRefresh = true;
                    lastScanTime = currentTime;
                }

                if (vertexBuffer == null || requestedRefresh) {
                    requestedRefresh = false;
                    rebuildVertexBuffer();
                }

                if (vertexBuffer != null) {
                    Vec3 cameraPos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glEnable(2848);
                    GL11.glDisable(2929);
                    GL11.glLineWidth(2.0F);
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    PoseStack matrix = event.matrixStack();
                    matrix.pushPose();
                    matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                    vertexBuffer.bind();
                    vertexBuffer.drawWithShader(matrix.last().pose(), new Matrix4f(event.projectionMatrix()), RenderSystem.getShader());
                    VertexBuffer.unbind();
                    matrix.popPose();
                    GL11.glEnable(2929);
                    GL11.glDisable(3042);
                    GL11.glDisable(2848);
                    GL11.glLineWidth(1.0F);
                }
            } else {
                if (vertexBuffer != null) {
                    vertexBuffer.close();
                    vertexBuffer = null;
                }
            }
        }
    }

    // 原签名 onRenderTick(RenderTickEvent)，判定 event.phase == Phase.END；
    // Fabric 的 END_CLIENT_TICK 即“每客户端 tick 末尾”，触发点等价。
    public static void onRenderTick() {
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
            }
        }
    }

    private static void rebuildVertexBuffer() {
        if (RenderSystem.isOnRenderThread()) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                vertexBuffer = new VertexBuffer(Usage.STATIC);
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder buffer = tessellator.getBuilder();
                buffer.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                BlockPos playerPos = mc.player.blockPosition();
                ChunkPos playerChunkPos = mc.player.chunkPosition();
                int chunkRadius = 2;

                for (int cx = playerChunkPos.x - chunkRadius; cx <= playerChunkPos.x + chunkRadius; cx++) {
                    for (int cz = playerChunkPos.z - chunkRadius; cz <= playerChunkPos.z + chunkRadius; cz++) {
                        LevelChunk chunk = mc.level.getChunk(cx, cz);
                        if (chunk != null && !chunk.isEmpty()) {
                            for (BlockEntity be : chunk.getBlockEntities().values()) {
                                if (!be.isRemoved() && !(be.getBlockPos().distSqr(playerPos) > 576.0)) {
                                    for (TreasureSenseRender.ContainerType type : CONTAINERS) {
                                        if (type.clazz.isInstance(be)) {
                                            drawBox(buffer, be.getBlockPos(), type.color, 1.0F);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                vertexBuffer.bind();
                vertexBuffer.upload(buffer.end());
                VertexBuffer.unbind();
            }
        }
    }

    private static void drawBox(BufferBuilder buffer, BlockPos pos, int color, float opacity) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float size = 1.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        buffer.vertex(x, y + size, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y, z).color(r, g, b, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(r, g, b, opacity).endVertex();
    }

    public static void freeBuffer() {
        if (RenderSystem.isOnRenderThread()) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Scoreboard scoreboard = mc.level.getScoreboard();
                PlayerTeam goldTeam = scoreboard.getPlayerTeam("kaleidoscope_gold_glow");

                for (MinecartChest minecart : mc.level
                    .getEntitiesOfClass(
                        MinecartChest.class,
                        mc.player != null ? mc.player.getBoundingBox().inflate(1000.0) : new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                        minecartx -> !minecartx.isRemoved()
                    )) {
                    IGlowingEntity glowingMinecart = (IGlowingEntity)minecart;
                    if (glowingMinecart.isModGlowing()) {
                        glowingMinecart.setGlowing(false);
                        if (goldTeam != null) {
                            scoreboard.removePlayerFromTeam(minecart.getStringUUID(), goldTeam);
                        }
                    }
                }

                if (goldTeam != null) {
                    scoreboard.removePlayerTeam(goldTeam);
                }
            }
        }
    }

    private record ContainerType(Class<? extends BlockEntity> clazz, int color) {
    }
}
