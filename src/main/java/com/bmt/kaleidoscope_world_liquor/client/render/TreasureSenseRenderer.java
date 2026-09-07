package com.bmt.kaleidoscope_world_liquor.client.render;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * 宝藏感知（treasure_sense）：1.21.11 原版 Gizmos 线框。
 * 每 1s 扫描 2 chunk（24 格）内的箱子/陷阱箱/木桶，Gizmos.cuboid + setAlwaysOnTop
 * 画透视线框（1.20.1 原版画框时 glDisable(GL_DEPTH_TEST)，穿墙等价）；
 * 24 格内的运输矿车加入 GOLD 发光队伍（1.20.1 原版 kaleidoscope_gold_glow）。
 * 颜色：陷阱箱红、箱子/木桶金（1.20.1 原值 16729156/16766720）。
 * 1.21.11 GizmoStyle.stroke(int) 是 ARGB——1.20.1 的 6 位 RGB 若不补 FF alpha
 * 通道会被判为全透明，线框不可见。
 *
 * 时机安全性：Minecraft.runTick 渲染段以 try-with-resources 包住
 * LevelRenderer.collectPerFrameGizmos()（ThreadLocal GizmoCollector 注册窗口），
 * Fabric WorldRenderEvents.DebugRender 钩子在 renderLevel 内
 * DebugRenderer.emitGizmos 调用前触发——处于收集窗口内，且早于
 * finalizeGizmoCollection() 的 drain，gizmo 同帧绘制。
 */
@Environment(EnvType.CLIENT)
public final class TreasureSenseRenderer {
    private static final List<BlockPos> containers = new ArrayList<>();
    private static long lastScanTime = 0L;
    private static final int SCAN_INTERVAL_MS = 1000;
    private static final double RANGE_SQ = 576.0;
    private static final double MINECART_INFLATE = 29.0;
    private static final int COLOR_TRAPPED = 0xFFFF3C34;
    private static final int COLOR_CHEST = 0xFFFFCF00;
    private static final String GOLD_GLOW_TEAM = "kaleidoscope_gold_glow";

    private TreasureSenseRenderer() {
    }

    public static void register() {
        net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !mc.player.hasEffect(ModEffects.TREASURE_SENSE) || containers.isEmpty()) {
                return;
            }
            for (BlockPos pos : containers) {
                var aabb = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
                int color = mc.level.getBlockEntity(pos) instanceof TrappedChestBlockEntity
                        ? COLOR_TRAPPED : COLOR_CHEST;
                Gizmos.cuboid(aabb, GizmoStyle.stroke(color)).setAlwaysOnTop();
            }
        });
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) {
                containers.clear();
                return;
            }
            if (!mc.player.hasEffect(ModEffects.TREASURE_SENSE)) {
                // 效果结束后必须继续跑矿车清理：无效果分支会解除残留的金色发光
                // 与队伍（1.20.1 原版 onRenderTick 同样无条件调用），否则矿车
                // 轮廓会永远残留。
                containers.clear();
                tickMinecartGlow(mc);
                return;
            }
            long now = System.currentTimeMillis();
            if (now - lastScanTime > SCAN_INTERVAL_MS) {
                lastScanTime = now;
                scanForContainers(mc);
            }
            tickMinecartGlow(mc);
        });
    }

    private static void scanForContainers(Minecraft mc) {
        containers.clear();
        BlockPos playerPos = mc.player.blockPosition();
        ChunkPos playerChunkPos = mc.player.chunkPosition();
        int chunkRadius = 2;
        for (int cx = playerChunkPos.x - chunkRadius; cx <= playerChunkPos.x + chunkRadius; cx++) {
            for (int cz = playerChunkPos.z - chunkRadius; cz <= playerChunkPos.z + chunkRadius; cz++) {
                LevelChunk chunk = mc.level.getChunk(cx, cz);
                if (chunk != null && !chunk.isEmpty()) {
                    for (var be : chunk.getBlockEntities().values()) {
                        if (!be.isRemoved() && !(be.getBlockPos().distSqr(playerPos) > RANGE_SQ)
                                && (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity)) {
                            containers.add(be.getBlockPos());
                        }
                    }
                }
            }
        }
    }

    /**
     * 运输矿车金色发光：1.20.1 原版在 RenderTick END 管理
     * kaleidoscope_gold_glow 队伍（GOLD 色、无碰撞、隐藏名牌）。
     */
    private static void tickMinecartGlow(Minecraft mc) {
        if (mc.isPaused()) {
            return;
        }
        boolean hasTreasureSense = mc.player.hasEffect(ModEffects.TREASURE_SENSE);
        List<MinecartChest> minecarts = mc.level.getEntitiesOfClass(MinecartChest.class,
                mc.player.getBoundingBox().inflate(MINECART_INFLATE), minecart -> !minecart.isRemoved());
        PlayerTeam goldTeam = mc.level.getScoreboard().getPlayerTeam(GOLD_GLOW_TEAM);
        if (goldTeam == null) {
            goldTeam = mc.level.getScoreboard().addPlayerTeam(GOLD_GLOW_TEAM);
            goldTeam.setColor(ChatFormatting.GOLD);
            goldTeam.setCollisionRule(Team.CollisionRule.NEVER);
            goldTeam.setNameTagVisibility(Team.Visibility.NEVER);
        }

        for (MinecartChest minecart : minecarts) {
            IGlowingEntity glowingMinecart = (IGlowingEntity) minecart;
            boolean shouldGlow = hasTreasureSense && mc.player.distanceToSqr(minecart) <= RANGE_SQ;
            if (glowingMinecart.isModGlowing() != shouldGlow) {
                if (shouldGlow) {
                    mc.level.getScoreboard().addPlayerToTeam(minecart.getStringUUID(), goldTeam);
                    glowingMinecart.setGlowing(true);
                } else {
                    glowingMinecart.setGlowing(false);
                    mc.level.getScoreboard().removePlayerFromTeam(minecart.getStringUUID(), goldTeam);
                }
            }
        }

        if (!hasTreasureSense) {
            for (MinecartChest minecart : minecarts) {
                IGlowingEntity glowingMinecart = (IGlowingEntity) minecart;
                if (glowingMinecart.isModGlowing()) {
                    glowingMinecart.setGlowing(false);
                    mc.level.getScoreboard().removePlayerFromTeam(minecart.getStringUUID(), goldTeam);
                }
            }
            if (goldTeam.getPlayers().isEmpty()) {
                mc.level.getScoreboard().removePlayerTeam(goldTeam);
            }
        }
    }
}
