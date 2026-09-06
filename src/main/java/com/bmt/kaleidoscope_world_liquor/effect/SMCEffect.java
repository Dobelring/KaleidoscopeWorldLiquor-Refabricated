package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * smc:elbow_strike（肘击）效果，由本模组在 smc 模组缺失时注册，
 * 供冰红茶 datamap（drink_effect/ice_tea.json）引用。
 */
public class SMCEffect extends MobEffect {
   private static final ResourceLocation ELBOW_STRIKE_KNOCKBACK_ID = ResourceLocation.fromNamespaceAndPath(
      "kaleidoscope_world_liquor", "elbow_strike_knockback"
   );

   public SMCEffect() {
      super(MobEffectCategory.BENEFICIAL, 16762624);
   }

   public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
      super.addAttributeModifiers(attributes, amplifier);
      double knockbackBonus = 3.0 * (amplifier + 1);
      attributes.getInstance(Attributes.ATTACK_KNOCKBACK)
         .addTransientModifier(new AttributeModifier(ELBOW_STRIKE_KNOCKBACK_ID, knockbackBonus, AttributeModifier.Operation.ADD_VALUE));
   }

   public void removeAttributeModifiers(AttributeMap attributes) {
      super.removeAttributeModifiers(attributes);
      attributes.getInstance(Attributes.ATTACK_KNOCKBACK).removeModifier(ELBOW_STRIKE_KNOCKBACK_ID);
   }
}
