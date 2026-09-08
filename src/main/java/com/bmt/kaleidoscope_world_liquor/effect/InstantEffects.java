package com.bmt.kaleidoscope_world_liquor.effect;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * 全部 5 个带行为的自定义效果：
 * - ExplosionEffect：剩余 1 tick 时爆炸（power 3+amplifier，破坏方块）
 * - LevelBoostEffect：剩余 1 tick 时给 3+3*amp 等级
 * - RespawnEffect：剩余 1 tick 时回重生点（含跨维度），给饥饿效果
 * - CrazyEffect：剩余 1 tick 时随机刷原版效果（enableModdedEffects 配置项控制是否含模组效果）
 * - DoubleDamageEffect：Fabric AFTER_DAMAGE 挂钩概率双倍伤害
 */
public final class InstantEffects {
    private InstantEffects() {
    }

    /** 爆炸 */
    public static class ExplosionEffect extends MobEffect {
        private static final float BASE_POWER = 3.0F;
        private static final float POWER_PER_AMPLIFIER = 1.0F;

        public ExplosionEffect(int color) {
            super(MobEffectCategory.HARMFUL, color);
        }

        @Override
        public boolean isInstantaneous() {
            return true;
        }

        @Override
        public boolean applyEffectTick(@NotNull ServerLevel level, @NotNull LivingEntity entity, int amplifier) {
            float power = BASE_POWER + amplifier * POWER_PER_AMPLIFIER;
            level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), power, Level.ExplosionInteraction.TNT);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration == 1;
        }
    }

    /** 经验等级提升 */
    public static class LevelBoostEffect extends MobEffect {
        private static final int BASE_LEVELS = 3;
        private static final int LEVELS_PER_AMPLIFIER = 3;

        public LevelBoostEffect(int color) {
            super(MobEffectCategory.HARMFUL, color);
        }

        @Override
        public boolean isInstantaneous() {
            return true;
        }

        @Override
        public boolean applyEffectTick(@NotNull ServerLevel level, @NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof Player player) {
                player.giveExperienceLevels(BASE_LEVELS + amplifier * LEVELS_PER_AMPLIFIER);
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration == 1;
        }
    }

    /** 回到重生点 */
    public static class RespawnEffect extends MobEffect {
        public RespawnEffect() {
            super(MobEffectCategory.NEUTRAL, 0x87CEEB);
        }

        @Override
        public boolean isInstantaneous() {
            return true;
        }

        @Override
        public boolean applyEffectTick(@NotNull ServerLevel level, @NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer serverPlayer) {
                net.minecraft.world.level.portal.TeleportTransition transition =
                        serverPlayer.findRespawnPositionAndUseSpawnBlock(false, net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING);
                ServerLevel targetLevel = transition.newLevel();
                Vec3 pos = transition.position();
                level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.teleport(new net.minecraft.world.level.portal.TeleportTransition(
                        targetLevel, pos, Vec3.ZERO, serverPlayer.getYRot(), serverPlayer.getXRot(),
                        net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
                serverPlayer.fallDistance = 0.0F;
                targetLevel.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration == 1;
        }
    }

    /** 随机效果轰炸 */
    public static class CrazyEffect extends MobEffect {
        public CrazyEffect() {
            super(MobEffectCategory.HARMFUL, 0xFF00FF);
        }

        @Override
        public boolean isInstantaneous() {
            return true;
        }

        @Override
        public boolean applyEffectTick(@NotNull ServerLevel level, @NotNull LivingEntity entity, int amplifier) {
            boolean enableModded = com.bmt.kaleidoscope_world_liquor.config.ModConfigs.ENABLE_MODDED_EFFECTS.get();
            BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
                if (effect != null && effect != this) {
                    if (!enableModded) {
                        Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                        if (effectId == null || !"minecraft".equals(effectId.getNamespace())) {
                            return;
                        }
                    }
                    entity.addEffect(new MobEffectInstance(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), 200, amplifier, false, false));
                }
            });
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration == 1;
        }
    }

    /** 概率双倍伤害（纯标记；伤害翻倍由 DoubleDamageMixin 注入 actuallyHurt 实现） */
    public static class DoubleDamageEffect extends MobEffect {
        public DoubleDamageEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xFF3C00);
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return false;
        }

        @Override
        public boolean applyEffectTick(@NotNull ServerLevel level, @NotNull LivingEntity entity, int amplifier) {
            return true;
        }
    }
}
