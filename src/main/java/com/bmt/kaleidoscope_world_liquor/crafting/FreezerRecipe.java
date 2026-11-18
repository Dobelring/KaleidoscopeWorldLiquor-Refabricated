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
   // 官方 1.2.0：extract_condition 更名 carrier（取产物需消耗的容器，如碗）
   private final Ingredient carrier;
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
      Ingredient carrier
   ) {
      this.inputFluid = inputFluid;
      this.inputIngredients = inputIngredients;
      this.resultItem = resultItem;
      this.craftTime = craftTime;
      this.resultTexture = resultTexture;
      this.carrier = carrier;
   }

   public boolean isEmpty() {
      return this == EMPTY || this.resultItem.isEmpty();
   }

   public boolean matches(FluidStack fluid, NonNullList<ItemStack> items, Level level) {
      if (fluid.getFluid().isSame(this.inputFluid.getFluid()) && fluid.getAmount() >= this.inputFluid.getAmount()) {
         // 官方 1.2.0：数量相等 + 双射匹配（NeoForge RecipeMatcher 语义，回溯实现）
         java.util.List<Ingredient> required = new java.util.ArrayList<>(this.inputIngredients.size());

         for (Ingredient ingredient : this.inputIngredients) {
            if (!ingredient.isEmpty()) {
               required.add(ingredient);
            }
         }

         java.util.List<ItemStack> present = new java.util.ArrayList<>(items.size());

         for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
               present.add(stack);
            }
         }

         return present.size() == required.size() && (required.isEmpty() || findMatches(present, required));
      } else {
         return false;
      }
   }

   private static boolean findMatches(java.util.List<ItemStack> present, java.util.List<Ingredient> required) {
      boolean[] used = new boolean[required.size()];
      return matchAt(present, required, 0, used);
   }

   private static boolean matchAt(java.util.List<ItemStack> present, java.util.List<Ingredient> required, int index, boolean[] used) {
      if (index == present.size()) {
         return true;
      }

      for (int r = 0; r < required.size(); r++) {
         if (!used[r] && required.get(r).test(present.get(index))) {
            used[r] = true;
            if (matchAt(present, required, index + 1, used)) {
               return true;
            }

            used[r] = false;
         }
      }

      return false;
   }

   public boolean canExtract(ItemStack heldItem) {
      return this.carrier.isEmpty() ? true : this.carrier.test(heldItem);
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

   public Ingredient getCarrier() {
      return this.carrier;
   }
}
