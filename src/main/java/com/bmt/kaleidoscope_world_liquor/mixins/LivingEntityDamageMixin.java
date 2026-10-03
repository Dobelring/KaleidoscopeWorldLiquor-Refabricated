package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.effect.DoubleDamageEffect;
import com.bmt.kaleidoscope_world_liquor.event.DamageEvents;
import com.bmt.kaleidoscope_world_liquor.event.EventHandlers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 伤害改数值接线（事件批次新增，需登记进 kaleidoscope_world_liquor.mixins.json 的 mixins 数组）。
 * <p>
 * Fabric 的 ServerLivingEntityEvents.ALLOW_DAMAGE 只能放行/取消、不能改数值，而本模组有
 * 三个“改最终伤害数值”的 Forge 监听器，全部落在原版 {@code LivingEntity#actuallyHurt}
 * （护甲 → 魔咒 → 吸收 → 扣血）的结算路径上：
 * <ul>
 *   <li>LivingHurtEvent 等价点 = {@code getDamageAfterMagicAbsorb} 之后（护甲+魔咒结算完、
 *       吸收之前）→ DamageEvents.onLivingHurt（龙舌兰单次伤害上限）、
 *       EventHandlers.onLivingHurt（破势 ground_crit ×1.5）；</li>
 *   <li>LivingDamageEvent 等价点 = actuallyHurt 末尾的 setHealth 调用
 *       （吸收记账完、扣血前，且过了 zero-damage 早退，与 Forge 事件时机一致）→
 *       DoubleDamageEffect.modifyDamage（重斩 ×2，方法体由 fluid 批次提供、含官方语义的暴击音/粒子）。</li>
 * </ul>
 * 同类两个 LivingHurt 监听器在原 Forge 总线上按 jar 内类名字母序（DamageEvents 在
 * EventHandlers 之前）执行，这里按该顺序串联。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    /** actuallyHurt 入参 DamageSource 暂存（单线程服务端、方法体内顺序执行，供后续改数值点取用）。 */
    @Unique
    private DamageSource kaleidoscope_world_liquor$capturedSource;

    @Inject(
        method = "actuallyHurt",
        at = @At("HEAD")
    )
    private void kaleidoscope_world_liquor$captureDamageSource(DamageSource source, float amount, CallbackInfo ci) {
        this.kaleidoscope_world_liquor$capturedSource = source;
    }

    /**
     * 原 Forge LivingHurtEvent（DamageEvents#onLivingHurt、EventHandlers#onLivingHurt）。
     * 官方监听器只改数值、从不取消，故改值回调即可完整表达。
     */
    @WrapOperation(
        method = "actuallyHurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"
        )
    )
    private float kaleidoscope_world_liquor$modifyHurtAmount(LivingEntity self, DamageSource source, float amount, Operation<Float> original) {
        float mitigated = original.call(self, source, amount); // 包实例方法：call 须带接收者（启动崩溃实测：Expected [LivingEntity,DamageSource,float]）
        // 原 Forge 总线注册序（jar 条目字母序）：DamageEvents 在前、EventHandlers 在后
        // （@WrapOperation 包实例方法调用时首个参数即接收者，MixinExtras 校验要求显式声明）
        mitigated = DamageEvents.onLivingHurt(self, source, mitigated);
        mitigated = EventHandlers.onLivingHurt(self, source, mitigated);
        return mitigated;
    }

    /**
     * 原 Forge LivingDamageEvent（effect/DoubleDamageEffect 构造器注册的监听体，
     * 现为 fluid 批次抽出的 DoubleDamageEffect.modifyDamage(target, source, amount)）。
     * <p>
     * 注入点 = actuallyHurt 末尾唯一一处 setHealth 调用（原版 1.20.1 字节码实锤）：
     * 进入此处前吸收已记账（step: max(amount-absorption,0) + 两次 setAbsorptionAmount），
     * 且 zero-damage 早退（f2==0 return）已过——与 Forge 在 {@code if (f2 != 0.0F)} 块内触发
     * LivingDamageEvent 的时机一致：伤害被完全吸收时不触发判定，翻倍只作用于扣血额。
     * 实参 = getHealth() - f2，此时未扣血，getHealth() 即当前血量，可反解出 f2。
     * （已知微差：Forge 下 CombatTracker.recordDamage 也见翻倍值，这里仍记原值，纯统计口径。）
     */
    @WrapOperation(
        method = "actuallyHurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"
        )
    )
    private void kaleidoscope_world_liquor$modifyFinalDamage(LivingEntity self, float healthAfterDamage, Operation<Void> original) {
        float finalDamage = self.getHealth() - healthAfterDamage;
        original.call(self, self.getHealth() - DoubleDamageEffect.modifyDamage(self, this.kaleidoscope_world_liquor$capturedSource, finalDamage));
    }
}
