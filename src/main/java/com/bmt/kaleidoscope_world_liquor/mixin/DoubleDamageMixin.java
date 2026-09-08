package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

/**
 * 重斩（double_damage）：攻击者携带效果时，最终伤害（护甲+附魔减免后、
 * 吸收/扣血前）按 20%+20%/级 概率翻倍——与 1.20.1 Forge LivingDamageEvent
 * 的注入时机字节级等价。命中判定与音效/粒子在 handler 内完成。
 */
@Mixin(LivingEntity.class)
public class DoubleDamageMixin {

    @ModifyExpressionValue(
            method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"
            )
    )
    private float kaleidoscope_world_liquor$doubleDamage(float original, ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            MobEffectInstance effectInstance = attacker.getEffect(ModEffects.DOUBLE_DAMAGE);
            if (effectInstance != null && com.bmt.kaleidoscope_world_liquor.effect.DoubleDamageHelper.rollAndBroadcast(attacker, (LivingEntity) (Object) this, effectInstance.getAmplifier())) {
                return original * 2.0F;
            }
        }
        return original;
    }
}
