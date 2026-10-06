package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModPaintings;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 原 Forge 版 @EventBusSubscriber(Bus.MOD) 的 BuildCreativeModeTabContentsEvent 监听 →
 * Fabric 的 {@code ItemGroupEvents.modifyEntriesEvent(ResourceKey<CreativeModeTab>)}：
 * 每个标签页一个回调（原“比较 tabKey 是否为 tavern_deco”改为直接按该 key 注册，门控语义等价）。
 * 往**别人的**（tavern 装饰栏）创造栏塞挂画用同一 API。
 */
public class CreativeTabEvents {
    /** 原 ResourceLocation.tryBuild("kaleidoscope_tavern", "tavern_deco")（已核实 tavern Fabric 版栏位 id 不变）。 */
    private static final ResourceKey<CreativeModeTab> TAVERN_DECO_TAB = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        new ResourceLocation("kaleidoscope_tavern", "tavern_deco")
    );

    public CreativeTabEvents() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(TAVERN_DECO_TAB)
            .register(entries -> addPaintingsToTabs(TAVERN_DECO_TAB, entries));
    }

    /**
     * 原 addPaintingsToTabs(BuildCreativeModeTabContentsEvent)。
     * 原逻辑：遍历条目找锚点 MASTER_MARISA_PAINTING，找到才在其后 putAfter 8 幅挂画
     * （TabVisibility.PARENT_AND_SEARCH_TABS，连续 putAfter 会反序紧贴锚点）。
     * Fabric 的 FabricItemGroupEntries#addAfter(anchor, collection, visibility) 缺锚点时
     * 会退化为“追加到末尾”，所以先显式检查锚点在场（getDisplayStacks 是实时显示列表），
     * 找不到就整体跳过，保持官方门控。
     */
    public static void addPaintingsToTabs(ResourceKey<CreativeModeTab> tabKey, FabricItemGroupEntries entries) {
        if (tabKey.location().equals(TAVERN_DECO_TAB.location())) {
            Item anchorItem = ModItems.MASTER_MARISA_PAINTING;
            boolean anchorFound = entries.getDisplayStacks().stream().anyMatch(stack -> stack.is(anchorItem));
            if (anchorFound) {
                //调用顺序与官方一致（BFXM 起、QXXY 止），插入结果同为反序紧贴锚点
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.QXXY_PAINTING_ITEM);
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.DREAM_PAINTING_ITEM);
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.CH_PAINTING_ITEM);
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.RABBIT_PAINTING_ITEM);
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.CHA_PAINTING_ITEM);
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.CHEN_PAINTING_ITEM);
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.BMT_PAINTING_ITEM);
                addPaintingAfterAnchor(entries, anchorItem, ModPaintings.BFXM_PAINTING_ITEM);
            }
        }
    }

    /** 等价原 entries.putAfter(anchorItem, new ItemStack(item), TabVisibility.PARENT_AND_SEARCH_TABS)。 */
    private static void addPaintingAfterAnchor(FabricItemGroupEntries entries, Item anchorItem, Item paintingItem) {
        entries.addAfter(anchorItem, List.of(new ItemStack(paintingItem)), TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
