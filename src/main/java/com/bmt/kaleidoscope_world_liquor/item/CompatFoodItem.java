package com.bmt.kaleidoscope_world_liquor.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.bmt.kaleidoscope_world_liquor.util.ItemUtils;
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

   @Nullable
   public String getCreatorModId(ItemStack stack) {
      return this.fakeModId;
   }

   // 官方 1.2.0：座右铭 tooltip（tooltip.<ns>.<path>.maxim，暗灰斜体，支持多行）
   @Override
   public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      if (id != null) {
         String key = "tooltip.%s.%s.maxim".formatted(id.getNamespace(), id.getPath());
         MutableComponent full = Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
         String text = full.getString();

         for (String line : text.split("\\n")) {
            if (!line.isEmpty()) {
               tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
               tooltipComponents.add(CommonComponents.EMPTY);
            }
         }
      }
   }

   // 官方 1.2.0：剩余物正确给到玩家背包/掉落，不再只依赖用尽时返回
   @Override
   @NotNull
   public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
      ItemStack resultStack = super.finishUsingItem(stack, level, livingEntity);
      if (this.remainderItem != null) {
         ItemStack remainder = new ItemStack(this.remainderItem);
         if (resultStack.isEmpty()) {
            return remainder;
         }

         if (livingEntity instanceof Player player) {
            ItemUtils.giveItemToPlayer(player, remainder);
         } else {
            livingEntity.spawnAtLocation(remainder);
         }
      }

      return resultStack;
   }
}
