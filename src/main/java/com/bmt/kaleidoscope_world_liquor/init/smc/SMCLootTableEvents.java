package com.bmt.kaleidoscope_world_liquor.init.smc;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.loot.RandomBrewLevelFunction;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.CompositeEntryBaseAccessor;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.LootItemAccessor;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.LootPoolAccessor;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.LootPoolSingletonContainerAccessor;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.LootTableAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

/**
 * 官方 1.2.0：smc 在场时，遍历全部战利品表，给指向 smc 饮品（ice_tea/friends_drink）的
 * LootItem 附加随机酿造等级（RandomBrewLevelFunction，3-6 级）。
 * 原 Forge LootTableLoadEvent（逐表）→ Fabric LootTableEvents.ALL_LOADED（全表遍历后统一挂载）。
 */
public class SMCLootTableEvents {
    private static final String[] SMC_DRINK_IDS = new String[]{"ice_tea", "friends_drink"};
    private static List<Item> smcDrinks;
    private static boolean resolved;

    public SMCLootTableEvents() {
    }

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("smc")) {
            LootTableEvents.ALL_LOADED.register(SMCLootTableEvents::onLootTablesLoaded);
        }
    }

    private static void onLootTablesLoaded(net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                           net.minecraft.world.level.storage.loot.LootDataManager lootTables) {
        // 1.20.1 LootDataManager 无 getAllTables()：按 getKeys(LootDataType.TABLE) + getElement 枚举
        for (Item drink : getSmcDrinks()) {
            for (ResourceLocation tableId : lootTables.getKeys(net.minecraft.world.level.storage.loot.LootDataType.TABLE)) {
                LootTable table = lootTables.getElement(new net.minecraft.world.level.storage.loot.LootDataId<>(net.minecraft.world.level.storage.loot.LootDataType.TABLE, tableId));
                if (table == null) {
                    continue;
                }
                for (LootPool pool : ((LootTableAccessor) table).kwl$getPools()) {
                    for (LootPoolEntryContainer entry : ((LootPoolAccessor) pool).kwl$getEntries()) {
                        processEntry(entry, drink);
                    }
                }
            }
        }
    }

    private static void processEntry(LootPoolEntryContainer entry, Item drink) {
        if (entry instanceof LootItem lootItem) {
            if (((LootItemAccessor) lootItem).kwl$getItem() == drink) {
                attachRandomBrewLevel(lootItem);
            }
        } else {
            if (entry instanceof CompositeEntryBase composite) {
                for (LootPoolEntryContainer child : ((CompositeEntryBaseAccessor) composite).kwl$getChildren()) {
                    processEntry(child, drink);
                }
            }
        }
    }

    private static void attachRandomBrewLevel(LootPoolSingletonContainer entry) {
        LootPoolSingletonContainerAccessor accessor = (LootPoolSingletonContainerAccessor) entry;
        LootItemFunction[] oldFunctions = accessor.kwl$getFunctions();

        for (LootItemFunction function : oldFunctions) {
            if (function == RandomBrewLevelFunction.INSTANCE) {
                return;
            }
        }

        LootItemFunction[] newFunctions = Arrays.copyOf(oldFunctions, oldFunctions.length + 1);
        newFunctions[oldFunctions.length] = RandomBrewLevelFunction.INSTANCE;
        accessor.kwl$setFunctions(newFunctions);
        BiFunction<ItemStack, LootContext, ItemStack> composite = (stack, context) -> {
            for (LootItemFunction functionx : newFunctions) {
                stack = functionx.apply(stack, context);
            }

            return stack;
        };
        accessor.kwl$setCompositeFunction(composite);
    }

    private static List<Item> getSmcDrinks() {
        if (!resolved) {
            resolved = true;
            List<Item> drinks = new ArrayList<>();
            if (FabricLoader.getInstance().isModLoaded("smc")) {
                for (String id : SMC_DRINK_IDS) {
                    Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("smc", id));
                    if (item != null && item != Items.AIR) {
                        drinks.add(item);
                    }
                }
            }

            smcDrinks = drinks;
        }

        return smcDrinks;
    }
}
