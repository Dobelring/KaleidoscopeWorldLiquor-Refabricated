package com.bmt.kaleidoscope_world_liquor.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 冰柜配方序列化常量。JSON 字段与 1.20.1 完全兼容：
 * type/fluid/fluid_amount/ingredients/result/craft_time/texture/extract_condition。
 * 26.1.2 RecipeSerializer 是 record：注册时 new RecipeSerializer<>(CODEC, STREAM_CODEC)。
 */
public final class FreezerRecipeSerializer {
    public static final MapCodec<FreezerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("fluid").forGetter(FreezerRecipe::fluid),
                    Codec.INT.optionalFieldOf("fluid_amount", 1000).forGetter(FreezerRecipe::fluidAmount),
                    Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(FreezerRecipe::ingredients),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(FreezerRecipe::result),
                    Codec.INT.optionalFieldOf("craft_time", 200).forGetter(FreezerRecipe::craftTime),
                    Identifier.CODEC.fieldOf("texture").forGetter(FreezerRecipe::resultTexture),
                    Ingredient.CODEC.optionalFieldOf("extract_condition").forGetter(FreezerRecipe::extractIngredient)
            ).apply(instance, FreezerRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FreezerRecipe> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, FreezerRecipe::fluid,
            ByteBufCodecs.VAR_INT, FreezerRecipe::fluidAmount,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FreezerRecipe::ingredients,
            ItemStackTemplate.STREAM_CODEC, FreezerRecipe::result,
            ByteBufCodecs.VAR_INT, FreezerRecipe::craftTime,
            Identifier.STREAM_CODEC, FreezerRecipe::resultTexture,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, FreezerRecipe::extractIngredient,
            FreezerRecipe::new
    );
}
