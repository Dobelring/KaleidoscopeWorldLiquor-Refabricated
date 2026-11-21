package com.bmt.kaleidoscope_world_liquor.loot;

import com.bmt.kaleidoscope_world_liquor.init.smc.SMCLootFunctionTypes;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

/**
 * 官方 1.2.0：战利品中的 smc 饮品随机附加 3-6 级酿造等级（类型注册与挂载均在
 * smc 模组在场时生效，见 SMCLootFunctionTypes / SMCLootTableEvents）。
 * 26.x：LootItemFunctionType 已由 MapCodec 注册表替代，codec() 返回恒等解码到单例的 codec。
 */
public class RandomBrewLevelFunction implements LootItemFunction {
    public static final int MIN_LEVEL = 3;
    public static final int LEVEL_COUNT = 4;
    public static final RandomBrewLevelFunction INSTANCE = new RandomBrewLevelFunction();
    public static final MapCodec<RandomBrewLevelFunction> CODEC = MapCodec.unit(INSTANCE);

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
    public MapCodec<? extends LootItemFunction> codec() {
        return SMCLootFunctionTypes.RANDOM_BREW_LEVEL;
    }
}
