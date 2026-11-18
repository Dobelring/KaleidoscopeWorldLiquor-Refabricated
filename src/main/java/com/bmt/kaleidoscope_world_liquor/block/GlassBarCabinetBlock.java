package com.bmt.kaleidoscope_world_liquor.block;

import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.PositionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * 官方 1.2.0：玻璃酒柜——继承酒柜，额外带 wood_side 状态（与相邻的同木种实木柜拼接时显示侧板）。
 */
public class GlassBarCabinetBlock extends BarCabinetBlock {
   public static final EnumProperty<GlassBarCabinetBlock.WoodSide> WOOD_SIDE = EnumProperty.create("wood_side", GlassBarCabinetBlock.WoodSide.class);
   private static final VoxelShape FULL_SAFE_SHAPE = BarCabinetBlock.FULL_SAFE_SHAPE;

   public GlassBarCabinetBlock(Properties properties, WoodType woodType) {
      super(properties, woodType);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(WOOD_SIDE, GlassBarCabinetBlock.WoodSide.NONE));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{WOOD_SIDE});
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = super.getStateForPlacement(context);
      return state == null ? null : (BlockState)state.setValue(WOOD_SIDE, this.getWoodSide(state, context.getLevel(), context.getClickedPos()));
   }

   @Override
   protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess tickAccess, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull RandomSource random) {
      BlockState updated = super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
      return (BlockState)updated.setValue(WOOD_SIDE, this.getWoodSide(updated, level, pos));
   }

   private GlassBarCabinetBlock.WoodSide getWoodSide(BlockState state, BlockGetter level, BlockPos pos) {
      Direction facing = (Direction)state.getValue(FACING);
      boolean leftWood = this.isSameWoodCabinet(level.getBlockState(pos.relative(facing.getClockWise())), facing);
      boolean rightWood = this.isSameWoodCabinet(level.getBlockState(pos.relative(facing.getCounterClockWise())), facing);

      return switch ((PositionType)state.getValue(POSITION)) {
         case LEFT -> rightWood ? GlassBarCabinetBlock.WoodSide.RIGHT : GlassBarCabinetBlock.WoodSide.NONE;
         case RIGHT -> leftWood ? GlassBarCabinetBlock.WoodSide.LEFT : GlassBarCabinetBlock.WoodSide.NONE;
         case MIDDLE -> leftWood && rightWood
            ? GlassBarCabinetBlock.WoodSide.BOTH
            : (leftWood ? GlassBarCabinetBlock.WoodSide.LEFT : (rightWood ? GlassBarCabinetBlock.WoodSide.RIGHT : GlassBarCabinetBlock.WoodSide.NONE));
         default -> GlassBarCabinetBlock.WoodSide.NONE;
      };
   }

   private boolean isSameWoodCabinet(BlockState state, Direction facing) {
      return state.getBlock() instanceof BarCabinetBlock block
         && !(block instanceof GlassBarCabinetBlock)
         && block.getWoodType() == this.getWoodType()
         && state.getValue(FACING) == facing;
   }

   public static enum WoodSide implements StringRepresentable {
      NONE("none"),
      LEFT("left"),
      RIGHT("right"),
      BOTH("both");

      private final String name;

      private WoodSide(String name) {
         this.name = name;
      }

      @Override
      public String getSerializedName() {
         return this.name;
      }
   }
}
