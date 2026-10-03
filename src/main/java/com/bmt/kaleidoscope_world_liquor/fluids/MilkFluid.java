package com.bmt.kaleidoscope_world_liquor.fluids;

import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class MilkFluid extends FlowingFluid {
    protected MilkFluid() {
        this.registerDefaultState((FluidState)((FluidState)this.stateDefinition.any()).setValue(LEVEL, 8));
    }

    protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    public Fluid getFlowing() {
        return ModFluids.MILK_FLOWING;
    }

    @Override
    public Fluid getSource() {
        return ModFluids.MILK_STILL;
    }

    @Override
    public Item getBucket() {
        return Items.MILK_BUCKET;
    }

    // 官方 Forge FluidType：canConvertToSource(false)
    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    public int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.MILK_LIQUID_BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == this.getFlowing() || fluid == this.getSource();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.isSame(this);
    }

    public static class Flowing extends MilkFluid {
        public Flowing() {
            super();
        }

        public boolean isSource(FluidState state) {
            return false;
        }

        public int getAmount(FluidState state) {
            return (Integer)state.getValue(LEVEL);
        }
    }

    public static class Still extends MilkFluid {
        public Still() {
            super();
        }

        public boolean isSource(FluidState state) {
            return true;
        }

        public int getAmount(FluidState state) {
            return 8;
        }
    }
}
