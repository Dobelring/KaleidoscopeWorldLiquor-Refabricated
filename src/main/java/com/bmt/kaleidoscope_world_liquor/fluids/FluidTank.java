package com.bmt.kaleidoscope_world_liquor.fluids;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Simple fluid tank replacing NeoForge FluidTank for the Fabric port.
 */
public class FluidTank {
   private final int capacity;
   private Fluid fluid = Fluids.EMPTY;
   private int amount;

   public FluidTank(int capacity) {
      this.capacity = capacity;
   }

   public int getCapacity() {
      return this.capacity;
   }

   public FluidStack getFluid() {
      return this.amount <= 0 ? FluidStack.EMPTY : new FluidStack(this.fluid, this.amount);
   }

   public boolean isEmpty() {
      return this.amount <= 0 || this.fluid == Fluids.EMPTY;
   }

   public int getFluidAmount() {
      return this.amount;
   }

   public FluidStack drain(int maxDrain, boolean simulate) {
      if (maxDrain <= 0 || this.isEmpty()) {
         return FluidStack.EMPTY;
      }

      int drained = Math.min(this.amount, maxDrain);
      FluidStack result = new FluidStack(this.fluid, drained);
      if (!simulate) {
         this.amount -= drained;
         if (this.amount <= 0) {
            this.fluid = Fluids.EMPTY;
         }

         this.onContentsChanged();
      }

      return result;
   }

   public int fill(FluidStack resource, boolean simulate) {
      if (resource == null || resource.isEmpty()) {
         return 0;
      }

      if (!this.isEmpty() && !this.fluid.isSame(resource.getFluid())) {
         return 0;
      }

      int filled = Math.min(resource.getAmount(), this.capacity - this.amount);
      if (filled <= 0) {
         return 0;
      }

      if (!simulate) {
         this.fluid = resource.getFluid();
         this.amount += filled;
         this.onContentsChanged();
      }

      return filled;
   }

   protected void onContentsChanged() {
   }

   public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag tag) {
      if (this.isEmpty()) {
         tag.putString("Fluid", "");
      } else {
         tag.putString("Fluid", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(this.fluid).toString());
      }

      tag.putInt("Amount", this.amount);
      return tag;
   }

   public void readFromNBT(HolderLookup.Provider registries, CompoundTag tag) {
      String fluidId = tag.getString("Fluid");
      if (fluidId.isEmpty()) {
         this.fluid = Fluids.EMPTY;
      } else {
         Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(net.minecraft.resources.ResourceLocation.parse(fluidId));
         this.fluid = fluid == null ? Fluids.EMPTY : fluid;
      }

      this.amount = tag.getInt("Amount");
   }
}
