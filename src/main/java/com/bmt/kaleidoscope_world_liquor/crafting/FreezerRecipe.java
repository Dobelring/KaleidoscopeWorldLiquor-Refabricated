package com.bmt.kaleidoscope_world_liquor.crafting;

import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;

public class FreezerRecipe implements Recipe<RecipeInput> {
   private final FluidStack inputFluid;
   private final NonNullList<Ingredient> inputIngredients;
   private final ItemStack resultItem;
   private final int craftTime;
   private final ResourceLocation resultTexture;
   private final Ingredient extractCondition;
   public static final FreezerRecipe EMPTY = new FreezerRecipe(
      FluidStack.EMPTY,
      NonNullList.withSize(4, Ingredient.EMPTY),
      ItemStack.EMPTY,
      0,
      ResourceLocation.parse("minecraft:textures/block/air.png"),
      Ingredient.EMPTY
   );

   public FreezerRecipe(
      FluidStack inputFluid,
      NonNullList<Ingredient> inputIngredients,
      ItemStack resultItem,
      int craftTime,
      ResourceLocation resultTexture,
      Ingredient extractCondition
   ) {
      this.inputFluid = inputFluid;
      this.inputIngredients = inputIngredients;
      this.resultItem = resultItem;
      this.craftTime = craftTime;
      this.resultTexture = resultTexture;
      this.extractCondition = extractCondition;
   }

   public boolean isEmpty() {
      return this == EMPTY || this.resultItem.isEmpty();
   }

   public boolean matches(FluidStack fluid, NonNullList<ItemStack> items, Level level) {
      if (fluid.getFluid().isSame(this.inputFluid.getFluid()) && fluid.getAmount() >= this.inputFluid.getAmount()) {
         boolean[] used = new boolean[items.size()];

         for (Ingredient ingredient : this.inputIngredients) {
            if (!ingredient.isEmpty()) {
               boolean matched = false;

               for (int i = 0; i < items.size(); i++) {
                  if (!used[i] && ingredient.test((ItemStack)items.get(i))) {
                     used[i] = true;
                     matched = true;
                     break;
                  }
               }

               if (!matched) {
                  return false;
               }
            }
         }

         for (int ix = 0; ix < items.size(); ix++) {
            if (!((ItemStack)items.get(ix)).isEmpty() && !used[ix]) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean canExtract(ItemStack heldItem) {
      return this.extractCondition.isEmpty() ? true : this.extractCondition.test(heldItem);
   }

   @Deprecated
   public boolean matches(RecipeInput pInput, Level pLevel) {
      return false;
   }

   public ItemStack assemble(RecipeInput pInput, Provider pRegistries) {
      return this.resultItem.copy();
   }

   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public ItemStack getResultItem(Provider pRegistries) {
      return this.resultItem;
   }

   public ItemStack getResultItem() {
      return this.resultItem;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.inputIngredients;
   }

   @Deprecated
   public ResourceLocation getId() {
      return ResourceLocation.parse("kaleidoscope_world_liquor:empty");
   }

   public RecipeSerializer<?> getSerializer() {
      return ModRecipes.FREEZER_SERIALIZER;
   }

   public RecipeType<?> getType() {
      return ModRecipes.FREEZER_TYPE;
   }

   public FluidStack getInputFluid() {
      return this.inputFluid;
   }

   public NonNullList<Ingredient> getInputIngredients() {
      return this.inputIngredients;
   }

   public int getCraftTime() {
      return this.craftTime;
   }

   public ResourceLocation getResultTexture() {
      return this.resultTexture;
   }

   public Ingredient getExtractCondition() {
      return this.extractCondition;
   }
}
