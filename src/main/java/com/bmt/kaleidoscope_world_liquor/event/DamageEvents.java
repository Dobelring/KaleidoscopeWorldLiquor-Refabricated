package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import java.util.Objects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 原 Forge 版 @EventBusSubscriber 的 LivingHurtEvent 监听（龙舌兰减伤：
 * 单次受到的伤害不超过最大生命 × (0.4 - 0.05 * amplifier)，下限 5%）。
 * <p>
 * Fabric 的 ServerLivingEntityEvents.ALLOW_DAMAGE 只能放行/取消、不能改数值，
 * 本类改由 mixin/LivingEntityDamageMixin 在 LivingEntity#actuallyHurt 的
 * getDamageAfterMagicAbsorb 之后（护甲+魔咒结算之后、吸收之前，与 Forge 的
 * LivingHurtEvent 触发点对应）回调 {@link #onLivingHurt}。
 */
public class DamageEvents {
    public DamageEvents() {
    }

    /**
     * 本类逻辑是 mixin 驱动、无 Fabric 事件可注册；保留空入口以维持主类
     * “所有事件类统一调用 register()”的约定（调用与否均不影响行为）。
     */
    public static void register() {
    }

    /** 原 onLivingHurt(LivingHurtEvent)。 */
    public static float onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        if (entity.hasEffect(ModEffects.TEQUILA_EFFECT)) {
            int amplifier = Objects.requireNonNull(entity.getEffect(ModEffects.TEQUILA_EFFECT)).getAmplifier();
            float maxDamagePercent = 0.4F - amplifier * 0.05F;
            if (maxDamagePercent < 0.05F) {
                maxDamagePercent = 0.05F;
            }

            float maxHealth = entity.getMaxHealth();
            float maxAllowedDamage = maxHealth * maxDamagePercent;
            if (amount > maxAllowedDamage) {
                amount = maxAllowedDamage;
            }
        }

        return amount;
    }
}
