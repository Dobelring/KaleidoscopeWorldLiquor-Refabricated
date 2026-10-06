package com.bmt.kaleidoscope_world_liquor.init.smc;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * smc:elbow_strike 效果（手肘击）。
 * 官方 1.1.11：改声明式 addAttributeModifier（击退系数 3.0*(amp+1) → 固定 +1.0）
 * 并恢复逐 tick（shouldApplyEffectTickThisTick=true）；
 * 属性修饰符 API 照 26.x（Holder 属性 + Identifier + ADD_VALUE，构造期注册）。
 */
public class SMCEffect extends MobEffect {
    public static final String SMC_MODID = "smc";
    private static final Identifier KNOCKBACK_ID = Identifier.fromNamespaceAndPath("smc", "elbow_strike_knockback");

    public SMCEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF9300);
        // 官方语义：击退加成固定 +1.0（原 3.0*(amp+1) 瞬态修饰符）
        this.addAttributeModifier(Attributes.ATTACK_KNOCKBACK, KNOCKBACK_ID, 1.0, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 官方 1.1.11：恢复 tick（原实现为 false）
        return true;
    }
}
