package com.bmt.kaleidoscope_world_liquor.init.smc;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * smc:elbow_strike 效果（手肘击）。
 * <p>
 * 官方 1.1.11 改为声明式：构造期
 * {@code addAttributeModifier(ATTACK_KNOCKBACK, id, 1.0, ADD_VALUE)}
 * ——击退系数从 {@code 3.0*(amp+1)} 降为固定 1.0；同时恢复每 tick 应用
 * （{@code shouldApplyEffectTickThisTick = true}）。
 * <p>
 * 26.x 差异：签名是 {@code addAttributeModifier(Holder<Attribute>, Identifier, double, Operation)}；
 * 修饰符 id 按官方用 {@code kaleidoscope_world_liquor} 命名空间（非 smc），
 * 它只是修饰符键（不进任何 registry），换命名空间无副作用。
 */
public class SMCEffect extends MobEffect {
    public static final String SMC_MODID = "smc";
    private static final Identifier ELBOW_STRIKE_KNOCKBACK_ID = Identifier.fromNamespaceAndPath(
            "kaleidoscope_world_liquor", "elbow_strike_knockback");

    public SMCEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF9300);
        this.addAttributeModifier(Attributes.ATTACK_KNOCKBACK, ELBOW_STRIKE_KNOCKBACK_ID, 1.0,
                AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
