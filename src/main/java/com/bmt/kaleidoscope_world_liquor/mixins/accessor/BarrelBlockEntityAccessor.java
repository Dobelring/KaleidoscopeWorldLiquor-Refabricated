package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({BarrelBlockEntity.class})
public interface BarrelBlockEntityAccessor {
   @Accessor("brewLevel")
   void setBrewLevel(int var1);

   @Accessor("brewTime")
   void setBrewTime(int var1);

   @Invoker("getBrewTimeForLevel")
   int invokeGetBrewTimeForLevel();
}
