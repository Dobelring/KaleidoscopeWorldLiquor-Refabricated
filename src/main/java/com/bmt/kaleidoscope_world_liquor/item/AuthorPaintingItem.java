package com.bmt.kaleidoscope_world_liquor.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * 作者画物品：背包 tooltip 追加一行作者（tooltip.kaleidoscope_tavern.<画名>，
 * 灰色斜体）。仅作用于物品 tooltip——WTHIT 指向已放置的画不显示作者
 * （与 1.21.11 同款拍板：背包显示、放出后不显示）。
 */
public class AuthorPaintingItem extends BlockItem {
    public AuthorPaintingItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(this);
        tooltip.add(Component.translatable("tooltip." + id.getNamespace() + "." + id.getPath())
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
