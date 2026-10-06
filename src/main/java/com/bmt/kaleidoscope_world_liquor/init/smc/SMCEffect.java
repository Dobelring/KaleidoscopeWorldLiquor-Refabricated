package com.bmt.kaleidoscope_world_liquor.init.smc;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * smc:elbow_strike 效果（手肘击）。
 * <p>官方 1.1.11 改声明式：构造期 {@code addAttributeModifier(ATTACK_KNOCKBACK, id, 1.0, ADD_VALUE)}
 * ——击退系数从 3.0*(amp+1) 降为固定 1.0；同时恢复每 tick 应用（{@code shouldApplyEffectTickThisTick=true}）。
 * <p>26.3 差异：签名是 {@code addAttributeModifier(Holder<Attribute>, Identifier, double, Operation)}。
 */
public class SMCEffect extends MobEffect {
    public static final String SMC_MODID = "smc";
    private static final Identifier KNOCKBACK_ID = Identifier.fromNamespaceAndPath(
            "kaleidoscope_world_liquor", "elbow_strike_knockback");

    public SMCEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF9300);
        this.addAttributeModifier(Attributes.ATTACK_KNOCKBACK, KNOCKBACK_ID, 1.0,
                AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
