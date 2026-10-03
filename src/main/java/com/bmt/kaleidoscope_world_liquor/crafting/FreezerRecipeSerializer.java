package com.bmt.kaleidoscope_world_liquor.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public class FreezerRecipeSerializer implements RecipeSerializer<FreezerRecipe> {
    public static final int MAX_INGREDIENTS = 4;

    public FreezerRecipeSerializer() {
    }

    @NotNull
    public FreezerRecipe fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pJson) {
        JsonArray ingredientsJson = GsonHelper.getAsJsonArray(pJson, "ingredients", new JsonArray());
        NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);

        for (int i = 0; i < ingredientsJson.size() && i < 4; i++) {
            ingredients.set(i, parseIngredientLenient(ingredientsJson.get(i)));
        }

        String fluidId = GsonHelper.getAsString(pJson, "fluid");
        ResourceLocation fluidLocation = new ResourceLocation(fluidId);
        // 原 Forge 写法 ForgeRegistries.FLUIDS.getValue(...) 未知 id 返回 null 后在 FluidStack 构造处崩溃；
        // 原版 FLUID 注册表为 DefaultedRegistry（缺省 water），直接 get 会静默变水，故先 containsKey 显式报错。
        if (!BuiltInRegistries.FLUID.containsKey(fluidLocation)) {
            throw new JsonSyntaxException("Unknown fluid '" + fluidId + "'");
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidLocation);
        int fluidAmount = GsonHelper.getAsInt(pJson, "fluid_amount", 1000);
        FluidVariant inputFluid = FluidVariant.of(fluid);
        JsonObject resultObj = GsonHelper.getAsJsonObject(pJson, "result");
        // 原 Forge CraftingHelper.getItemStack(resultObj, true)；1.20.1 原版等价为 ShapedRecipe.itemStackFromJson（读 item+count）。
        // 现有 freezer 配方 result 均无 nbt 字段，行为一致。
        ItemStack resultItem = ShapedRecipe.itemStackFromJson(resultObj);
        int craftTime = GsonHelper.getAsInt(pJson, "craft_time", 200);
        String textureId = GsonHelper.getAsString(pJson, "texture");
        Ingredient extractIngredient = Ingredient.EMPTY;
        if (pJson.has("extract_condition")) {
            extractIngredient = parseIngredientLenient(pJson.get("extract_condition"));
        }

        return new FreezerRecipe(pRecipeId, inputFluid, fluidAmount, ingredients, resultItem, craftTime, new ResourceLocation(textureId), extractIngredient);
    }

    private static Ingredient parseIngredientLenient(JsonElement elem) {
        if (elem == null || elem.isJsonNull()) {
            return Ingredient.EMPTY;
        } else if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
            String s = elem.getAsString();
            if (s.isEmpty()) {
                return Ingredient.EMPTY;
            } else {
                JsonObject obj = new JsonObject();
                obj.addProperty("item", s);
                return Ingredient.fromJson(obj);
            }
        } else if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.has("nbt") && !obj.has("type")) {
                // TODO(fabric): "forge:nbt" 是 Forge 专有 Ingredient 类型，1.20.1 原版 Ingredient.fromJson 无 type 分发，
                // 会忽略 type/nbt 字段退化为按 item 匹配（当前 freezer 配方数据无用到 nbt 的，行为不受影响）。
                obj.addProperty("type", "forge:nbt");
            }

            return Ingredient.fromJson(obj);
        } else {
            return Ingredient.EMPTY;
        }
    }

    public FreezerRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
        FluidVariant inputFluid = FluidVariant.fromPacket(pBuffer);
        int fluidAmount = pBuffer.readVarInt();
        int size = Math.min(4, pBuffer.readVarInt());
        NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);

        for (int i = 0; i < size; i++) {
            ingredients.set(i, Ingredient.fromNetwork(pBuffer));
        }

        ItemStack resultItem = pBuffer.readItem();
        int craftTime = pBuffer.readInt();
        ResourceLocation resultTexture = pBuffer.readResourceLocation();
        Ingredient extractIngredient = Ingredient.fromNetwork(pBuffer);
        return new FreezerRecipe(pRecipeId, inputFluid, fluidAmount, ingredients, resultItem, craftTime, resultTexture, extractIngredient);
    }

    public void toNetwork(FriendlyByteBuf pBuffer, FreezerRecipe pRecipe) {
        pRecipe.getInputFluid().toPacket(pBuffer);
        pBuffer.writeVarInt(pRecipe.getFluidAmount());
        pBuffer.writeVarInt(pRecipe.getIngredients().size());

        for (Ingredient ingredient : pRecipe.getIngredients()) {
            ingredient.toNetwork(pBuffer);
        }

        pBuffer.writeItem(pRecipe.getResultItem());
        pBuffer.writeInt(pRecipe.getCraftTime());
        pBuffer.writeResourceLocation(pRecipe.getResultTexture());
        pRecipe.getExtractIngredient().toNetwork(pBuffer);
    }
}
