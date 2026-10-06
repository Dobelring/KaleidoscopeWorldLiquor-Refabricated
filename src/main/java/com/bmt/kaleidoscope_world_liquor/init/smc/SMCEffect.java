package com.bmt.kaleidoscope_world_liquor.init.smc;

import java.util.UUID;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * {@code smc:elbow_strike}（肘击）效果的实现类。
 * <p>
 * 本类本身全部是原版 API（无 Forge 依赖）。注册不在本类：原 Forge 版由
 * {@code ModEffects} 的 {@code SMC_MOB_EFFECTS}（ns=smc）在
 * {@code !ModList.isLoaded("smc")} 时以 {@code SMCEffect::new} 代注册
 * （smc 已装时由 smc 模组自己提供），Fabric 版同样由 ModEffects 处理。
 * <p>
 * 官方 1.1.12：击退从手写瞬态修饰（3.0×(amplifier+1)）改为声明式修饰 1.0
 * （1.20.1 原版按 (amplifier+1) 缩放应用），并恢复效果 tick（isDurationEffectTick=true）。
 */
public class SMCEffect extends MobEffect {
    // 1.20.1 MobEffect#addAttributeModifier 的 id 参数是 String（官方 forge 版同款字面量）
    private static final String ELBOW_STRIKE_KNOCKBACK_UUID = "7107DE5E-1472-9872-6231-639485149237";

    public SMCEffect() {
        super(MobEffectCategory.BENEFICIAL, 16762624);
        this.addAttributeModifier(Attributes.ATTACK_KNOCKBACK, ELBOW_STRIKE_KNOCKBACK_UUID, 1.0, Operation.ADDITION);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
