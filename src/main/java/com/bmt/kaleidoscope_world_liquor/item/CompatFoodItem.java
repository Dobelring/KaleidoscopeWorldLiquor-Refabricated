package com.bmt.kaleidoscope_world_liquor.item;

import net.minecraft.world.item.Item;

/**
 * twilight 联动食物物品：模型/贴图/名称以 kaleidoscope_twilight 命名空间提供
 * （assets 已随第 2 步迁移），物品本身注册在 twilight 命名空间。
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
}
