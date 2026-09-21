package com.bmt.kaleidoscope_world_liquor.client.render;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 宝藏感知（treasure_sense）。
 * 每 1s 扫描 2 chunk（24 格）内的箱子/陷阱箱/木桶，画穿墙线框；
 * 24 格内的运输矿车加入 GOLD 发光队伍（1.20.1 原版 kaleidoscope_gold_glow）。
 * 颜色：陷阱箱红、箱子/木桶金（1.20.1 原值 16729156/16766720）。
 *
 * <p><b>26.3：改用原版 Gizmos 的 always-on-top 通道。</b>
 * 26.2 及更早是自建方案（注册 LINES_SNIPPET + 深度恒过的自定义管线，BufferBuilder 收顶点，
 * 再自己 createRenderPass 直画主渲染目标）——那是为了绕开 26.1.2 时代
 * {@code Gizmos.setAlwaysOnTop} 在光影下失效（当时它是"清主目标深度后重画"，Iris 重定向帧缓冲后不生效）。
 * 26.3 渲染改走 frame graph 后这条路走不通了：层级渲染期间已有打开的 render pass，
 * 再 {@code createRenderPass} 会抛 "Close the existing render pass before creating a new one!"；
 * 而原版现在**内建了 always-on-top 通道**（{@code LevelRenderer.executeAlwaysOnTop} +
 * {@code LevelTargetBundle.alwaysOnTopDepth} 专用深度目标，gizmo 分 standard/alwaysOnTop 两组），
 * 于是改回原版 API：{@code Gizmos.cuboid(pos, style).setAlwaysOnTop()}。
 * 这样既没有自建管线/渲染通道，也不需要 Iris 反射绑定。
 *
 * <p>发射时机用 {@code LevelRenderEvents.BEFORE_GIZMOS}：它在本帧 gizmo 收集之前触发，
 * 此时 {@code Gizmos} 的收集器已由 {@code LevelRenderer.collectPerFrameRenderThreadGizmos()} 装好。
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
    private static final float BOX_LINE_WIDTH = 2.0F;

    private TreasureSenseRenderer() {
    }

    public static void register() {
        net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            if (containers.isEmpty()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            for (BlockPos pos : containers) {
                int color = mc.level.getBlockEntity(pos) instanceof TrappedChestBlockEntity
                        ? COLOR_TRAPPED : COLOR_CHEST;
                Gizmos.cuboid(pos, GizmoStyle.stroke(color, BOX_LINE_WIDTH)).setAlwaysOnTop();
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
        for (int cx = playerChunkPos.x() - chunkRadius; cx <= playerChunkPos.x() + chunkRadius; cx++) {
            for (int cz = playerChunkPos.z() - chunkRadius; cz <= playerChunkPos.z() + chunkRadius; cz++) {
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
            goldTeam.setColor(Optional.of(net.minecraft.world.scores.TeamColor.GOLD));
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
