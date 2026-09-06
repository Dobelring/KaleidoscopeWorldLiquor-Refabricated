package com.bmt.kaleidoscope_world_liquor.fluids;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Lightweight fluid+amount holder replacing NeoForge FluidStack for the Fabric port.
 */
public class FluidStack {
   public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);
   private final Fluid fluid;
   private final int amount;

   public FluidStack(Fluid fluid, int amount) {
      this.fluid = fluid;
      this.amount = amount;
   }

   public Fluid getFluid() {
      return this.fluid;
   }

   public int getAmount() {
      return this.amount;
   }

   public boolean isEmpty() {
      return this.amount <= 0 || this.fluid == Fluids.EMPTY;
   }

   public CompoundTag save(HolderLookup.Provider registries, CompoundTag tag) {
      tag.putString("Fluid", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(this.fluid).toString());
      tag.putInt("Amount", this.amount);
      return tag;
   }

   public static FluidStack load(HolderLookup.Provider registries, CompoundTag tag) {
      Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(net.minecraft.resources.ResourceLocation.parse(tag.getString("Fluid")));
      if (fluid == null) {
         fluid = Fluids.EMPTY;
      }

      return new FluidStack(fluid, tag.getInt("Amount"));
   }

   public static FluidStack readFromNBT(HolderLookup.Provider registries, CompoundTag tag) {
      return load(registries, tag);
   }
}
