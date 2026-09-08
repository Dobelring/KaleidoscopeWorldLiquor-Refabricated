package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.block.DollBlock;
import com.bmt.kaleidoscope_world_liquor.item.DollItem;
import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩偶联动（1.21.11 无 kaleidoscope_doll 模组，liquor 自实现 DollBlock/DollItem）。
 * doll_0..5 以 kaleidoscope_world_liquor 命名空间无条件注册（与 1.20.1 一致：
 * 贡献者收藏品，无合成配方由本模组提供，切石机配方在数据包里）。
 * 作者名与 tooltip 键对应 contributor_0..5。
 */
public final class DollIntegration {
    private static final Map<String, String> AUTHOR_DOLL_DEFINITIONS = new LinkedHashMap<>();
    /** 注册后的全部玩偶方块（客户端渲染层/图鉴用） */
    public static final List<Block> DOLL_BLOCKS = new ArrayList<>();

    private DollIntegration() {
    }

    public static void register() {
        AUTHOR_DOLL_DEFINITIONS.put("doll_0", "旧梦");
        AUTHOR_DOLL_DEFINITIONS.put("doll_1", "白馒头");
        AUTHOR_DOLL_DEFINITIONS.put("doll_2", "茶枣子");
        AUTHOR_DOLL_DEFINITIONS.put("doll_3", "1335");
        AUTHOR_DOLL_DEFINITIONS.put("doll_4", "辰");
        AUTHOR_DOLL_DEFINITIONS.put("doll_5", "小兔子要听话哦");

        for (Map.Entry<String, String> entry : AUTHOR_DOLL_DEFINITIONS.entrySet()) {
            String name = entry.getKey();
            String tooltipKey = "tooltip.kaleidoscope_doll.doll.contributor_" + name.substring(name.length() - 1);
            Block block = Registry.register(BuiltInRegistries.BLOCK, PortHelper.createBlockId(name),
                    new DollBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_LIGHT_GRAY)
                            .strength(0.0F, 10.0F)
                            .sound(SoundType.WOOL)
                            .pushReaction(PushReaction.DESTROY)
                            .noOcclusion()
                            .setId(PortHelper.createBlockId(name))));
            DOLL_BLOCKS.add(block);
            Item item = new DollItem(block, tooltipKey,
                    new Item.Properties().useBlockDescriptionPrefix().setId(PortHelper.createItemId(name)));
            ((net.minecraft.world.item.BlockItem) item).registerBlocks(Item.BY_BLOCK, item);
            Registry.register(BuiltInRegistries.ITEM, PortHelper.createItemId(name), item);
        }
    }
}
