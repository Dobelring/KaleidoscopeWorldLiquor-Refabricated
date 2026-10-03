package com.bmt.kaleidoscope_world_liquor.crafting;

import com.bmt.kaleidoscope_world_liquor.init.ModRecipes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class FreezerRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final FluidVariant inputFluid;
    private final int fluidAmount;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack resultItem;
    private final int craftTime;
    private final ResourceLocation resultTexture;
    private final Ingredient extractIngredient;
    public static final FreezerRecipe EMPTY = new FreezerRecipe(
        new ResourceLocation("kaleidoscope_world_liquor", "empty"),
        FluidVariant.blank(),
        0,
        NonNullList.withSize(4, Ingredient.EMPTY),
        ItemStack.EMPTY,
        0,
        null,
        Ingredient.EMPTY
    );

    // 原 Forge 版第二个参数为 FluidStack（fluid + amount 一体）；Fabric 无 FluidStack，
    // 按裁定改为 FluidVariant + 独立 fluidAmount 字段（matches 的 amount 参数即原 fluid.getAmount()）。
    public FreezerRecipe(
        ResourceLocation id,
        FluidVariant inputFluid,
        int fluidAmount,
        NonNullList<Ingredient> ingredients,
        ItemStack resultItem,
        int craftTime,
        ResourceLocation resultTexture,
        Ingredient extractIngredient
    ) {
        this.id = id;
        this.inputFluid = inputFluid;
        this.fluidAmount = fluidAmount;
        this.ingredients = ingredients;
        this.resultItem = resultItem;
        this.craftTime = craftTime;
        this.resultTexture = resultTexture;
        this.extractIngredient = extractIngredient;
    }

    public boolean matches(FluidVariant fluid, long amount, NonNullList<ItemStack> items, Level level) {
        if (fluid.getFluid().isSame(this.inputFluid.getFluid()) && amount >= this.fluidAmount) {
            boolean[] used = new boolean[items.size()];

            for (Ingredient ingredient : this.ingredients) {
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
        return this.extractIngredient.isEmpty() ? true : this.extractIngredient.test(heldItem);
    }

    @Deprecated
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pAccess) {
        return this.resultItem.copy();
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public ItemStack getResultItem(RegistryAccess pAccess) {
        return this.resultItem;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FREEZER_SERIALIZER;
    }

    public RecipeType<?> getType() {
        return ModRecipes.FREEZER_TYPE;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public FluidVariant getInputFluid() {
        return this.inputFluid;
    }

    public int getFluidAmount() {
        return this.fluidAmount;
    }

    public ItemStack getResultItem() {
        return this.resultItem;
    }

    public int getCraftTime() {
        return this.craftTime;
    }

    public ResourceLocation getResultTexture() {
        return this.resultTexture;
    }

    public Ingredient getExtractIngredient() {
        return this.extractIngredient;
    }

    public ItemStack getExtractDisplayStack() {
        if (this.extractIngredient.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack[] items = this.extractIngredient.getItems();
            return items.length > 0 ? items[0] : ItemStack.EMPTY;
        }
    }
}
