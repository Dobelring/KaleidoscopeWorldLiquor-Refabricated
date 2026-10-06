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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
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
 * 宝藏感知：穿透线框（关深度测试的 DEBUG_LINES 顶点缓冲）+ 容器/矿车发光队伍。
 *
 * <p>官方 1.1.12 重构：目标列表改由服务端扫描后经 TreasureSensePacket 下发
 * （原客户端 5×5 区块自行扫描箱子/木桶 + 近身矿车距离判定全部移除），
 * 客户端只负责按包内坐标重建线框、按包内实体 id 点亮矿车。
 *
 * <p>Fabric 事件映射（沿用移植版既有方案）：
 * <ul>
 *   <li>画线框 → {@code WorldRenderEvents.BEFORE_ENTITIES}；</li>
 *   <li>发光队伍更新 → {@code ClientTickEvents.END_CLIENT_TICK}（原 Forge 逐帧 END tick）。 </li>
 * </ul>
 */
@Environment(EnvType.CLIENT)
public class TreasureSenseRender {
    private static VertexBuffer vertexBuffer;
    private static boolean dirty = true;
    private static final List<BlockPos> LOOT_BLOCK_POSITIONS = new CopyOnWriteArrayList<>();
    private static volatile Set<Integer> lootMinecartIds = Set.of();
    private static final int GLOW_COLOR = 16766720;
    private static final String GOLD_GLOW_TEAM = "kaleidoscope_gold_glow";

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

    /** 服务端 TreasureSensePacket 到达后由 ClientPacketHandler 调用（官方 handleTreasureSense/updateLootTargets 等价）。 */
    public static void updateLootTargets(List<BlockPos> positions, List<Integer> minecartIds) {
        LOOT_BLOCK_POSITIONS.clear();
        LOOT_BLOCK_POSITIONS.addAll(positions);
        lootMinecartIds = Set.copyOf(minecartIds);
        dirty = true;
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
                if (vertexBuffer == null || dirty) {
                    dirty = false;
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
                boolean isCurrentlyGlowing = glowingMinecart.isModGlowing();
                if (isCurrentlyGlowing != shouldGlow) {
                    if (shouldGlow) {
                        scoreboard.addPlayerToTeam(minecart.getStringUUID(), goldTeam);
                        glowingMinecart.setGlowing(true);
                    } else {
                        glowingMinecart.setGlowing(false);
                        scoreboard.removePlayerFromTeam(minecart.getStringUUID(), goldTeam);
                    }
                }
            }

            if (!hasTreasureSense && goldTeam.getPlayers().isEmpty()) {
                scoreboard.removePlayerTeam(goldTeam);
            }
        }
    }

    private static void rebuildVertexBuffer() {
        if (RenderSystem.isOnRenderThread()) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
            }

            vertexBuffer = new VertexBuffer(Usage.DYNAMIC);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            for (BlockPos pos : LOOT_BLOCK_POSITIONS) {
                drawBox(buffer, pos, GLOW_COLOR, 1.0F);
            }

            vertexBuffer.bind();
            vertexBuffer.upload(buffer.end());
            VertexBuffer.unbind();
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
        buffer.vertex(x + size, y + size, z).color(r, g, b, opacity).endVertex();
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

            LOOT_BLOCK_POSITIONS.clear();
            lootMinecartIds = Set.of();
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Scoreboard scoreboard = mc.level.getScoreboard();
                PlayerTeam goldTeam = scoreboard.getPlayerTeam("kaleidoscope_gold_glow");

                for (AbstractMinecartContainer minecart : mc.level
                    .getEntitiesOfClass(
                        AbstractMinecartContainer.class,
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
}
