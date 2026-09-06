package com.bmt.kaleidoscope_world_liquor.compat.jei;

import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FreezerCategory implements IRecipeCategory<FreezerRecipe> {
   public static final RecipeType<FreezerRecipe> TYPE = RecipeType.create("kaleidoscope_world_liquor", "freezer", FreezerRecipe.class);
   private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "textures/gui/jei/freezer.png");
   private static final Component TITLE = Component.translatable("block.kaleidoscope_world_liquor.freezer");
   public static final int WIDTH = 180;
   public static final int HEIGHT = 150;
   private final IDrawable bgDraw;
   private final IDrawable iconDraw;

   public FreezerCategory(IGuiHelper guiHelper) {
      this.bgDraw = guiHelper.createDrawable(BG, 0, 0, 180, 150);
      this.iconDraw = guiHelper.createDrawableItemLike(ModBlocks.FREEZER.asItem());
   }

   public static List<FreezerRecipe> getRecipes() {
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         return List.of();
      } else {
         List<FreezerRecipe> recipes = Lists.newArrayList();
         recipes.addAll(
            level.getRecipeManager().getAllRecipesFor(ModRecipes.FREEZER_TYPE)
               .stream()
               .map(RecipeHolder::value)
               .toList()
         );
         return recipes;
      }
   }

   public void draw(@NotNull FreezerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.bgDraw.draw(guiGraphics);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, FreezerRecipe recipe, IFocusGroup focuses) {
      Fluid inputFluid = recipe.getInputFluid().getFluid();
      int fluidAmount = recipe.getInputFluid().getAmount();
      IRecipeSlotBuilder fluidSlot = builder.addSlot(RecipeIngredientRole.INPUT, 10, 9);
      Item bucketItem = inputFluid.getBucket();
      if (bucketItem != null && bucketItem != Items.AIR) {
         int fluidBuckets = fluidAmount / 1000;
         fluidSlot.addItemStack(new ItemStack(bucketItem, Math.max(1, fluidBuckets)));
      } else {
         fluidSlot.addFluidStack(inputFluid, fluidAmount);
      }

      int offsetX = 0;

      for (int i = 0; i < 4; i++) {
         Ingredient ingredient = (Ingredient)recipe.getInputIngredients().get(i);
         if (!ingredient.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 30 + offsetX, 9).addItemStacks(Arrays.asList(ingredient.getItems()));
            offsetX += 18;
         }
      }

      Ingredient extractCondition = recipe.getExtractCondition();
      if (!extractCondition.isEmpty()) {
         builder.addSlot(RecipeIngredientRole.CATALYST, 115, 100).addItemStacks(Arrays.asList(extractCondition.getItems()));
      }

      int seconds = recipe.getCraftTime() / 20;
      ItemStack outputStack = recipe.getResultItem(Minecraft.getInstance().level.registryAccess()).copy();
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.OUTPUT, 152, 86).addItemStack(outputStack))
         .addTooltipCallback(
            (recipeSlotView, tooltip) -> tooltip.add(Component.translatable("jei.kaleidoscope_world_liquor.freezer.freeze_time", new Object[]{seconds}))
         );
   }

   @NotNull
   public RecipeType<FreezerRecipe> getRecipeType() {
      return TYPE;
   }

   @NotNull
   public Component getTitle() {
      return TITLE;
   }

   public int getWidth() {
      return 180;
   }

   public int getHeight() {
      return 150;
   }

   @Nullable
   public IDrawable getIcon() {
      return this.iconDraw;
   }
}
