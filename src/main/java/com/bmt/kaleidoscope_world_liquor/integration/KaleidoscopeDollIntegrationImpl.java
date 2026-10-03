package com.bmt.kaleidoscope_world_liquor.integration;

import com.github.ysbbbbbb.kaleidoscopedoll.block.DollBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollItem;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 玩偶联动的实际实现（只有玩偶模组存在、且 kaleidoscope_nether 缺失时才会被加载）。
 * <p>
 * Forge 版依赖 {@code RegisterEvent}（BLOCK / ITEM 两次）与
 * {@code BuildCreativeModeTabContentsEvent}（{@code ModCreativeTabs.AUTHOR_DOLL_TAB}）；
 * Fabric 无 mod 总线，改为在这里一次性注册方块 + 物品（eager + Registry.register），
 * 并把条目写进玩偶模组的三个静态表。
 * <p>
 * javap 核对（libs/kaleidoscopedoll-1.20.1-fabric-1.0.9.jar）：Fabric 版玩偶模组
 * <b>没有 AUTHOR_DOLL_TAB</b>（ModCreativeTabs 只有 private 的 vanilla_doll /
 * player_doll / entity_doll 三个标签页，且 ModRegisterEvent#registerBlocks 只注册
 * kaleidoscope_doll:doll_* 自己的玩偶），作者玩偶改由这三个静态表驱动：
 * DOLL_ITEMS 中且在 SPECIAL_TOOLTIPS 里有键的物品 → 进入玩偶的 player_doll 标签页
 * （等价于原 Forge 往 AUTHOR_DOLL_TAB 里 accept），因此这里不再使用 ItemGroupEvents。
 */
class KaleidoscopeDollIntegrationImpl {
    private static final Map<ResourceLocation, Block> DOLL_BLOCKS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Item> DOLL_ITEMS = new LinkedHashMap<>();
    private static final Map<String, String> AUTHOR_DOLL_DEFINITIONS = Map.of(
        "doll_0",
        "contributor_0",
        "doll_1",
        "contributor_1",
        "doll_2",
        "contributor_2",
        "doll_3",
        "contributor_3",
        "doll_4",
        "contributor_4",
        "doll_5",
        "contributor_5"
    );

    KaleidoscopeDollIntegrationImpl() {
    }

    /**
     * 原 Forge 版是 {@code register(IEventBus)} + 两个 {@code @SubscribeEvent(RegisterEvent)}
     * （registerBlocks / registerItems）+ 一个
     * {@code @SubscribeEvent(BuildCreativeModeTabContentsEvent)}（addToCreativeTab）。
     * 注意原方法名 {@code isDollModLoaded()} 的语义是反的（返回「未加载」），
     * 事件处理器里都写成 {@code if (!isDollModLoaded())} 才执行，此处保留原样。
     */
    static void register() {
        if (!isDollModLoaded()) {
            AUTHOR_DOLL_DEFINITIONS.forEach((dollId, tooltipKey) -> {
                ResourceLocation id = new ResourceLocation("kaleidoscope_world_liquor", dollId);

                // 原 registerBlocks(RegisterEvent)：注册方块
                DollBlock block = new DollBlock();
                Registry.register(BuiltInRegistries.BLOCK, id, block);
                DOLL_BLOCKS.put(id, block);

                // 原 registerItems(RegisterEvent)：注册物品（原版此处先从 DOLL_BLOCKS 取方块，
                // Fabric 单次遍历内方块刚刚注册，必然非 null）
                DollItem item = new DollItem(block, tooltipKey);
                Registry.register(BuiltInRegistries.ITEM, id, item);
                DOLL_ITEMS.put(id, item);

                // 原 Forge：ModRegisterEvent.SPECIAL_TOOLTIPS.put(id, tooltipKey)
                //  Fabric 版玩偶模组的玩偶表由这三个静态表驱动：
                //   - DOLL_ITEMS + SPECIAL_TOOLTIPS 里都存在的物品 → 进入玩偶的 player_doll 标签页
                //     （等价于原 Forge 的 addToCreativeTab 往 AUTHOR_DOLL_TAB 里 accept）；
                //   - DOLL_BLOCKS → 玩偶客户端会为这些方块注册 cutout 渲染层，并参与幻影等逻辑。
                ModRegisterEvent.DOLL_BLOCKS.put(id, block);
                ModRegisterEvent.DOLL_ITEMS.add(item);
                ModRegisterEvent.SPECIAL_TOOLTIPS.put(id, tooltipKey);
            });
        }
    }

    /**
     * 原 Forge 版同名方法（语义是反的：返回 true 表示「玩偶模组未加载」）。
     * Fabric 上守卫由 {@link KaleidoscopeDollIntegration#register()} 把门，此处保留原判断。
     */
    private static boolean isDollModLoaded() {
        return !FabricLoader.getInstance().isModLoaded("kaleidoscope_doll");
    }
}
