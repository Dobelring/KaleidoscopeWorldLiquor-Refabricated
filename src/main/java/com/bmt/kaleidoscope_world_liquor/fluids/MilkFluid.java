package com.bmt.kaleidoscope_world_liquor.fluids;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public abstract class MilkFluid extends FlowingFluid {
   private final Supplier<? extends FlowingFluid> flowing;
   private final Supplier<? extends FlowingFluid> still;
   private final Supplier<? extends Item> bucket;
   private final Supplier<? extends Block> block;

   protected MilkFluid(
      Supplier<? extends FlowingFluid> flowing,
      Supplier<? extends FlowingFluid> still,
      Supplier<? extends Item> bucket,
      Supplier<? extends Block> block
   ) {
      this.flowing = flowing;
      this.still = still;
      this.bucket = bucket;
      this.block = block;
      this.registerDefaultState(this.stateDefinition.any().setValue(FlowingFluid.LEVEL, 8));
   }

   @Override
   public @NotNull Fluid getFlowing() {
      return this.flowing.get();
   }

   @Override
   public @NotNull Fluid getSource() {
      return this.still.get();
   }

   @Override
   public @NotNull Item getBucket() {
      return this.bucket.get();
   }

   @Override
   protected boolean canConvertToSource(Level level) {
      return false;
   }

   @Override
   protected int getSlopeFindDistance(LevelReader level) {
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
   public @NotNull BlockState createLegacyBlock(FluidState state) {
      return this.block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
   }

   @Override
   protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
      BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
      Block.dropResources(state, level, pos, blockEntity);
   }

   @Override
   public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
      return direction == Direction.DOWN && !fluid.isSame(this);
   }

   @Override
   public boolean isSame(Fluid fluid) {
      return fluid == this.getFlowing() || fluid == this.getSource();
   }

   @Override
   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(FlowingFluid.LEVEL);
   }

   public static class Flowing extends MilkFluid {
      public Flowing(
         Supplier<? extends FlowingFluid> flowing,
         Supplier<? extends FlowingFluid> still,
         Supplier<? extends Item> bucket,
         Supplier<? extends Block> block
      ) {
         super(flowing, still, bucket, block);
      }

      @Override
      public boolean isSource(FluidState state) {
         return false;
      }

      @Override
      public int getAmount(FluidState state) {
         return state.getValue(FlowingFluid.LEVEL);
      }
   }

   public static class Still extends MilkFluid {
      public Still(
         Supplier<? extends FlowingFluid> flowing,
         Supplier<? extends FlowingFluid> still,
         Supplier<? extends Item> bucket,
         Supplier<? extends Block> block
      ) {
         super(flowing, still, bucket, block);
      }

      @Override
      public boolean isSource(FluidState state) {
         return true;
      }

      @Override
      public int getAmount(FluidState state) {
         return state.getValue(FlowingFluid.LEVEL);
      }
   }
}
