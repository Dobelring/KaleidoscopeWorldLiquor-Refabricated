package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.block.entity.WallRecordBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WallRecordBlock extends Block implements EntityBlock {
   public static final DirectionProperty FACING = DirectionProperty.create("facing", Plane.HORIZONTAL);
   public static final IntegerProperty MODEL_INDEX = IntegerProperty.create("model_index", 0, 24);
   private static final VoxelShape NORTH_WALL_SHAPE = Block.box(1.0, 1.0, 0.0, 15.0, 15.0, 1.0);
   private static final VoxelShape SOUTH_WALL_SHAPE = Block.box(1.0, 1.0, 15.0, 15.0, 15.0, 16.0);
   private static final VoxelShape WEST_WALL_SHAPE = Block.box(0.0, 1.0, 1.0, 1.0, 15.0, 15.0);
   private static final VoxelShape EAST_WALL_SHAPE = Block.box(15.0, 1.0, 1.0, 16.0, 15.0, 15.0);

   public WallRecordBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(MODEL_INDEX, 0));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, MODEL_INDEX});
   }

   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level instanceof ServerLevel serverLevel) {
         getDrops(state, serverLevel, pos, level.getBlockEntity(pos)).forEach(stack -> {
            if (!player.addItem(stack)) {
               player.drop(stack, false);
            }
         });
         level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
         level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, player.getSoundSource(), 1.0F, 1.0F);
      }

      return InteractionResult.SUCCESS;
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
      List<ItemStack> drops = super.getDrops(state, params);
      BlockEntity blockEntity = (BlockEntity)params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (blockEntity instanceof WallRecordBlockEntity be && !be.getRecord().isEmpty()) {
         drops.add(be.getRecord().copyWithCount(1));
      }

      return drops;
   }

   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return level.getBlockEntity(pos) instanceof WallRecordBlockEntity be && !be.getRecord().isEmpty()
         ? be.getRecord().copyWithCount(1)
         : super.getCloneItemStack(level, pos, state);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction clickedFace = context.getClickedFace();
      if (!clickedFace.getAxis().isHorizontal()) {
         return null;
      } else {
         Direction direction = clickedFace.getOpposite();
         return (BlockState)this.defaultBlockState().setValue(FACING, direction);
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      Direction facing = (Direction)state.getValue(FACING);
      BlockPos supportPos = pos.relative(facing);
      return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing.getOpposite());
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      Direction facing = (Direction)state.getValue(FACING);
      return direction == facing && !state.canSurvive(level, currentPos)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape(state, direction, facingState, level, currentPos, facingPos);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.getCollisionShape(state, level, pos, context);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return switch ((Direction)state.getValue(FACING)) {
         case NORTH -> NORTH_WALL_SHAPE;
         case SOUTH -> SOUTH_WALL_SHAPE;
         case WEST -> WEST_WALL_SHAPE;
         case EAST -> EAST_WALL_SHAPE;
         default -> super.getCollisionShape(state, level, pos, context);
      };
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new WallRecordBlockEntity(pos, state);
   }
}
