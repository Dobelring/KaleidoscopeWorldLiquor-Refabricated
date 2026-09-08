package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.item.BottledDrinkItem;
import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.Block;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class ModItems {
    private ModItems() {
    }

    /** 1.21.11 的自定义唱片：纯物品 + jukebox_playable 组件指向 jukebox_song 数据。
     *  默认挂长曲；放入唱片机时由 MusicDiscEvents 随机改写为具体曲目的 song
     *  （双曲时长不同，混播随机会让音符粒子/比较器与实际音乐失配） */
    public static final ResourceKey<JukeboxSong> JUKEBOX_SONG_RANDOM_DISC =
            ResourceKey.create(Registries.JUKEBOX_SONG, PortHelper.id("random_disc"));
    public static final ResourceKey<JukeboxSong> JUKEBOX_SONG_RANDOM_DISC_SHORT =
            ResourceKey.create(Registries.JUKEBOX_SONG, PortHelper.id("random_disc_short"));

    // ========== 注册辅助（照 cookery 范式） ==========
    public static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> factory, Item.Properties properties) {
        Item item = factory.apply(properties.setId(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static Item registerItemViaBlock(Block block, BiFunction<Block, Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, block.builtInRegistryHolder().key().identifier());
        return registerItem(key, p -> factory.apply(block, p), properties.useBlockDescriptionPrefix());
    }

    public static Item registerItemViaBlock(Block block, BiFunction<Block, Item.Properties, Item> factory) {
        return registerItemViaBlock(block, factory, new Item.Properties());
    }

    public static Item registerItemViaBlock(Block block) {
        return registerItemViaBlock(block, BlockItem::new);
    }

    public static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        return registerItem(PortHelper.createItemId(name), factory, properties);
    }

    // ========== 18 名酒（DrinkBlockItem 复用前置 tavern 类） ==========
    public static final Item BOMBAY_SAPPHIRE_GIN = registerItemViaBlock(ModBlocks.BOMBAY_SAPPHIRE_GIN, DrinkBlockItem::new);
    public static final Item JACK_DANIEL = registerItemViaBlock(ModBlocks.JACK_DANIEL, DrinkBlockItem::new);
    public static final Item SMIRNOFF_RED_VODKA = registerItemViaBlock(ModBlocks.SMIRNOFF_RED_VODKA, DrinkBlockItem::new);

    public static final Item ABSOLUT_VODKA = registerItemViaBlock(ModBlocks.ABSOLUT_VODKA, DrinkBlockItem::new);
    public static final Item PINA_COLADA = registerItemViaBlock(ModBlocks.PINA_COLADA, DrinkBlockItem::new);
    public static final Item MAOTAI = registerItemViaBlock(ModBlocks.MAOTAI, DrinkBlockItem::new);
    public static final Item BACARDI_CARTA_BLANCA = registerItemViaBlock(ModBlocks.BACARDI_CARTA_BLANCA, DrinkBlockItem::new);
    public static final Item SPIRYT_VODKA = registerItemViaBlock(ModBlocks.SPIRYT_VODKA, DrinkBlockItem::new);
    public static final Item SKYY_VODKA = registerItemViaBlock(ModBlocks.SKYY_VODKA, DrinkBlockItem::new);
    public static final Item JOHNNIE_WALKER = registerItemViaBlock(ModBlocks.JOHNNIE_WALKER, DrinkBlockItem::new);
    public static final Item LAFITE_1982 = registerItemViaBlock(ModBlocks.LAFITE_1982, DrinkBlockItem::new);
    public static final Item STRONGBOW = registerItemViaBlock(ModBlocks.STRONGBOW, DrinkBlockItem::new);
    public static final Item DASSAI = registerItemViaBlock(ModBlocks.DASSAI, DrinkBlockItem::new);
    public static final Item KWAS_CHLEBOWY = registerItemViaBlock(ModBlocks.KWAS_CHLEBOWY, DrinkBlockItem::new);
    public static final Item BAMBOO_LEAF_GREEN_LIQUOR = registerItemViaBlock(ModBlocks.BAMBOO_LEAF_GREEN_LIQUOR, DrinkBlockItem::new);
    public static final Item COOL_TEA = registerItemViaBlock(ModBlocks.COOL_TEA, DrinkBlockItem::new);
    public static final Item SOUR_PLUM = registerItemViaBlock(ModBlocks.SOUR_PLUM, DrinkBlockItem::new);

    // ========== cola / tonic_water（BottledDrinkItem 饮用 + UseRemainder 还瓶） ==========
    public static final Item COLA = registerItem("cola",
            BottledDrinkItem::new,
            new Item.Properties().stacksTo(16).food(
                    new FoodProperties.Builder().alwaysEdible().build(),
                    Consumables.defaultDrink()
                            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HASTE, 300), 1.0F))
                            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SPEED, 300), 1.0F))
                            .build()));
    public static final Item TONIC_WATER = registerItem("tonic_water",
            BottledDrinkItem::new,
            new Item.Properties().stacksTo(16).food(
                    new FoodProperties.Builder().alwaysEdible().build(),
                    Consumables.defaultDrink()
                            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 300), 1.0F))
                            .build()));

    // ========== 6 鸡尾酒 ==========
    public static final Item JERK = registerItemViaBlock(ModBlocks.JERK, CocktailBlockItem::new);
    public static final Item AROUND_THE_WORLD = registerItemViaBlock(ModBlocks.AROUND_THE_WORLD, CocktailBlockItem::new);
    public static final Item LONG_ISLAND_ICED_TEA = registerItemViaBlock(ModBlocks.LONG_ISLAND_ICED_TEA, CocktailBlockItem::new);
    public static final Item SHRIMP_COOKTAIL = registerItemViaBlock(ModBlocks.SHRIMP_COOKTAIL, CocktailBlockItem::new);
    public static final Item PINE_COLADA = registerItemViaBlock(ModBlocks.PINE_COLADA, CocktailBlockItem::new);
    public static final Item GIN_TONIC = registerItemViaBlock(ModBlocks.GIN_TONIC, CocktailBlockItem::new);

    // ========== 16 吧台椅 ==========
    public static final Item BAR_STOOL_BLACK = registerItemViaBlock(ModBlocks.BAR_STOOL_BLACK);
    public static final Item BAR_STOOL_WHITE = registerItemViaBlock(ModBlocks.BAR_STOOL_WHITE);
    public static final Item BAR_STOOL_LIGHT_GRAY = registerItemViaBlock(ModBlocks.BAR_STOOL_LIGHT_GRAY);
    public static final Item BAR_STOOL_GRAY = registerItemViaBlock(ModBlocks.BAR_STOOL_GRAY);
    public static final Item BAR_STOOL_BROWN = registerItemViaBlock(ModBlocks.BAR_STOOL_BROWN);
    public static final Item BAR_STOOL_RED = registerItemViaBlock(ModBlocks.BAR_STOOL_RED);
    public static final Item BAR_STOOL_ORANGE = registerItemViaBlock(ModBlocks.BAR_STOOL_ORANGE);
    public static final Item BAR_STOOL_YELLOW = registerItemViaBlock(ModBlocks.BAR_STOOL_YELLOW);
    public static final Item BAR_STOOL_LIME = registerItemViaBlock(ModBlocks.BAR_STOOL_LIME);
    public static final Item BAR_STOOL_GREEN = registerItemViaBlock(ModBlocks.BAR_STOOL_GREEN);
    public static final Item BAR_STOOL_CYAN = registerItemViaBlock(ModBlocks.BAR_STOOL_CYAN);
    public static final Item BAR_STOOL_LIGHT_BLUE = registerItemViaBlock(ModBlocks.BAR_STOOL_LIGHT_BLUE);
    public static final Item BAR_STOOL_BLUE = registerItemViaBlock(ModBlocks.BAR_STOOL_BLUE);
    public static final Item BAR_STOOL_PURPLE = registerItemViaBlock(ModBlocks.BAR_STOOL_PURPLE);
    public static final Item BAR_STOOL_MAGENTA = registerItemViaBlock(ModBlocks.BAR_STOOL_MAGENTA);
    public static final Item BAR_STOOL_PINK = registerItemViaBlock(ModBlocks.BAR_STOOL_PINK);

    // ========== 11 酒柜/酒窖柜 + 冰柜 ==========
    public static final Item OAK_BAR_CABINET = registerItemViaBlock(ModBlocks.OAK_BAR_CABINET);
    public static final Item OAK_GLASS_BAR_CABINET = registerItemViaBlock(ModBlocks.OAK_GLASS_BAR_CABINET);
    public static final Item BIRCH_BAR_CABINET = registerItemViaBlock(ModBlocks.BIRCH_BAR_CABINET);
    public static final Item SPRUCE_BAR_CABINET = registerItemViaBlock(ModBlocks.SPRUCE_BAR_CABINET);
    public static final Item DARK_OAK_BAR_CABINET = registerItemViaBlock(ModBlocks.DARK_OAK_BAR_CABINET);
    public static final Item CHERRY_BAR_CABINET = registerItemViaBlock(ModBlocks.CHERRY_BAR_CABINET);
    public static final Item OAK_CELLAR_CABINET = registerItemViaBlock(ModBlocks.OAK_CELLAR_CABINET);
    public static final Item BIRCH_CELLAR_CABINET = registerItemViaBlock(ModBlocks.BIRCH_CELLAR_CABINET);
    public static final Item SPRUCE_CELLAR_CABINET = registerItemViaBlock(ModBlocks.SPRUCE_CELLAR_CABINET);
    public static final Item DARK_OAK_CELLAR_CABINET = registerItemViaBlock(ModBlocks.DARK_OAK_CELLAR_CABINET);
    public static final Item CHERRY_CELLAR_CABINET = registerItemViaBlock(ModBlocks.CHERRY_CELLAR_CABINET);
    public static final Item FREEZER = registerItemViaBlock(ModBlocks.FREEZER);

    // ========== 唱片 ==========
    public static final Item CUSTOM_RECORD = registerItem("custom_record",
            p -> new Item(p.jukeboxPlayable(JUKEBOX_SONG_RANDOM_DISC)),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    /** 静态字段随类加载完成注册，此方法仅用于在 onInitialize 中固定初始化顺序 */
    public static void register() {
    }
}
