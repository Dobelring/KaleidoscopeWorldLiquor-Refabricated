package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 臻酿附魔书的冷却条（官方 1.2.0：每本书自身 NBT 冷却）。
 * 26.1.2：GuiGraphics 更名 GuiGraphicsExtractor，冷却绘制方法为 itemCooldown——
 * 其内部从玩家冷却系统取百分比（首个 float 局部即该百分比），@ModifyVariable 截获后改写：
 * 非臻酿附魔书 → 0（原过滤行为），臻酿书 → 按书内 kaleidoscope_brew_cooldown_until
 * NBT 显示剩余冷却（配合 MiscEvents 的每书 NBT 冷却写入）。
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsMixin {

    @ModifyVariable(
        method = "itemCooldown",
        at = @At("STORE"),
        ordinal = 0
    )
    private float kwl$perBookCooldown(float cooldownPercent, ItemStack stack, int x, int y) {
        if (!stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
            return cooldownPercent;
        }
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (!hasBrewAccelerator(enchantments) && !hasBrewAccelerator(stored)) {
            // 非臻酿附魔书不显示冷却（原版行为）
            return 0.0F;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0.0F;
        }
        CompoundTag tag = customData.copyTag();
        long cooldownUntil = tag.getLong("kaleidoscope_brew_cooldown_until").orElse(0L);
        long remaining = cooldownUntil - (Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime());
        return remaining <= 0L ? 0.0F : Mth.clamp((float) remaining / 2400.0F, 0.0F, 1.0F);
    }

    private boolean hasBrewAccelerator(ItemEnchantments enchantments) {
        if (enchantments == null) {
            return false;
        }
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().unwrapKey().map(k -> k.identifier().equals(ModEnchantments.BREW_ACCELERATOR.identifier())).orElse(false)) {
                return true;
            }
        }
        return false;
    }
}
