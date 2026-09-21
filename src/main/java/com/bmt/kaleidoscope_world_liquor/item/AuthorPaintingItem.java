package com.bmt.kaleidoscope_world_liquor.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * 作者画物品：背包 tooltip 追加一行"作者：xxx"（tooltip.kaleidoscope_tavern.<画名>，
 * 灰色斜体，与 1.20.1 原版一致）。仅作用于物品 tooltip——WTHIT 指向已放置的画走
 * 自己的 provider，不会显示作者行（用户拍板 2026-09-08：背包显示、放出后不显示）。
 */
public class AuthorPaintingItem extends BlockItem {
    public AuthorPaintingItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        Identifier id = BuiltInRegistries.ITEM.getKey(this);
        tooltip.accept(Component.translatable("tooltip." + id.getNamespace() + "." + id.getPath())
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
