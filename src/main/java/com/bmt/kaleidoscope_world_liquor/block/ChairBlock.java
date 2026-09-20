package com.bmt.kaleidoscope_world_liquor.block;

import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChairBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final MapCodec<ChairBlock> CODEC = simpleCodec(properties -> new ChairBlock());
   private static final double SIT_HEIGHT = 0.9;
   private static final VoxelShape SHAPE = Shapes.or(
      Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0), new VoxelShape[]{Block.box(7.0, 1.0, 7.0, 9.0, 12.0, 9.0), Block.box(3.0, 12.0, 3.0, 13.0, 15.0, 13.0)}
   );

   public ChairBlock() {
      super(Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.METAL).strength(1.0F).noOcclusion());
      this.registerDefaultState(
         (BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false)
      );
   }

   public MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, WATERLOGGED});
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
         .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         List<SitEntity> seats = level.getEntitiesOfClass(SitEntity.class, new AABB(pos));
         if (seats.isEmpty()) {
            SitEntity seat = new SitEntity(level, pos, SIT_HEIGHT);
            seat.setYRot(((Direction)state.getValue(FACING)).toYRot());
            level.addFreshEntity(seat);
            player.startRiding(seat, true);
            return InteractionResult.CONSUME;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
      level.getEntitiesOfClass(SitEntity.class, new AABB(pos)).forEach(Entity::discard);
   }
}
