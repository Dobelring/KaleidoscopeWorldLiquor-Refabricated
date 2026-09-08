package com.bmt.kaleidoscope_world_liquor.blockentity;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.ItemStack;

/**
 * 墙上唱片 BE：存一张唱片。字段名与 1.20.1 一致（Record）。
 */
public class WallRecordBlockEntity extends BaseBlockEntity {
    private static final String RECORD = "Record";

    private ItemStack record = ItemStack.EMPTY;

    public WallRecordBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WALL_RECORD_BE, pos, state);
    }

    public ItemStack getRecord() {
        return this.record;
    }

    public void setRecord(ItemStack record) {
        this.record = record;
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.record = valueInput.read(RECORD, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (!this.record.isEmpty()) {
            valueOutput.store(RECORD, ItemStack.CODEC, this.record);
        }
    }
}
