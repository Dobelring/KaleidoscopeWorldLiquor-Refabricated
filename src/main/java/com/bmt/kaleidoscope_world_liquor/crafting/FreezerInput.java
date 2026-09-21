package com.bmt.kaleidoscope_world_liquor.crafting;

import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

/**
 * 冰柜配方输入：流体槽（Fabric transfer 存储，mB 换算比较）+ 4 个输入槽。
 */
public record FreezerInput(CustomFluidTank tank, List<ItemStack> items) implements RecipeInput {

    public boolean fluidMatches(Identifier fluidId, int amountMb) {
        if (tank.getFluidAmountMb() < amountMb) {
            return false;
        }
        return tank.getFluidVariant().getFluid().builtInRegistryHolder().key().identifier().equals(fluidId);
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
