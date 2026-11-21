package com.bmt.kaleidoscope_world_liquor.loot;

import com.bmt.kaleidoscope_world_liquor.init.smc.SMCLootFunctionTypes;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.Serializer;

/**
 * 官方 1.2.0：战利品中的 smc 饮品随机附加 3-6 级酿造等级（类型注册与挂载均在
 * smc 模组在场时生效，见 SMCLootFunctionTypes / SMCLootTableEvents）。
 */
public class RandomBrewLevelFunction implements LootItemFunction {
    public static final int MIN_LEVEL = 3;
    public static final int LEVEL_COUNT = 4;
    public static final RandomBrewLevelFunction INSTANCE = new RandomBrewLevelFunction();

    public RandomBrewLevelFunction() {
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        int brewLevel = MIN_LEVEL + random.nextInt(LEVEL_COUNT);
        BottleBlockItem.setBrewLevel(stack, brewLevel);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return SMCLootFunctionTypes.RANDOM_BREW_LEVEL;
    }

    public static class RandomBrewLevelSerializer implements Serializer<RandomBrewLevelFunction> {
        public RandomBrewLevelSerializer() {
        }

        @Override
        public void serialize(JsonObject json, RandomBrewLevelFunction value, JsonSerializationContext context) {
        }

        @Override
        public RandomBrewLevelFunction deserialize(JsonObject json, JsonDeserializationContext context) {
            return RandomBrewLevelFunction.INSTANCE;
        }
    }
}
