package com.bmt.kaleidoscope_world_liquor.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompatFoodItem extends Item {
    private final String fakeModId;
    @Nullable
    private final Item remainderItem;

    public CompatFoodItem(Properties properties, String fakeModId) {
        this(properties, fakeModId, null);
    }

    public CompatFoodItem(Properties properties, String fakeModId, @Nullable Item remainderItem) {
        super(properties);
        this.fakeModId = fakeModId;
        this.remainderItem = remainderItem;
    }

    // TODO(fabric): 原为 Forge IForgeItem#getCreatorModId(ItemStack) 的覆写（供 JEI/创造模式物品归属显示使用），
    // Fabric 原版 Item 无此钩子，故保留为普通方法以不丢失该字段的语义（照 CF item/BunItem 先例）。
    @Nullable
    public String getCreatorModId(ItemStack stack) {
        return this.fakeModId;
    }

    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, livingEntity);
        if (this.remainderItem != null) {
            if (stack.isEmpty()) {
                return new ItemStack(this.remainderItem);
            } else {
                livingEntity.spawnAtLocation(new ItemStack(this.remainderItem));
                return resultStack;
            }
        } else {
            return resultStack;
        }
    }
}
