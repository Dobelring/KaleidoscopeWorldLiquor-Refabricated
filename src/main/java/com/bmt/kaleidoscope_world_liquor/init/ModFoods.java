package com.bmt.kaleidoscope_world_liquor.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * twilight 联动食物的食物属性。
 * 1.20.1 效果 -> 1.21.11（经 srg2named.tsv 核对原版字段）：
 * liangshan: fire_resistance + speed；kita: luck + saturation；
 * pochi: regeneration + luck；magic_corner: haste。
 */
public final class ModFoods {
    private ModFoods() {
    }

    public static final FoodProperties LIANGSHAN_ICE_CONE = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(0.4F)
            .alwaysEdible()
            .build();
    public static final Consumable LIANGSHAN_ICE_CONE_CONSUMABLE = drinkEffects(
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600),
            new MobEffectInstance(MobEffects.SPEED, 9600));

    public static final FoodProperties KITA_STUFFED_CRISP = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.5F)
            .alwaysEdible()
            .build();
    public static final Consumable KITA_STUFFED_CRISP_CONSUMABLE = drinkEffects(
            new MobEffectInstance(MobEffects.LUCK, 9600),
            new MobEffectInstance(MobEffects.SATURATION, 9600));

    public static final FoodProperties POCHI_PUDDING = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.7F)
            .alwaysEdible()
            .build();
    public static final Consumable POCHI_PUDDING_CONSUMABLE = drinkEffects(
            new MobEffectInstance(MobEffects.REGENERATION, 9600),
            new MobEffectInstance(MobEffects.LUCK, 9600));

    public static final FoodProperties MAGIC_CRISPY_CORNER = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.3F)
            .alwaysEdible()
            .build();
    public static final Consumable MAGIC_CRISPY_CORNER_CONSUMABLE = drinkEffects(
            new MobEffectInstance(MobEffects.HASTE, 9600));

    private static Consumable drinkEffects(MobEffectInstance... effects) {
        Consumable.Builder builder = Consumables.defaultFood();
        for (MobEffectInstance effect : effects) {
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(effect, 1.0F));
        }
        return builder.build();
    }
}
