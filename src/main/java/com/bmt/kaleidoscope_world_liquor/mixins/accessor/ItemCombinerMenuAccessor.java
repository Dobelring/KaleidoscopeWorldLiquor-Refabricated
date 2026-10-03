package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 访问器（事件批次新增，需登记进 kaleidoscope_world_liquor.mixins.json 的 mixins 数组）。
 * <p>
 * {@code inputSlots}/{@code resultSlots} 声明在 ItemCombinerMenu（AnvilMenu 的父类），
 * 而 mixin 的 @Shadow 只按目标类**自身声明字段**解析（sponge-mixin
 * TargetClassContext.findAliasedField 只查 classNode.fields，不上溯父类），
 * 所以给 AnvilMenu 写的 AnvilMenuMixin 不能直接 @Shadow 这两个字段，改走本访问器
 * （字段在 ItemCombinerMenu 自身，@Accessor 合法）。
 */
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {

    @Accessor("inputSlots")
    Container getInputSlots();

    @Accessor("resultSlots")
    ResultContainer getResultSlots();
}
