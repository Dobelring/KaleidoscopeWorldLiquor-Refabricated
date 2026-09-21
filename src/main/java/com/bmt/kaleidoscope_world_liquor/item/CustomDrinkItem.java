package com.bmt.kaleidoscope_world_liquor.item;

import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.block.Block;

/**
 * 带自定义饮用音效的饮品（1.1.9 起官方把音效从事件层搬到物品层）。
 *
 * <p>26.1.2 适配：{@code Item#getDrinkingSound()}/{@code getEatingSound()} 已不存在，
 * 饮用音效由 {@code CONSUMABLE} 组件的 {@code sound} 承载
 * （{@code ItemStack.onUseTick} 读该组件、按 vanilla 节奏播放）。
 *
 * <p><b>坑</b>：tavern 的 {@code DrinkBlockItem} 构造器会强行
 * {@code properties.component(CONSUMABLE, Consumables.DEFAULT_DRINK)} 覆盖音效，
 * 所以在外面把组件塞进 {@code Item.Properties} 是无效的（听到的会是原版通用饮水音）。
 * 这里用 {@link KeepCustomConsumableProperties} 拦下那次覆盖；因为要拿到这个子类实例，
 * Properties 必须由本类自己创建——物品 id 从方块键推得（与 {@code registerItemViaBlock} 同款），
 * 描述键照旧 {@code useBlockDescriptionPrefix()}。
 *
 * <p>只设 CONSUMABLE、不设 FOOD：这些饮品本来就不是食物，加 FOOD 会变成可食用物品。
 */
public abstract class CustomDrinkItem extends DrinkBlockItem {
    protected CustomDrinkItem(Block block, SoundEvent sound) {
        super(block, customSoundProperties(block, sound));
    }

    private static Item.Properties customSoundProperties(Block block, SoundEvent sound) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, block.builtInRegistryHolder().key().identifier());
        return new KeepCustomConsumableProperties()
                .useBlockDescriptionPrefix()
                .component(DataComponents.CONSUMABLE, consumable(sound))
                .setId(key);
    }

    private static Consumable consumable(SoundEvent sound) {
        return Consumables.defaultDrink()
                .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound))
                .build();
    }

    /** 酒柜客户端拦截判定：手持物是否带本模组的自定义喝声音效。 */
    public static boolean hasCustomSound(ItemStack stack) {
        return stack.getItem() instanceof CustomDrinkItem;
    }

    /**
     * 只拦 tavern 对 CONSUMABLE 的**那次覆盖**：本类自己的设置是第一次调用，放行；
     * 之后 DrinkBlockItem 构造器里的 {@code component(CONSUMABLE, DEFAULT_DRINK)} 被吞掉，
     * 其余属性（stacksTo / craftRemainder 等）照常放行。
     */
    private static final class KeepCustomConsumableProperties extends Item.Properties {
        private boolean customConsumableSet;

        @Override
        public <T> Item.Properties component(DataComponentType<T> type, T value) {
            if (type == DataComponents.CONSUMABLE) {
                if (this.customConsumableSet) {
                    return this;
                }
                this.customConsumableSet = true;
            }
            return super.component(type, value);
        }
    }

    public static class CoolTea extends CustomDrinkItem {
        public CoolTea(Block block) {
            super(block, ModSounds.COOL_ICE_TEA_DRINK);
        }
    }

    public static class IceTea extends CustomDrinkItem {
        public IceTea(Block block) {
            super(block, ModSounds.ICE_TEA_EAT);
        }
    }

    public static class SourPlum extends CustomDrinkItem {
        public SourPlum(Block block) {
            super(block, ModSounds.SOUR_PLUM_DRINK);
        }
    }
}
