package com.bmt.kaleidoscope_world_liquor.init.smc;

import com.bmt.kaleidoscope_world_liquor.item.CustomDrinkItem;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 「森罗厨（smc）」命名空间的物品/方块代注册（原 Forge 版由主类
 * {@code SMCItems.register(modEventBus)} 无条件挂到 mod 总线，类内没有任何
 * isModLoaded 判断——本类同样无条件注册，由主类调用 {@link #registerSMCItems()}）。
 */
public class SMCItems {
    public static final String SMC_MODID = "smc";
    public static final Block SMC_ICE_TEA_BLOCK = DrinkBlock.create()
        .maxCount(4)
        .shapes(
            new VoxelShape[]{
                Block.box(4.0, 0.0, 5.0, 12.0, 14.0, 11.0),
                Block.box(0.5, 0.0, 4.0, 15.5, 14.0, 12.0),
                Shapes.or(Block.box(0.5, 0.0, 7.0, 15.5, 14.0, 15.5), Block.box(4.0, 0.0, 0.5, 12.0, 14.0, 7.0)),
                Block.box(0.5, 0.0, 0.5, 15.5, 14.0, 15.5)
            }
        )
        .build();
    public static final Item SMC_ICE_TEA_ITEM = new CustomDrinkItem.IceTea(SMC_ICE_TEA_BLOCK);

    public SMCItems() {
    }

    public static void registerSMCItems() {
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(SMC_MODID, "ice_tea"), SMC_ICE_TEA_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(SMC_MODID, "ice_tea"), SMC_ICE_TEA_ITEM);
    }
}
