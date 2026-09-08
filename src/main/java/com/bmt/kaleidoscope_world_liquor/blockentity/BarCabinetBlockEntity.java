package com.bmt.kaleidoscope_world_liquor.blockentity;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 酒柜 BE：左右双槽 + 单槽模式（异形酒瓶独占时）。
 * 字段名与 1.20.1 一致（left_item/right_item/is_single）。
 */
public class BarCabinetBlockEntity extends BaseBlockEntity {
    private static final String LEFT = "left_item";
    private static final String RIGHT = "right_item";
    private static final String SINGLE = "is_single";

    private ItemStack leftItem = ItemStack.EMPTY;
    private ItemStack rightItem = ItemStack.EMPTY;
    private boolean isSingle = false;

    public BarCabinetBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
        super(ModBlockEntities.BAR_CABINET_BE, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.leftItem = valueInput.read(LEFT, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.rightItem = valueInput.read(RIGHT, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.isSingle = valueInput.getBooleanOr(SINGLE, false);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (!this.leftItem.isEmpty()) {
            valueOutput.store(LEFT, ItemStack.CODEC, this.leftItem);
        }
        if (!this.rightItem.isEmpty()) {
            valueOutput.store(RIGHT, ItemStack.CODEC, this.rightItem);
        }
        valueOutput.putBoolean(SINGLE, this.isSingle);
    }

    public ItemStack getLeftItem() {
        return this.leftItem;
    }

    public void setLeftItem(ItemStack leftItem) {
        this.leftItem = leftItem;
    }

    public ItemStack getRightItem() {
        return this.rightItem;
    }

    public void setRightItem(ItemStack rightItem) {
        this.rightItem = rightItem;
    }

    public void setSingle(boolean single) {
        this.isSingle = single;
    }

    public boolean isSingle() {
        return this.isSingle;
    }
}
