package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({LivingEntity.class})
public interface LivingEntityAccessor {
    @Accessor("jumping")
    boolean isJumping();

    @Accessor("jumping")
    void setJumping(boolean var1);

    @Invoker("calculateFallDamage")
    int invokeCalculateFallDamage(float var1, float var2);
}
