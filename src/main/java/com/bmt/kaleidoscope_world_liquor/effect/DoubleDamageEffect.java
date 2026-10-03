package com.bmt.kaleidoscope_world_liquor.effect;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import java.util.Random;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DoubleDamageEffect extends MobEffect {
    private static final Random RANDOM = new Random();
    private static final float BASE_CHANCE = 0.2F;
    private static final float CHANCE_PER_LEVEL = 0.2F;
    private static final float DAMAGE_MULTIPLIER = 2.0F;

    public DoubleDamageEffect() {
        super(MobEffectCategory.BENEFICIAL, 16729344);
    }

    public boolean isInstantenous() {
        return false;
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    // 已接线：mixins/LivingEntityDamageMixin 在 actuallyHurt 末尾 setHealth 前调用本方法
    //（吸收已记账、过 zero-damage 早退，等价原 LivingDamageEvent 时机：完全吸收的伤害不触发）。
    // 原 Forge 私有监听器 onLivingDamage(LivingDamageEvent)（构造器 addListener 挂钩）。
    // 语义：服务端、伤害结算时，若伤害来源的直接实体 attacker 持有 double_damage 效果，
    // 按 0.2 + 0.2 * amplifier 概率将本次伤害 ×2，并播放暴击音效 + 暴击表现；未触发返回原值。
    // 注：官方源码内并无 DOUBLE_DAMAGE_PROCESSING 防重入集合（已 grep 核实），本方法亦无嵌套伤害调用，故未引入。
    public static float modifyDamage(LivingEntity target, DamageSource source, float amount) {
        Level level = target.level();
        if (!level.isClientSide()) {
            if (source.getEntity() instanceof LivingEntity attacker) {
                MobEffectInstance effectInstance = attacker.getEffect(ModEffects.DOUBLE_DAMAGE_EFFECT);
                if (effectInstance != null) {
                    int amplifier = effectInstance.getAmplifier();
                    float chance = 0.2F + amplifier * 0.2F;
                    if (RANDOM.nextFloat() < chance) {
                        float originalDamage = amount;
                        float finalDamage = originalDamage * 2.0F;
                        level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.5F);
                        if (attacker instanceof Player player) {
                            player.crit(target);
                        } else {
                            for (int i = 0; i < 5; i++) {
                                double x = target.getX() + RANDOM.nextDouble() * 2.0 - 1.0;
                                double y = target.getY() + target.getBbHeight() / 2.0F;
                                double z = target.getZ() + RANDOM.nextDouble() * 2.0 - 1.0;
                                level.addParticle(ParticleTypes.CRIT, x, y, z, 0.0, -0.1, 0.0);
                            }
                        }

                        return finalDamage;
                    }
                }
            }
        }

        return amount;
    }
}
