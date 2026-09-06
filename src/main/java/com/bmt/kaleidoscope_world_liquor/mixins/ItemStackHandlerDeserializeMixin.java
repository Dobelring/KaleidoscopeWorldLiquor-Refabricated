package com.bmt.kaleidoscope_world_liquor.mixins;

import com.github.ysbbbbbb.kaleidoscopetavern.util.neo.ItemStackHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 兼容旧版（NeoForge）存档：Items 列表中可能存在只有 Slot 而无 id 的空条目，
 * 直接传给 ItemStack.parse 会打印 "Tried to load invalid item" 警告。
 * 注入后跳过这些无效条目，静默兼容。
 */
@Mixin(ItemStackHandler.class)
public abstract class ItemStackHandlerDeserializeMixin {
   @Inject(method = "deserializeNBT", at = @At("HEAD"), cancellable = true)
   private void kaleidoscope$skipInvalidItems(HolderLookup.Provider provider, CompoundTag nbt, CallbackInfo ci) {
      ListTag tagList = nbt.getList("Items", 10);
      boolean changed = false;
      for (int i = 0; i < tagList.size(); i++) {
         CompoundTag itemTags = tagList.getCompound(i);
         if (!itemTags.contains("id")) {
            tagList.remove(i);
            i--;
            changed = true;
         }
      }
      if (changed) {
         nbt.put("Items", tagList);
      }
   }
}
