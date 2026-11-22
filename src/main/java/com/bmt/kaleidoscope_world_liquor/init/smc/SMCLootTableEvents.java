package com.bmt.kaleidoscope_world_liquor.init.smc;

import com.bmt.kaleidoscope_world_liquor.loot.RandomBrewLevelFunction;
import com.bmt.kaleidoscope_world_liquor.mixin.accessor.CompositeEntryBaseAccessor;
import com.bmt.kaleidoscope_world_liquor.mixin.accessor.LootItemAccessor;
import com.bmt.kaleidoscope_world_liquor.mixin.accessor.LootPoolEntryContainerAccessor;
import com.bmt.kaleidoscope_world_liquor.mixin.accessor.LootTableAccessor;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

/**
 * 官方 1.2.0：smc 在场时，遍历全部战利品表，给指向 smc 饮品（ice_tea/friends_drink）的
 * LootItem 附加随机酿造等级（RandomBrewLevelFunction，3-6 级）。
 * 原 Forge LootTableLoadEvent（逐表）→ Fabric loot-api-v3 ALL_LOADED（26.x 战利品表进动态
 * 注册表，直接遍历 Registry<LootTable>）。26.x LootPool.entries 本身 public，无需 accessor。
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
                                           net.minecraft.core.Registry<LootTable> tables) {
        for (Item drink : getSmcDrinks()) {
            for (LootTable table : tables) {
                for (LootPool pool : ((LootTableAccessor) table).kwl$getPools()) {
                    for (LootPoolEntryContainer entry : pool.entries) {
                        processEntry(entry, drink);
                    }
                }
            }
        }
    }

    private static void processEntry(LootPoolEntryContainer entry, Item drink) {
        if (entry instanceof LootItem lootItem) {
            Holder<Item> item = ((LootItemAccessor) lootItem).kwl$getItem();
            if (item.value() == drink) {
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

    private static void attachRandomBrewLevel(LootItem entry) {
        LootPoolEntryContainerAccessor accessor = (LootPoolEntryContainerAccessor) entry;
        java.util.Optional<Holder<LootItemFunction>> oldModifier = accessor.kwl();
        // 26.3：SequenceFunction.functions 无公开访问器，无法完全展开查重；表对象每次加载均为新实例，
        // 单次加载内每条目只访问一次，顶层判重足够
        if (oldModifier.isPresent() && oldModifier.get().value() == RandomBrewLevelFunction.INSTANCE) {
            return;
        }
        Holder<LootItemFunction> appended = Holder.direct(RandomBrewLevelFunction.INSTANCE);
        Holder<LootItemFunction> composed = oldModifier.isPresent()
                ? Holder.direct(SequenceFunction.of(List.of(oldModifier.get(), appended)))
                : appended;
        accessor.kwl(java.util.Optional.of(composed));
    }

    private static List<Item> getSmcDrinks() {
        if (!resolved) {
            resolved = true;
            List<Item> drinks = new ArrayList<>();
            if (FabricLoader.getInstance().isModLoaded("smc")) {
                for (String id : SMC_DRINK_IDS) {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("smc", id));
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
