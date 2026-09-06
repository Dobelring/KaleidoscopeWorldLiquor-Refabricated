package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.block.AbstractStorageBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.PositionType;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BarCellarCabinetBlock extends AbstractStorageBlock {
   private static final MapCodec<BarCellarCabinetBlock> CODEC = simpleCodec(p -> new BarCellarCabinetBlock());
   public static final TagKey<Item> BAR_CELLAR_CABINET_NATIVE_BLACKLIST = TagKey.create(
      Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "cellar_cabinet_blocklist")
   );
   public static final TagKey<Item> BAR_CELLAR_CABINET_PLACEABLE = TagKey.create(
      Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "bar_cellar_cabinet_placeable")
   );
   private static final VoxelShape FULL_SAFE_SHAPE = Block.box(0.001, 0.001, 0.001, 15.999, 15.999, 15.999);

   public BarCellarCabinetBlock() {
      super(Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POWERED, false))
            .setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.SINGLE)
      );
   }

   protected ItemInteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return super.handleUse(state, level, pos, player, hand, hitResult);
   }

   protected ItemInteractionResult putOn(Level level, BlockPos pos, Player player, StorageBlockEntity storage, int clickedSlot) {
      ItemStack handItem = player.getMainHandItem();
      if (this.blockListCheck(handItem)) {
         this.sendMessage(player, Component.translatable("message.kaleidoscope_tavern.rack.irregular"));
         return ItemInteractionResult.FAIL;
      } else if (storage.getItems().getStackInSlot(clickedSlot).isEmpty()) {
         storage.getItems().setStackInSlot(clickedSlot, handItem.split(1));
         storage.refresh();
         level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS);
         return ItemInteractionResult.SUCCESS;
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   protected boolean blockListCheck(ItemStack stack) {
      boolean isNativeBottle = stack.getItem() instanceof BottleBlockItem;
      return isNativeBottle ? stack.is(BAR_CELLAR_CABINET_NATIVE_BLACKLIST) : !stack.is(BAR_CELLAR_CABINET_PLACEABLE);
   }

   protected int getClickedSlot(Direction direction, BlockPos pos, BlockHitResult hitResult) {
      if (hitResult.getDirection() != direction) {
         return -1;
      } else {
         double localX = this.getLocalX(direction, pos, hitResult);
         double relativeY = hitResult.getLocation().y - pos.getY();
         int column = (int)(localX * 3.0) % 3;
         int row = 2 - (int)(relativeY * 3.0) % 3;
         return column + row * 3;
      }
   }

   protected Vec3 getShootPos(Direction direction, BlockPos pos, int slot) {
      Vec3 center = Vec3.atLowerCornerOf(pos).add(0.5, 0.5, 0.5);
      Vec3 scale = Vec3.atLowerCornerOf(direction.getNormal()).scale(0.5);
      return center.add(scale);
   }

   protected Vec3 getMovement(Direction direction, BlockPos pos, int slot) {
      double factor = Math.random() * 2.0 + 0.5;
      Vec3 normal = Vec3.atLowerCornerWithOffset(direction.getNormal(), 0.0, 0.1, 0.0);
      return normal.scale(factor);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      Direction self = (Direction)state.getValue(FACING);
      Direction left = self.getClockWise();
      Direction right = self.getCounterClockWise();
      if (direction == left) {
         boolean leftIsCabinet = neighborState.is(this) && neighborState.getValue(FACING) == self;
         PositionType position = (PositionType)state.getValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION);
         if (leftIsCabinet) {
            if (position == PositionType.SINGLE) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.RIGHT);
            }

            if (position == PositionType.LEFT) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.MIDDLE);
            }
         } else {
            if (position == PositionType.RIGHT) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.SINGLE);
            }

            if (position == PositionType.MIDDLE) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.LEFT);
            }
         }
      } else if (direction == right) {
         boolean rightIsCabinet = neighborState.is(this) && neighborState.getValue(FACING) == self;
         PositionType position = (PositionType)state.getValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION);
         if (rightIsCabinet) {
            if (position == PositionType.SINGLE) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.LEFT);
            }

            if (position == PositionType.RIGHT) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.MIDDLE);
            }
         } else {
            if (position == PositionType.LEFT) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.SINGLE);
            }

            if (position == PositionType.MIDDLE) {
               return (BlockState)state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.RIGHT);
            }
         }
      }

      return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction opposite = context.getHorizontalDirection().getOpposite();
      BlockState left = level.getBlockState(pos.relative(opposite.getClockWise()));
      BlockState right = level.getBlockState(pos.relative(opposite.getCounterClockWise()));
      PositionType position = PositionType.SINGLE;
      boolean leftIsCabinet = left.is(this) && left.getValue(FACING) == opposite;
      boolean rightIsCabinet = right.is(this) && right.getValue(FACING) == opposite;
      if (leftIsCabinet && rightIsCabinet) {
         position = PositionType.MIDDLE;
      } else if (leftIsCabinet) {
         position = PositionType.RIGHT;
      } else if (rightIsCabinet) {
         position = PositionType.LEFT;
      }

      boolean signal = level.hasNeighborSignal(pos);
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, opposite)).setValue(POWERED, signal))
         .setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, position);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new BarCellarCabinetBlockEntity(pos, state);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, POWERED, com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION});
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return 0.2F;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return FULL_SAFE_SHAPE;
   }

   public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
      return FULL_SAFE_SHAPE;
   }
}
