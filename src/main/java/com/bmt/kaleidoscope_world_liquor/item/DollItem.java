package com.bmt.kaleidoscope_world_liquor.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;


/**
 * 玩偶物品（1.21.11 无 kaleidoscope_doll 模组，liquor 自实现）。
 * 显示名照原版 DollItem：固定键 block.kaleidoscope_doll.doll（"玩偶"，liquor lang 自带，
 * 因 doll mod 的 lang 缺失）；tooltip 追加"作者：xxx"。
 */
public class DollItem extends BlockItem {
    private static final Component DOLL_NAME = Component.translatable("block.kaleidoscope_doll.doll");
    private final String authorKey;

    public DollItem(Block block, String authorKey, Properties properties) {
        super(block, properties);
        this.authorKey = authorKey;
    }

    @Override
    public Component getName(ItemStack stack) {
        return DOLL_NAME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(this.authorKey)
                .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC));
    }
}
