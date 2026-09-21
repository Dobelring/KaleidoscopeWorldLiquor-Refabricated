package com.bmt.kaleidoscope_world_liquor.item;

import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * 瓶装饮料（cola / tonic_water）：饮用动画 + 喝完返还玻璃瓶。
 * 1.20.1 的 finishUsingItem 手动还瓶，1.21.11 用 UseRemainder 组件等价实现。
 * 1.1.9 起「颜色：XX」tooltip 由事件层移入物品覆写（26.x 线的移植版此前整段丢失，本次补齐）。
 */
public class BottledDrinkItem extends Item implements IHasContainer {
    public BottledDrinkItem(Properties properties) {
        super(properties
                .component(net.minecraft.core.component.DataComponents.USE_REMAINDER,
                        new net.minecraft.world.item.component.UseRemainder(new net.minecraft.world.item.ItemStackTemplate(Items.GLASS_BOTTLE))));
    }

    @Override
    public Item getContainerItem() {
        return Items.GLASS_BOTTLE;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 32;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
                                @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        if (this == ModItems.COLA) {
            tooltip.accept(Component.empty()
                    .append(Component.translatable("item.kaleidoscope_world_liquor.cola.tooltip.front").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("item.kaleidoscope_world_liquor.cola.tooltip.back").withStyle(ChatFormatting.DARK_RED)));
        }
        if (this == ModItems.TONIC_WATER) {
            tooltip.accept(Component.empty()
                    .append(Component.translatable("item.kaleidoscope_world_liquor.tonic_water.tooltip.front").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("item.kaleidoscope_world_liquor.tonic_water.tooltip.back").withStyle(ChatFormatting.WHITE)));
        }
    }
}
