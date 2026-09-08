package com.bmt.kaleidoscope_world_liquor.compat.jei;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.crafting.FreezerRecipe;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * JEI 冰柜分类（照 cookery PotRecipeCategory 范式 + 流体输入槽）。
 * 原版 1.20.1 FreezerCategory 同款布局：流体左、4 输入中、碗、右输出。
 */
public class FreezerCategory implements IRecipeCategory<RecipeHolder<FreezerRecipe>> {
    public static final IRecipeHolderType<FreezerRecipe> TYPE = IRecipeType.create(ModRecipes.FREEZER_RECIPE_TYPE);
    private static final Identifier BG = Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "textures/gui/jei/freezer.png");
    private static final Component TITLE = Component.translatable("block.kaleidoscope_world_liquor.freezer");
    public static final int WIDTH = 180;
    public static final int HEIGHT = 150;

    private final IDrawable bgDraw;
    private final IDrawable slotDraw;
    private final IDrawable iconDraw;

    public FreezerCategory(IGuiHelper guiHelper) {
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.slotDraw = guiHelper.getSlotDrawable();
        this.iconDraw = guiHelper.createDrawableItemLike(ModItems.FREEZER);
    }

    /** 按流体反查满桶物品（与 FreezerBlock.findBucketFor 同逻辑） */
    private static ItemStack findBucketFor(net.minecraft.world.level.material.Fluid fluid) {
        if (fluid == com.bmt.kaleidoscope_world_liquor.fluids.MilkFluid.STILL) {
            return new ItemStack(net.minecraft.world.item.Items.MILK_BUCKET);
        }
        if (fluid == net.minecraft.world.level.material.Fluids.WATER) {
            return new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET);
        }
        if (fluid == net.minecraft.world.level.material.Fluids.LAVA) {
            return new ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET);
        }
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            if (entry.getValue() instanceof net.minecraft.world.item.BucketItem b
                    && !(entry.getValue() instanceof net.minecraft.world.item.MobBucketItem)
                    && b.getContent() == fluid) {
                return new ItemStack(entry.getValue());
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<RecipeHolder<FreezerRecipe>> getRecipes() {
        if (Minecraft.getInstance().level == null) {
            return List.of();
        }
        return List.copyOf(Minecraft.getInstance().level.recipeAccess()
                .getSynchronizedRecipes().getAllOfType(ModRecipes.FREEZER_RECIPE_TYPE));
    }

    @Override
    public void draw(@NotNull RecipeHolder<FreezerRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.bgDraw.draw(guiGraphics);
        Component time = Component.translatable("jei.kaleidoscope_world_liquor.freezer.freeze_time",
                recipe.value().craftTime() / 20);
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence sequence = time.getVisualOrderText();
        guiGraphics.text(font, sequence, WIDTH / 2 - font.width(sequence) / 2, 132, 0x555555, false);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<FreezerRecipe> recipe, @NotNull IFocusGroup focuses) {
        FreezerRecipe r = recipe.value();
        // 布局照 1.20.1 原版 FreezerCategory：流体(10,9)、物品 30,9 起每 18px 横排、
        // 碗 CATALYST(115,100)、输出(152,86)（此前自创坐标与背景贴图错位）
        var fluid = BuiltInRegistries.FLUID.get(r.fluid()).orElse(null);
        if (fluid != null) {
            // 1.20.1 原版优先显示满桶物品图标（getBucket()），无桶才画流体
            ItemStack bucket = findBucketFor(fluid.value());
            if (!bucket.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, 10, 9).addItemStack(bucket);
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, 10, 9).addFluidStack(fluid.value(), r.fluidAmount());
            }
        }
        int offsetX = 0;
        for (var ingredient : r.ingredients()) {
            if (!ingredient.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, 30 + offsetX, 9).addIngredients(ingredient);
                offsetX += 18;
            }
        }
        r.extractIngredient().ifPresent(bowl ->
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 115, 100).addIngredients(bowl));
        ItemStack output = r.result().create();
        int seconds = r.craftTime() / 20;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 152, 86).addItemStack(output)
                .addRichTooltipCallback((recipeSlotView, tooltip) ->
                        tooltip.add(Component.translatable("jei.kaleidoscope_world_liquor.freezer.freeze_time", seconds)));
    }

    @Override
    public @NotNull IRecipeType<RecipeHolder<FreezerRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return TITLE;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    @Nullable
    public IDrawable getIcon() {
        return iconDraw;
    }
}
