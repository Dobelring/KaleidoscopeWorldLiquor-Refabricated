package com.bmt.kaleidoscope_world_liquor.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;

public class ModFoods {
    public static final FoodProperties LIANGSHAN_ICE_CONE = new Builder()
        .nutrition(5)
        .saturationMod(0.4F)
        .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600), 1.0F)
        .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 9600), 1.0F)
        .alwaysEat()
        .build();
    public static final FoodProperties KITA_STUFFED_CRISP = new Builder()
        .nutrition(6)
        .saturationMod(0.5F)
        .effect(new MobEffectInstance(MobEffects.LUCK, 9600), 1.0F)
        .effect(new MobEffectInstance(MobEffects.SATURATION, 9600), 1.0F)
        .alwaysEat()
        .build();
    public static final FoodProperties POCHI_PUDDING = new Builder()
        .nutrition(8)
        .saturationMod(0.7F)
        .effect(new MobEffectInstance(MobEffects.REGENERATION, 9600), 1.0F)
        .effect(new MobEffectInstance(MobEffects.LUCK, 9600), 1.0F)
        .alwaysEat()
        .build();
    public static final FoodProperties MAGIC_CRISPY_CORNER = new Builder()
        .nutrition(4)
        .saturationMod(0.3F)
        .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 9600), 1.0F)
        .alwaysEat()
        .build();

    public ModFoods() {
    }
}
