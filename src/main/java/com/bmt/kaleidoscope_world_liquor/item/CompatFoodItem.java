package com.bmt.kaleidoscope_world_liquor.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * twilight/smc 联动食物物品：模型/贴图/名称以联动命名空间提供，物品本身注册在联动命名空间。
 * 官方 1.2.0：座右铭 tooltip（tooltip.<ns>.<path>.maxim，暗灰斜体，支持多行）。
 */
public class CompatFoodItem extends Item {
    private final String namespace;

    public CompatFoodItem(Properties properties, String namespace) {
        super(properties);
        this.namespace = namespace;
    }

    public String namespace() {
        return namespace;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltipAdder, TooltipFlag flag) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null) {
            String key = "tooltip.%s.%s.maxim".formatted(id.getNamespace(), id.getPath());
            MutableComponent full = Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
            String text = full.getString();
            for (String line : text.split("\\n")) {
                if (!line.isEmpty()) {
                    tooltipAdder.accept(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                } else {
                    tooltipAdder.accept(CommonComponents.EMPTY);
                }
            }
        }
    }
}