package com.bmt.kaleidoscope_world_liquor.util;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class PortHelper {
    private PortHelper() {
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, name);
    }

    public static ResourceKey<Block> createBlockId(String name) {
        return ResourceKey.create(Registries.BLOCK, id(name));
    }

    public static ResourceKey<net.minecraft.world.item.Item> createItemId(String name) {
        return ResourceKey.create(Registries.ITEM, id(name));
    }

    public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> items, boolean alwaysPutTag, HolderLookup.Provider registries) {
        net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                listTag.add(ItemStack.CODEC.encode(stack, registries.createSerializationContext(NbtOps.INSTANCE), entry).getOrThrow());
            }
        }
        if (!listTag.isEmpty() || alwaysPutTag) {
            tag.put("Items", listTag);
        }
        return tag;
    }
}
