package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * 重斩的掷骰与表现层（mixin handler 调用）。1.20.1 常量原样：
 * 基础 20% + 每级 20%，音效 PLAYER_ATTACK_CRIT，非玩家攻击者出 5 个暴击粒子。
 */
public final class DoubleDamageHelper {
    private static final Random RANDOM = new Random();
    private static final float BASE_CHANCE = 0.2F;
    private static final float CHANCE_PER_LEVEL = 0.2F;

    private DoubleDamageHelper() {
    }

    public static boolean rollAndBroadcast(LivingEntity attacker, LivingEntity target, int amplifier) {
        float chance = BASE_CHANCE + amplifier * CHANCE_PER_LEVEL;
        if (RANDOM.nextFloat() >= chance) {
            return false;
        }
        Level level = target.level();
        level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.5F);
        if (!(attacker instanceof Player)) {
            for (int i = 0; i < 5; i++) {
                double x = target.getX() + RANDOM.nextDouble() * 2.0 - 1.0;
                double y = target.getY() + target.getBbHeight() / 2.0F;
                double z = target.getZ() + RANDOM.nextDouble() * 2.0 - 1.0;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT, x, y, z, 0.0, -0.1, 0.0);
            }
        }
        return true;
    }
}
