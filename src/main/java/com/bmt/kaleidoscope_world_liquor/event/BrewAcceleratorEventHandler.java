package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.BarrelBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class BrewAcceleratorEventHandler {
   public static final int COOLDOWN_TICKS = 2400;

   public static void register() {
      UseBlockCallback.EVENT.register(BrewAcceleratorEventHandler::onRightClickBlock);
   }

   public static boolean hasBrewAccelerator(ItemStack stack, Level level) {
      return getBrewAcceleratorLevel(stack, level) > 0;
   }

   private static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
      BlockPos pos = hitResult.getBlockPos();
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide || hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }

      boolean isEnchantedBook = stack.getItem() instanceof EnchantedBookItem;
      int enchantLevel = 0;
      if (isEnchantedBook) {
         enchantLevel = getBrewAcceleratorLevel(stack, level);
      }

      if (isEnchantedBook && enchantLevel != 0) {
         // 官方 1.2.0：冷却改为每本书自身 NBT（kaleidoscope_brew_cooldown_until），不再全局冷却附魔书
         long gameTime = level.getGameTime();
         net.minecraft.nbt.CompoundTag stackTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
         long cooldownUntil = stackTag.getLong("kaleidoscope_brew_cooldown_until");
         if (cooldownUntil > gameTime) {
            int remainingSeconds = (int)((cooldownUntil - gameTime) / 20L);
            player.displayClientMessage(
               Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.cooldown", new Object[]{remainingSeconds}), true
            );
            return InteractionResult.FAIL;
         } else if (level.getBlockState(pos).getBlock() instanceof BarrelBlock) {
            BlockEntity be = BarrelBlock.getBarrelEntity(level, pos, level.getBlockState(pos));
            if (be instanceof IBarrel barrel) {
               if (!barrel.isBrewing()) {
                  player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"), true);
                  return InteractionResult.FAIL;
               } else if (barrel.isMaxBrewLevel()) {
                  player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"), true);
                  return InteractionResult.FAIL;
               } else {
                  try {
                     BarrelBlockEntityAccessor accessor = (BarrelBlockEntityAccessor)barrel;
                     int newLevel = Math.min(barrel.getBrewLevel() + 1, 6);
                     accessor.setBrewLevel(newLevel);
                     accessor.setBrewTime(accessor.invokeGetBrewTimeForLevel());
                     ((BarrelBlockEntity)barrel).refresh();
                     stackTag.putLong("kaleidoscope_brew_cooldown_until", gameTime + COOLDOWN_TICKS);
                     stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(stackTag));
                     level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
                     player.displayClientMessage(
                        Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.success", new Object[]{newLevel}), true
                     );
                     return InteractionResult.SUCCESS;
                  } catch (Exception var13) {
                     player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.error"), true);
                     return InteractionResult.FAIL;
                  }
               }
            }
         }
      }
      return InteractionResult.PASS;
   }

   public static int getBrewAcceleratorLevel(ItemStack stack, Level level) {
      ItemEnchantments storedEnchants = (ItemEnchantments)stack.get(DataComponents.STORED_ENCHANTMENTS);
      if (storedEnchants != null && !storedEnchants.isEmpty()) {
         RegistryLookup<Enchantment> enchantLookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
         return enchantLookup.get(ModEnchantments.BREW_ACCELERATOR).<Integer>map(storedEnchants::getLevel).orElse(0);
      } else {
         return 0;
      }
   }
}
