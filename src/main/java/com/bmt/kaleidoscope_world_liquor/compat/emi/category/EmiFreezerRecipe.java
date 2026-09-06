package com.bmt.kaleidoscope_world_liquor.compat.emi.category;

import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class EmiFreezerRecipe extends BasicEmiRecipe {
   public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
      ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "freezer"),
      EmiStack.of(ModBlocks.FREEZER)
   );

   private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "textures/gui/jei/freezer.png");
   private static final int WIDTH = 180;
   private static final int HEIGHT = 150;

   private final Component freezeTimeTooltip;

   public EmiFreezerRecipe(ResourceLocation id, List<EmiIngredient> inputs, List<EmiIngredient> catalysts, List<EmiStack> outputs, Component freezeTimeTooltip) {
      super(CATEGORY, id, WIDTH, HEIGHT);
      this.inputs = inputs;
      this.catalysts = catalysts;
      this.outputs = outputs;
      this.freezeTimeTooltip = freezeTimeTooltip;
   }

   public static void register(EmiRegistry registry) {
      registry.addCategory(CATEGORY);
      registry.addWorkstation(CATEGORY, EmiStack.of(ModBlocks.FREEZER));

      registry.getRecipeManager().getAllRecipesFor(ModRecipes.FREEZER_TYPE).forEach(holder -> {
         FreezerRecipe recipe = holder.value();
         List<EmiIngredient> inputs = new java.util.ArrayList<>();

         Fluid inputFluid = recipe.getInputFluid().getFluid();
         int fluidAmount = recipe.getInputFluid().getAmount();
         Item bucketItem = inputFluid.getBucket();
         if (bucketItem != null && bucketItem != Items.AIR) {
            inputs.add(EmiStack.of(bucketItem, Math.max(1, fluidAmount / 1000)));
         } else {
            inputs.add(EmiStack.of(inputFluid, fluidAmount));
         }

         for (int i = 0; i < 4; i++) {
            Ingredient ingredient = recipe.getInputIngredients().get(i);
            if (!ingredient.isEmpty()) {
               inputs.add(EmiIngredient.of(ingredient));
            }
         }

         List<EmiStack> outputs = List.of(EmiStack.of(recipe.getResultItem()));

         List<EmiIngredient> catalysts = new java.util.ArrayList<>();
         Ingredient extractCondition = recipe.getExtractCondition();
         if (!extractCondition.isEmpty()) {
            catalysts.add(EmiIngredient.of(extractCondition));
         }

         int seconds = recipe.getCraftTime() / 20;
         Component freezeTimeTooltip = Component.translatable("jei.kaleidoscope_world_liquor.freezer.freeze_time", seconds)
            .withStyle(ChatFormatting.GRAY);

         registry.addRecipe(new EmiFreezerRecipe(holder.id(), inputs, catalysts, outputs, freezeTimeTooltip));
      });
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      widgets.addTexture(BG, 1, 1, WIDTH, HEIGHT, 0, 0);

      int offsetY = 0;
      for (EmiIngredient input : inputs) {
         widgets.addSlot(input, 10, 9 + offsetY);
         offsetY += 18;
      }

      if (!outputs.isEmpty()) {
         widgets.addSlot(outputs.getFirst(), 148, 82)
            .recipeContext(this)
            .large(true)
            .appendTooltip(freezeTimeTooltip);
      }

      if (!catalysts.isEmpty()) {
         widgets.addSlot(catalysts.getFirst(), 115, 100);
      }
   }
}
