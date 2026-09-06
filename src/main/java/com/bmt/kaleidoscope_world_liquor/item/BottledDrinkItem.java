package com.bmt.kaleidoscope_world_liquor.item;

import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 瓶装饮料（cola / tonic_water）：饮用动画 + 喝完返还玻璃瓶。
 * 1.20.1 的 finishUsingItem 手动还瓶，1.21.11 用 UseRemainder 组件等价实现。
 */
public class BottledDrinkItem extends Item implements IHasContainer {
    public BottledDrinkItem(Properties properties) {
        super(properties
                .component(net.minecraft.core.component.DataComponents.USE_REMAINDER,
                        new net.minecraft.world.item.component.UseRemainder(new ItemStack(Items.GLASS_BOTTLE))));
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
}
