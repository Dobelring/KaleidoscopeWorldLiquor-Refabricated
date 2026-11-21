package com.bmt.kaleidoscope_world_liquor.init.smc;

import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 官方 1.2.0：smc 在场时，把 smc 自家创造栏（smc:smc_food_tab）里的
 * ice_tea/friends_drink 原位替换为最高酿造等级版本（否则从 smc 栏拿到的饮品
 * 无酿造等级 → 喝了没效果）。原 Forge BuildCreativeModeTabContentsEvent 的
 * MutableHashedLinkedMap putBefore 语义 → fabric-creative-tab-api-v1 的
 * FabricCreativeModeTabOutput 可变 display/search 列表原位 set（同位置替换，顺序不变）。
 */
public final class SMCCreativeTabEvents {
    private static final Identifier SMC_FOOD_TAB = Identifier.fromNamespaceAndPath("smc", "smc_food_tab");

    private SMCCreativeTabEvents() {
    }

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("smc")) {
            CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, SMC_FOOD_TAB))
                    .register(SMCCreativeTabEvents::onBuildContents);
        }
    }

    private static void onBuildContents(FabricCreativeModeTabOutput output) {
        applyMaxBrewLevel(output.getDisplayStacks(), "ice_tea");
        applyMaxBrewLevel(output.getSearchTabStacks(), "ice_tea");
        applyMaxBrewLevel(output.getDisplayStacks(), "friends_drink");
        applyMaxBrewLevel(output.getSearchTabStacks(), "friends_drink");
    }

    private static void applyMaxBrewLevel(List<ItemStack> stacks, String path) {
        Item drink = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("smc", path));
        if (drink == null || drink == Items.AIR) {
            return;
        }
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.is(drink) && BottleBlockItem.getBrewLevel(stack) <= 0) {
                ItemStack maxLevelDrink = stack.copy();
                BottleBlockItem.setBrewLevel(maxLevelDrink, 6);
                stacks.set(i, maxLevelDrink);
            }
        }
    }
}
