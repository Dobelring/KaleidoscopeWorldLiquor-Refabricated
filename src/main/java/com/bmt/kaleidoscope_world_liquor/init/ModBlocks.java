package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.ChairBlock;
import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.WallRecordBlock;
import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {
    }

    // ========== 注册辅助（照 cookery/tavern 范式） ==========
    public static Block register(ResourceKey<Block> key, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static Block commonReg(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        return register(PortHelper.createBlockId(name), factory, properties);
    }

    private static VoxelShape[] drinkShapes(double height, double inset) {
        // 1.20.1 原版 18 名酒的四组朝向碰撞箱：竖放/横放/横放旋转/平放，height 为瓶高，inset 为侧向内缩
        double c1 = 6.0, c2 = 10.0;        // 竖放内圈
        double s1 = 2.0, s2 = 14.0;        // 横放外圈
        double h = height, half1 = 6.0, half2 = 10.0;
        return new VoxelShape[]{
                Block.box(c1 - inset / 2, 0, c1 - inset / 2, c2 + inset / 2, h, c2 + inset / 2),
                Block.box(s1, 0, 5.0 + inset / 2, s2, h, 11.0 - inset / 2),
                Shapes.or(Block.box(s1, 0, 10.0 - inset / 2 + 4, s2, h, 14.0 + inset / 2),
                        Block.box(6.0 - inset / 2, 0, 1.0 + inset / 2, 10.0 + inset / 2, h, 5.0 - inset / 2 + 4)),
                Block.box(1.0 + inset / 2, 0, 1.0 + inset / 2, 15.0 - inset / 2, h, 15.0 - inset / 2)
        };
    }

    private static Block drinkReg(String name, double height, double inset) {
        // 26.1.2 tavern DrinkBlock.Builder.setId 收纯名称字符串；方块内部 Properties 以
        // tavern 命名空间生成描述键（block.kaleidoscope_tavern.<name>），liquor lang 已补齐键。
        // 注册键仍为 liquor 命名空间（commonReg→register(key, ...)）。
        return commonReg(name, p -> DrinkBlock.create()
                .setId(name)
                .maxCount(4)
                .shapes(drinkShapes(height, inset))
                .build(), BlockBehaviour.Properties.of());
    }

    // ========== 18 名酒（DrinkBlock 复用前置 tavern 类，liquor ns 注册） ==========
    public static final Block BOMBAY_SAPPHIRE_GIN = drinkReg("bombay_sapphire_gin", 16.0, 0.0);
    public static final Block JACK_DANIEL = drinkReg("jack_daniel", 13.0, 1.0);
    public static final Block SMIRNOFF_RED_VODKA = drinkReg("smirnoff_red_vodka", 16.0, 0.0);
    public static final Block ABSOLUT_VODKA = drinkReg("absolut_vodka", 14.0, 1.0);
    public static final Block PINA_COLADA = drinkReg("pina_colada", 15.0, 0.0);
    public static final Block MAOTAI = drinkReg("maotai", 13.0, 1.0);
    public static final Block BACARDI_CARTA_BLANCA = drinkReg("bacardi_carta_blanca", 16.0, 0.0);
    public static final Block SPIRYT_VODKA = drinkReg("spiryt_vodka", 16.0, 0.0);
    public static final Block SKYY_VODKA = drinkReg("skyy_vodka", 16.0, 0.0);
    public static final Block JOHNNIE_WALKER = drinkReg("johnnie_walker", 14.0, 0.0);
    public static final Block LAFITE_1982 = drinkReg("lafite_1982", 16.0, 0.0);
    public static final Block STRONGBOW = drinkReg("strongbow", 16.0, 0.0);
    public static final Block DASSAI = drinkReg("dassai", 16.0, 0.0);
    public static final Block KWAS_CHLEBOWY = drinkReg("kwas_chlebowy", 16.0, 1.0);
    public static final Block BAMBOO_LEAF_GREEN_LIQUOR = drinkReg("bamboo_leaf_green_liquor", 13.0, 1.0);

    // cool_tea / sour_plum 是矮罐造型，碰撞箱与酒瓶不同（1.20.1 原版为 box(4,0,5,12,14,11) 系）
    public static VoxelShape[] canShapes(double height) {
        return new VoxelShape[]{
                Block.box(4, 0, 5, 12, height, 11),
                Block.box(0.5, 0, 4, 15.5, height, 12),
                Shapes.or(Block.box(0.5, 0, 7, 15.5, height, 15.5), Block.box(4, 0, 0.5, 12, height, 7)),
                Block.box(0.5, 0, 0.5, 15.5, height, 15.5)
        };
    }

    private static Block canDrinkReg(String name, double height) {
        return commonReg(name, p -> DrinkBlock.create()
                .setId(name)
                .maxCount(4)
                .shapes(canShapes(height))
                .build(), BlockBehaviour.Properties.of());
    }

    public static final Block COOL_TEA = canDrinkReg("cool_tea", 14.0);
    public static final Block SOUR_PLUM = canDrinkReg("sour_plum", 14.0);

    // ========== 16 吧台椅 ==========
    private static Block stoolReg(String name) {
        return commonReg(name, ChairBlock::new, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(1.0F)
                .noOcclusion());
    }

    public static final Block BAR_STOOL_BLACK = stoolReg("bar_stool_black");
    public static final Block BAR_STOOL_WHITE = stoolReg("bar_stool_white");
    public static final Block BAR_STOOL_LIGHT_GRAY = stoolReg("bar_stool_light_gray");
    public static final Block BAR_STOOL_GRAY = stoolReg("bar_stool_gray");
    public static final Block BAR_STOOL_BROWN = stoolReg("bar_stool_brown");
    public static final Block BAR_STOOL_RED = stoolReg("bar_stool_red");
    public static final Block BAR_STOOL_ORANGE = stoolReg("bar_stool_orange");
    public static final Block BAR_STOOL_YELLOW = stoolReg("bar_stool_yellow");
    public static final Block BAR_STOOL_LIME = stoolReg("bar_stool_lime");
    public static final Block BAR_STOOL_GREEN = stoolReg("bar_stool_green");
    public static final Block BAR_STOOL_CYAN = stoolReg("bar_stool_cyan");
    public static final Block BAR_STOOL_LIGHT_BLUE = stoolReg("bar_stool_light_blue");
    public static final Block BAR_STOOL_BLUE = stoolReg("bar_stool_blue");
    public static final Block BAR_STOOL_PURPLE = stoolReg("bar_stool_purple");
    public static final Block BAR_STOOL_MAGENTA = stoolReg("bar_stool_magenta");
    public static final Block BAR_STOOL_PINK = stoolReg("bar_stool_pink");

    // ========== 6 鸡尾酒（CocktailBlock 复用前置 tavern 类） ==========
    private static Block cocktailReg(String name) {
        return commonReg(name, CocktailBlock::new, BlockBehaviour.Properties.of());
    }

    public static final Block JERK = cocktailReg("jerk");
    public static final Block AROUND_THE_WORLD = cocktailReg("around_the_world");
    public static final Block LONG_ISLAND_ICED_TEA = cocktailReg("long_island_iced_tea");
    public static final Block SHRIMP_COOKTAIL = cocktailReg("shrimp_cocktail");
    public static final Block PINE_COLADA = cocktailReg("pine_colada");
    public static final Block GIN_TONIC = cocktailReg("gin_tonic");

    // ========== 冰柜（BE 逻辑见 block/entity/FreezerBlockEntity） ==========
    public static final Block FREEZER = commonReg("freezer", FreezerBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL)
            .strength(5.0F, 1200.0F)
            .noOcclusion());

    // ========== 墙上唱片（无 BlockItem；唱片由 MusicDiscEvents 放置，第 9 步接） ==========
    public static final Block WALL_RECORD = commonReg("wall_record", WallRecordBlock::new, BlockBehaviour.Properties.of()
            .noCollision()
            .instabreak()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.DESTROY));

    // ========== 11 酒柜/酒窖柜（noOcclusion 必须：贴面取光不能走整块遮挡，否则联排内侧发黑——1.20.1 同款） ==========
    private static Block barCabinetReg(String name) {
        return commonReg(name, BarCabinetBlock::new, BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .ignitedByLava());
    }

    private static Block cellarCabinetReg(String name) {
        return commonReg(name, BarCellarCabinetBlock::new, BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .ignitedByLava());
    }

    public static final Block OAK_BAR_CABINET = barCabinetReg("oak_bar_cabinet");
    public static final Block OAK_GLASS_BAR_CABINET = barCabinetReg("oak_glass_bar_cabinet");
    public static final Block BIRCH_BAR_CABINET = barCabinetReg("birch_bar_cabinet");
    public static final Block SPRUCE_BAR_CABINET = barCabinetReg("spruce_bar_cabinet");
    public static final Block DARK_OAK_BAR_CABINET = barCabinetReg("dark_oak_bar_cabinet");
    public static final Block CHERRY_BAR_CABINET = barCabinetReg("cherry_bar_cabinet");
    public static final Block OAK_CELLAR_CABINET = cellarCabinetReg("oak_cellar_cabinet");
    public static final Block BIRCH_CELLAR_CABINET = cellarCabinetReg("birch_cellar_cabinet");
    public static final Block SPRUCE_CELLAR_CABINET = cellarCabinetReg("spruce_cellar_cabinet");
    public static final Block DARK_OAK_CELLAR_CABINET = cellarCabinetReg("dark_oak_cellar_cabinet");
    public static final Block CHERRY_CELLAR_CABINET = cellarCabinetReg("cherry_cellar_cabinet");

    // ========== 牛奶流体方块（ModFluids.register() 须先于本类静态加载） ==========
    public static final Block MILK_LIQUID_BLOCK = commonReg("milk_liquid",
            p -> new LiquidBlock(ModFluids.MILK_STILL, p),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .noCollision()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable());

    // ========== 8 幅作者画（tavern 命名空间 PaintingBlock，liquor 提供数据/资产/创造栏） ==========
    public static Block paintingReg(String name) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("kaleidoscope_tavern", name));
        return Registry.register(BuiltInRegistries.BLOCK, key,
                new com.github.ysbbbbbb.kaleidoscopetavern.block.deco.PaintingBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.WOOD)
                                .strength(1.0F)
                                .sound(SoundType.WOOD)
                                .noOcclusion()
                                .pushReaction(PushReaction.DESTROY)
                                .setId(key)));
    }

    public static final Block BFXM_PAINTING = paintingReg("bfxm_painting");
    public static final Block BMT_PAINTING = paintingReg("bmt_painting");
    public static final Block DREAM_PAINTING = paintingReg("dream_painting");
    public static final Block CHA_PAINTING = paintingReg("cha_painting");
    public static final Block CHEN_PAINTING = paintingReg("chen_painting");
    public static final Block RABBIT_PAINTING = paintingReg("rabbit_painting");
    public static final Block CH_PAINTING = paintingReg("ch_painting");
    public static final Block QXXY_PAINTING = paintingReg("qxxy_painting");
    /** 静态字段随类加载完成注册，此方法仅用于在 onInitialize 中固定初始化顺序 */
    public static void register() {
    }

    /**
     * 复用 tavern DrinkBlock 的方块必须登记进 tavern 的 DRINK_BE 类型：
     * 26.1.2 Fabric API 在 BlockEntityType 上提供 addValidBlock(default 方法)，
     * 直接把 liquor 方块加入支持集（tavern 26.1.2 构造时写死 24 方块，无 addSupportedBlock）。
     * 共 18 方块：16 名酒 + cool_tea/sour_plum 两罐 + smc:ice_tea。
     */
    public static void registerDrinkBeSupportedBlocks() {
        var drinkBe = com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.DRINK_BE;
        for (Block block : new Block[]{
                BOMBAY_SAPPHIRE_GIN, JACK_DANIEL, SMIRNOFF_RED_VODKA, ABSOLUT_VODKA,
                PINA_COLADA, MAOTAI, BACARDI_CARTA_BLANCA, SPIRYT_VODKA, SKYY_VODKA,
                JOHNNIE_WALKER, LAFITE_1982, STRONGBOW, DASSAI, KWAS_CHLEBOWY,
                BAMBOO_LEAF_GREEN_LIQUOR, COOL_TEA, SOUR_PLUM,
                com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems.SMC_ICE_TEA_BLOCK}) {
            drinkBe.addValidBlock(block);
        }
    }
}
