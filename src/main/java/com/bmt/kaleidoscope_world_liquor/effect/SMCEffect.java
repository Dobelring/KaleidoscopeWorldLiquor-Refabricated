package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class SMCEffect extends MobEffect {
   private static final ResourceLocation ELBOW_STRIKE_KNOCKBACK_ID = ResourceLocation.fromNamespaceAndPath(
      "kaleidoscope_world_liquor", "elbow_strike_knockback"
   );

   public SMCEffect() {
      super(MobEffectCategory.BENEFICIAL, 16762624);
      this.addAttributeModifier(Attributes.ATTACK_KNOCKBACK, ELBOW_STRIKE_KNOCKBACK_ID, 1.0, Operation.ADD_VALUE);
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return true;
   }
}
