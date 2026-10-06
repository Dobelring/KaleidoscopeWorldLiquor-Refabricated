package com.bmt.kaleidoscope_world_liquor.client.render;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 宝藏感知（treasure_sense）。
 * <p>
 * <b>官方 1.1.11 重构：目标列表改由服务端扫描后经 {@code TreasureSensePayload} 下发</b>
 * （原客户端每秒扫 5×5 区块箱子/木桶 + 近身矿车距离判定全部移除），
 * 客户端只负责按包内坐标画线框、按包内实体 id 点亮矿车。
 *
 * <p><b>26.3：渲染管线保持本分支既有写法不变</b>——改用原版 Gizmos 的 always-on-top 通道
 * （{@code Gizmos.cuboid(pos, style).setAlwaysOnTop()}），发射时机用
 * {@code LevelRenderEvents.BEFORE_GIZMOS}；26.3 是 frame graph 渲染，
 * 层级渲染期间不能再自建 render pass（会抛 "Close the existing render pass..."）。
 * 本次只改数据来源，不动渲染调用。
 *
 * <p>线框颜色按官方 1.1.11 固定 16766720（金），不再区分陷阱箱。
 */
@Environment(EnvType.CLIENT)
public final class TreasureSenseRenderer {
    /** 服务端下发的容器坐标（不可变快照，客户端主线程读写）。 */
    private static volatile List<BlockPos> lootTargets = List.of();
    /** 服务端下发的「有战利品表的容器矿车」实体 id 集合。 */
    private static volatile Set<Integer> lootMinecartIds = Set.of();

    private static final int GLOW_COLOR = 16766720;
    /** Gizmos 颜色按 ARGB 解析：官方 16766720=0x00FFD700 高位 alpha 为 0 → 全透明不可见，必须补不透明位。 */
    private static final int GLOW_COLOR_ARGB = 0xFF000000 | GLOW_COLOR;
    private static final double MINECART_INFLATE = 64.0;
    private static final String GOLD_GLOW_TEAM = "kaleidoscope_gold_glow";
    private static final float BOX_LINE_WIDTH = 2.0F;

    private TreasureSenseRenderer() {
    }

    public static void register() {
        net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            List<BlockPos> targets = lootTargets;
            if (targets.isEmpty()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            for (BlockPos pos : targets) {
                Gizmos.cuboid(pos, GizmoStyle.stroke(GLOW_COLOR_ARGB, BOX_LINE_WIDTH)).setAlwaysOnTop();
            }
        });
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) {
                clearTargets();
                return;
            }
            if (!mc.player.hasEffect(ModEffects.TREASURE_SENSE)) {
                // 效果结束后必须继续跑矿车清理：无效果分支会解除残留的金色发光
                // 与队伍，否则矿车轮廓会永远残留。
                clearTargets();
                tickMinecartGlow(mc);
                return;
            }
            tickMinecartGlow(mc);
        });
    }

    /** 服务端 TreasureSensePayload 到达后由 ClientPacketHandler 调用。 */
    public static void updateLootTargets(List<BlockPos> positions, List<Integer> minecartIds) {
        lootTargets = List.copyOf(positions);
        lootMinecartIds = Set.copyOf(minecartIds);
    }

    /** 效果消失 / 断线时清空包内目标（官方同款语义）。 */
    private static void clearTargets() {
        lootTargets = List.of();
        lootMinecartIds = Set.of();
    }

    /**
     * 容器矿车金色发光：目标集合来自服务端 payload，
     * 24 格内扫描半径由服务端决定，客户端只按 id 匹配 + inflate(64) 兜底找实体。
     */
    private static void tickMinecartGlow(Minecraft mc) {
        if (mc.isPaused()) {
            return;
        }
        boolean hasTreasureSense = mc.player.hasEffect(ModEffects.TREASURE_SENSE);
        Set<Integer> targetIds = hasTreasureSense ? lootMinecartIds : Set.of();
        List<AbstractMinecartContainer> minecarts = mc.level.getEntitiesOfClass(AbstractMinecartContainer.class,
                mc.player.getBoundingBox().inflate(MINECART_INFLATE), minecart -> !minecart.isRemoved());
        PlayerTeam goldTeam = mc.level.getScoreboard().getPlayerTeam(GOLD_GLOW_TEAM);
        if (goldTeam == null) {
            goldTeam = mc.level.getScoreboard().addPlayerTeam(GOLD_GLOW_TEAM);
            goldTeam.setColor(Optional.of(net.minecraft.world.scores.TeamColor.GOLD));
            goldTeam.setCollisionRule(Team.CollisionRule.NEVER);
            goldTeam.setNameTagVisibility(Team.Visibility.NEVER);
        }

        for (AbstractMinecartContainer minecart : minecarts) {
            IGlowingEntity glowingMinecart = (IGlowingEntity) minecart;
            boolean shouldGlow = targetIds.contains(minecart.getId());
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
            for (AbstractMinecartContainer minecart : minecarts) {
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
