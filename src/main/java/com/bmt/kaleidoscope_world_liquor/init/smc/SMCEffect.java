package com.bmt.kaleidoscope_world_liquor.init.smc;

import java.util.UUID;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import org.jetbrains.annotations.NotNull;

/**
 * {@code smc:elbow_strike}（肘击）效果的实现类。
 * <p>
 * 本类本身全部是原版 API（无 Forge 依赖）。注册不在本类：原 Forge 版由
 * {@code ModEffects} 的 {@code SMC_MOB_EFFECTS}（ns=smc）在
 * {@code !ModList.isLoaded("smc")} 时以 {@code SMCEffect::new} 代注册
 * （smc 已装时由 smc 模组自己提供），Fabric 版同样由 ModEffects 处理。
 */
public class SMCEffect extends MobEffect {
    private static final UUID ELBOW_STRIKE_KNOCKBACK_UUID = UUID.fromString("7f2a3d4b-9c8e-1b6f-5d7a-3e9c2b8a1d4f");

    public SMCEffect() {
        super(MobEffectCategory.BENEFICIAL, 16762624);
    }

    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        double knockbackBonus = 3.0 * (amplifier + 1);
        attributes.getInstance(Attributes.ATTACK_KNOCKBACK)
            .addTransientModifier(new AttributeModifier(ELBOW_STRIKE_KNOCKBACK_UUID, "Elbow Strike Knockback Bonus", knockbackBonus, Operation.ADDITION));
    }

    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        attributes.getInstance(Attributes.ATTACK_KNOCKBACK).removeModifier(ELBOW_STRIKE_KNOCKBACK_UUID);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
