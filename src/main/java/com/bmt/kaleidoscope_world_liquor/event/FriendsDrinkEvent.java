package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.network.FriendsDrinkPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 官方 1.2.0：干杯之歌服务端跟踪——带 smc:ohh_ye_ye 的非玩家实体按维度登记，
 * 每 20t 向 64 格内玩家下发实体 id+剩余时长（目标为空时只发一次空包收尾）。
 */
public class FriendsDrinkEvent {
    private static final double SYNC_RANGE_SQR = 4096.0;
    private static final int SYNC_INTERVAL = 20;
    private static final Map<ResourceKey<Level>, Set<LivingEntity>> TRACKED = new HashMap<>();
    private static final Set<UUID> EMPTY_SENT = new HashSet<>();
    private static long ticks;

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (level.isClientSide()) {
                return;
            }
            if (ModEffects.OHH_YE_YE != null && entity instanceof LivingEntity living && !(living instanceof ServerPlayer) && living.hasEffect(ModEffects.OHH_YE_YE)) {
                tracked(level).add(living);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(FriendsDrinkEvent::onServerTick);
    }

    /** 效果挂钩：效果挂上/摘除时维护登记表（mixin 调用）。 */
    public static void onEffectAdded(Entity entity, MobEffectInstance instance) {
        if (ModEffects.OHH_YE_YE != null && !entity.level().isClientSide()
                && !(entity instanceof ServerPlayer) && entity instanceof LivingEntity living && isTarget(instance)) {
            tracked(living.level()).add(living);
        }
    }

    public static void onEffectRemoved(Entity entity, MobEffectInstance instance) {
        if (isTarget(instance)) {
            Set<LivingEntity> set = TRACKED.get(entity.level().dimension());
            if (set != null) {
                set.remove(entity);
            }
        }
    }

    private static boolean isTarget(MobEffectInstance instance) {
        return ModEffects.OHH_YE_YE != null && instance != null && instance.getEffect().value() == ModEffects.OHH_YE_YE.value();
    }

    private static Set<LivingEntity> tracked(Level level) {
        return TRACKED.computeIfAbsent(level.dimension(), key -> new HashSet<>());
    }

    private static void onServerTick(MinecraftServer server) {
        ticks++;
        if (ticks % SYNC_INTERVAL != 0L || ModEffects.OHH_YE_YE == null) {
            return;
        }
        for (Set<LivingEntity> set : TRACKED.values()) {
            set.removeIf(entity -> entity.isRemoved() || !entity.hasEffect(ModEffects.OHH_YE_YE));
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.level();
            Set<LivingEntity> set = TRACKED.get(level.dimension());
            List<LivingEntity> targets = new ArrayList<>();
            if (set != null) {
                for (LivingEntity entity : set) {
                    if (entity.level() == level && player.distanceToSqr(entity) <= SYNC_RANGE_SQR) {
                        targets.add(entity);
                    }
                }
            }
            if (targets.isEmpty()) {
                if (EMPTY_SENT.add(player.getUUID())) {
                    send(player, targets);
                }
            } else {
                EMPTY_SENT.remove(player.getUUID());
                send(player, targets);
            }
        }
    }

    private static void send(ServerPlayer player, List<LivingEntity> targets) {
        int[] ids = new int[targets.size()];
        int[] durations = new int[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            LivingEntity entity = targets.get(i);
            ids[i] = entity.getId();
            MobEffectInstance instance = entity.getEffect(ModEffects.OHH_YE_YE);
            durations[i] = instance == null ? 0 : instance.getDuration();
        }
        ServerPlayNetworking.send(player, new FriendsDrinkPayload(ids, durations));
    }
}