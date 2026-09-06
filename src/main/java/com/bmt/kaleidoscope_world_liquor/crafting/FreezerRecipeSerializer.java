package com.bmt.kaleidoscope_world_liquor.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 冰柜配方序列化器。JSON 字段与 1.20.1 完全兼容：
 * type/fluid/fluid_amount/ingredients/result/craft_time/texture/extract_condition。
 */
public class FreezerRecipeSerializer implements RecipeSerializer<FreezerRecipe> {
    public static final MapCodec<FreezerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("fluid").forGetter(FreezerRecipe::fluid),
                    Codec.INT.optionalFieldOf("fluid_amount", 1000).forGetter(FreezerRecipe::fluidAmount),
                    Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(FreezerRecipe::ingredients),
                    ItemStack.CODEC.fieldOf("result").forGetter(FreezerRecipe::result),
                    Codec.INT.optionalFieldOf("craft_time", 200).forGetter(FreezerRecipe::craftTime),
                    Identifier.CODEC.fieldOf("texture").forGetter(FreezerRecipe::resultTexture),
                    Ingredient.CODEC.optionalFieldOf("extract_condition").forGetter(FreezerRecipe::extractIngredient)
            ).apply(instance, FreezerRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FreezerRecipe> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, FreezerRecipe::fluid,
            ByteBufCodecs.VAR_INT, FreezerRecipe::fluidAmount,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FreezerRecipe::ingredients,
            ItemStack.STREAM_CODEC, FreezerRecipe::result,
            ByteBufCodecs.VAR_INT, FreezerRecipe::craftTime,
            Identifier.STREAM_CODEC, FreezerRecipe::resultTexture,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, FreezerRecipe::extractIngredient,
            FreezerRecipe::new
    );

    @Override
    public @NotNull MapCodec<FreezerRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, FreezerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
