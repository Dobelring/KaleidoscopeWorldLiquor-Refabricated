package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;

/**
 * 模组/联动 tag 常量。bar_cabinet_irregular 与 cellar_cabinet_blacklist
 * 的 JSON 数据已在第 3 步迁移（kaleidoscope_tavern 命名空间）。
 */
public final class ModTags {
    private ModTags() {
    }

    /** 酒柜异形酒瓶（占双槽） */
    public static final TagKey<Item> BAR_CABINET_IRREGULAR = itemTag(KaleidoscopeWorldLiquor.MOD_ID, "bar_cabinet_irregular");
    /** 酒柜可放物品 */
    public static final TagKey<Item> BAR_CABINET_PLACEABLE = itemTag(KaleidoscopeWorldLiquor.MOD_ID, "bar_cabinet_placeable");
    /** 酒窖柜可放物品 */
    public static final TagKey<Item> BAR_CELLAR_CABINET_PLACEABLE = itemTag(KaleidoscopeWorldLiquor.MOD_ID, "bar_cellar_cabinet_placeable");
    /** 酒窖柜原生酒黑名单（tavern 1.20.1 即为空 tag） */
    public static final TagKey<Item> BAR_CELLAR_CABINET_NATIVE_BLACKLIST = itemTag("kaleidoscope_tavern", "cellar_cabinet_blocklist");

    /** boss 实体（斩首/掉落判定用） */
    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "bosses"));

    private static TagKey<Item> itemTag(String namespace, String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, name));
    }
}
