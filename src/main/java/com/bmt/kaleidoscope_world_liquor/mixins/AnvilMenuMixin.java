package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.event.BrewAcceleratorEventHandler;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.ItemCombinerMenuAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
   @Inject(
      method = {"createResult"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kaleidoscope$cancelBrewAcceleratorCombine(CallbackInfo ci) {
      ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor)this;
      Container inputSlots = accessor.getInputSlots();
      Player player = accessor.getPlayer();
      ItemStack right = inputSlots.getItem(1);
      ItemStack left = inputSlots.getItem(0);
      Level level = player.level();
      if (right.getItem() instanceof EnchantedBookItem && BrewAcceleratorEventHandler.hasBrewAccelerator(right, level) && !(left.getItem() instanceof EnchantedBookItem)) {
         ci.cancel();
      }
   }
}
