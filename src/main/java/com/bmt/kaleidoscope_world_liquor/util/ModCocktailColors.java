package com.bmt.kaleidoscope_world_liquor.util;

import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import com.google.common.collect.Lists;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 官方 1.1.11 新增的 4 个调酒颜色（brown/orange/light_blue/pink）——
 * 这 4 个不在 tavern {@code ColorUtils} 的 16 色 tag 映射里，需要本模组自己识别。
 * <p>
 * 26.3 差异：{@code ColorUtils.ITEM_COLOR_CACHE} 返回 {@link TextColor}（1.21.1 是 ChatFormatting），
 * 且默认 {@link TextColor#WHITE}；tavern 的 {@code ItemStackHandler} 在 {@code util.neo} 包。
 */
public final class ModCocktailColors {
    public static final int NO_COLOR = -1;

    public static final List<ExtraColor> EXTRA_COLORS = Lists.newArrayList(
            new ExtraColor(tag("cocktail_ingredient_brown"), 8606770, "brown"),
            new ExtraColor(tag("cocktail_ingredient_orange"), 16351261, "orange"),
            new ExtraColor(tag("cocktail_ingredient_light_blue"), 3847130, "light_blue"),
            new ExtraColor(tag("cocktail_ingredient_pink"), 15961002, "pink")
    );

    private ModCocktailColors() {
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("kaleidoscope_tavern", name));
    }

    public static int getCustomColorByTag(TagKey<Item> tag) {
        for (ExtraColor extra : EXTRA_COLORS) {
            if (extra.tag().equals(tag)) {
                return extra.rgb();
            }
        }
        return NO_COLOR;
    }

    public static int getCustomColor(Item item) {
        for (ExtraColor extra : EXTRA_COLORS) {
            if (item.builtInRegistryHolder().is(extra.tag())) {
                return extra.rgb();
            }
        }
        return NO_COLOR;
    }

    @Nullable
    public static String getCustomColorName(Item item) {
        for (ExtraColor extra : EXTRA_COLORS) {
            if (item.builtInRegistryHolder().is(extra.tag())) {
                return extra.name();
            }
        }
        return null;
    }

    /** 单件物品的混合色：药水=白，自定义 4 色优先，其余回落到 tavern 的 tag 色。 */
    public static int getMixColor(ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            return 16777215;
        }
        int custom = getCustomColor(stack.getItem());
        if (custom != NO_COLOR) {
            return custom;
        }
        TextColor textColor = ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
        return textColor != null ? textColor.getValue() : NO_COLOR;
    }

    /** 雪克杯存储物的混合色（倒入特调杯时的最终颜色）。 */
    public static int mixStorageColors(ItemStackHandler storage) {
        List<Integer> colors = Lists.newArrayList();
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack ingredient = storage.getStackInSlot(i);
            if (!ingredient.isEmpty()) {
                int color = getMixColor(ingredient);
                if (color != NO_COLOR) {
                    colors.add(color);
                }
            }
        }
        return mixRgb(colors);
    }

    public static int mixRgb(List<Integer> colors) {
        if (colors.isEmpty()) {
            return 16777215;
        }
        int totalR = 0;
        int totalG = 0;
        int totalB = 0;
        for (int color : colors) {
            totalR += color >> 16 & 0xFF;
            totalG += color >> 8 & 0xFF;
            totalB += color & 0xFF;
        }
        int count = colors.size();
        return totalR / count << 16 | totalG / count << 8 | totalB / count;
    }

    public record ExtraColor(TagKey<Item> tag, int rgb, String name) {
    }
}
