package com.bmt.kaleidoscope_world_liquor.client.event;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

import java.util.List;

/**
 * 冥视（hostile_detection）：与 1.20.1 原版一致的客户端发光——
 * 每 tick 检测 37 格内的敌对生物，32 格内发光显示（IGlowingEntity）。
 * 原版无任何音效，不添加。
 */
@Environment(EnvType.CLIENT)
public final class HostileDetectionHandler {
    private static final double RANGE_SQ = 1024.0;

    private HostileDetectionHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) {
            return;
        }
        boolean hasEffect = mc.player.hasEffect(ModEffects.HOSTILE_DETECTION);
        List<Mob> mobs = mc.level.getEntitiesOfClass(Mob.class,
                mc.player.getBoundingBox().inflate(37.0), mob -> mob.isAlive());
        for (Mob mob : mobs) {
            IGlowingEntity glowingMob = (IGlowingEntity) mob;
            boolean isHostile = mob instanceof Enemy;
            boolean shouldGlow = hasEffect && isHostile && mc.player.distanceToSqr(mob) <= RANGE_SQ;
            if (glowingMob.isModGlowing() != shouldGlow) {
                glowingMob.setGlowing(shouldGlow);
            }
        }
        if (!hasEffect) {
            for (Mob mob : mobs) {
                IGlowingEntity glowingMob = (IGlowingEntity) mob;
                if (glowingMob.isModGlowing()) {
                    glowingMob.setGlowing(false);
                }
            }
        }
    }
}
