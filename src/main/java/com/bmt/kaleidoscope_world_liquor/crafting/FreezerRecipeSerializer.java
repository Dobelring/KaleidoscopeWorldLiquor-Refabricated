package com.bmt.kaleidoscope_world_liquor.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FreezerRecipeSerializer implements RecipeSerializer<FreezerRecipe> {
   public static final int MAX_INGREDIENTS = 4;
   public static final MapCodec<FreezerRecipe> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(recipe -> recipe.getInputFluid().getFluid()),
            Codec.INT.optionalFieldOf("fluid_amount", 1000).forGetter(recipe -> recipe.getInputFluid().getAmount()),
            Ingredient.CODEC
               .listOf()
               .xmap(list -> {
                  NonNullList<Ingredient> nonnull = NonNullList.withSize(4, Ingredient.EMPTY);

                  for (int i = 0; i < Math.min(list.size(), 4); i++) {
                     nonnull.set(i, (Ingredient)list.get(i));
                  }

                  return nonnull;
               }, nonnull -> nonnull.stream().filter(i -> !i.isEmpty()).toList())
               .optionalFieldOf("ingredients", NonNullList.withSize(4, Ingredient.EMPTY))
               .forGetter(FreezerRecipe::getInputIngredients),
            ItemStack.CODEC.fieldOf("result").forGetter(FreezerRecipe::getResultItem),
            Codec.INT.optionalFieldOf("craft_time", 200).forGetter(FreezerRecipe::getCraftTime),
            ResourceLocation.CODEC.fieldOf("texture").forGetter(FreezerRecipe::getResultTexture),
            Ingredient.CODEC.optionalFieldOf("extract_condition", Ingredient.EMPTY).forGetter(FreezerRecipe::getExtractCondition)
         )
         .apply(
            instance,
            (fluid, fluidAmount, ingredients, resultItem, craftTime, resultTexture, extractCondition) -> new FreezerRecipe(
               new FluidStack(fluid, fluidAmount), ingredients, resultItem, craftTime, resultTexture, extractCondition
            )
         )
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, FreezerRecipe> STREAM_CODEC = StreamCodec.of(
      FreezerRecipeSerializer::toNetwork, FreezerRecipeSerializer::fromNetwork
   );
   private static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_ITEM_STACK = StreamCodec.of((buf, stack) -> {
      buf.writeBoolean(!stack.isEmpty());
      if (!stack.isEmpty()) {
         ItemStack.STREAM_CODEC.encode(buf, stack);
      }
   }, buf -> buf.readBoolean() ? (ItemStack)ItemStack.STREAM_CODEC.decode(buf) : ItemStack.EMPTY);

   @NotNull
   public MapCodec<FreezerRecipe> codec() {
      return CODEC;
   }

   @NotNull
   public StreamCodec<RegistryFriendlyByteBuf, FreezerRecipe> streamCodec() {
      return STREAM_CODEC;
   }

   public static FreezerRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf pBuffer) {
      Fluid inputFluid = BuiltInRegistries.FLUID.get(pBuffer.readResourceLocation());
      int fluidAmount = pBuffer.readInt();
      int ingredientSize = Math.min(4, pBuffer.readVarInt());
      NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);

      for (int i = 0; i < ingredientSize; i++) {
         ingredients.set(i, (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer));
      }

      ItemStack resultItem = (ItemStack)OPTIONAL_ITEM_STACK.decode(pBuffer);
      int craftTime = pBuffer.readInt();
      ResourceLocation resultTexture = pBuffer.readResourceLocation();
      Ingredient extractCondition = (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer);
      return new FreezerRecipe(new FluidStack(inputFluid, fluidAmount), ingredients, resultItem, craftTime, resultTexture, extractCondition);
   }

   public static void toNetwork(@NotNull RegistryFriendlyByteBuf pBuffer, FreezerRecipe pRecipe) {
      pBuffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(pRecipe.getInputFluid().getFluid()));
      pBuffer.writeInt(pRecipe.getInputFluid().getAmount());
      List<Ingredient> nonEmptyIngredients = pRecipe.getInputIngredients().stream().filter(i -> !i.isEmpty()).toList();
      pBuffer.writeVarInt(nonEmptyIngredients.size());

      for (Ingredient ingredient : nonEmptyIngredients) {
         Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, ingredient);
      }

      OPTIONAL_ITEM_STACK.encode(pBuffer, pRecipe.getResultItem());
      pBuffer.writeInt(pRecipe.getCraftTime());
      pBuffer.writeResourceLocation(pRecipe.getResultTexture());
      Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, pRecipe.getExtractCondition());
   }
}
