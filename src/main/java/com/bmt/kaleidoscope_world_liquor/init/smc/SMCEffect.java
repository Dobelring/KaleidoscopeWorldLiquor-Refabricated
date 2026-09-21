package com.bmt.kaleidoscope_world_liquor.init.smc;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

/**
 * smc:elbow_strike 效果（手肘击）：击退加成 3.0*(amp+1)。
 * 属性修饰符 API 照 1.21.11（Identifier + ADD_VALUE）。
 */
public class SMCEffect extends MobEffect {
    public static final String SMC_MODID = "smc";
    private static final Identifier KNOCKBACK_ID = Identifier.fromNamespaceAndPath("smc", "elbow_strike_knockback");

    public SMCEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF9300);
    }

    // 原版 SMCEffect：ATTACK_KNOCKBACK（攻击击退）+3.0*(amp+1)，transient 修饰符
    @Override
    public void addAttributeModifiers(@NotNull AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(attributes, amplifier);
        double knockbackBonus = 3.0 * (amplifier + 1);
        AttributeInstance attribute = attributes.getInstance(Attributes.ATTACK_KNOCKBACK);
        if (attribute != null) {
            attribute.addTransientModifier(new AttributeModifier(KNOCKBACK_ID, knockbackBonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        AttributeInstance attribute = attributes.getInstance(Attributes.ATTACK_KNOCKBACK);
        if (attribute != null) {
            attribute.removeModifier(KNOCKBACK_ID);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
