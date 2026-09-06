package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidStack;
import com.bmt.kaleidoscope_world_liquor.fluids.FluidUtils;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FreezerBlock extends BaseEntityBlock {
   public static final MapCodec<FreezerBlock> CODEC = simpleCodec(FreezerBlock::new);
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty OPEN = BooleanProperty.create("open");
   public static final BooleanProperty WORKING = BooleanProperty.create("working");
   private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 1.0, 16.0, 12.0, 14.0);
   private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 2.0, 16.0, 12.0, 15.0);
   private static final VoxelShape SHAPE_EAST = Block.box(2.0, 0.0, 0.0, 15.0, 12.0, 16.0);
   private static final VoxelShape SHAPE_WEST = Block.box(1.0, 0.0, 0.0, 14.0, 12.0, 16.0);

   public FreezerBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(OPEN, false))
            .setValue(WORKING, false)
      );
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      boolean open = level.hasNeighborSignal(pos) && !level.getBlockState(pos.above()).isRedstoneConductor(level, pos.above());
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection())).setValue(OPEN, open);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return switch ((Direction)state.getValue(FACING)) {
         case NORTH -> SHAPE_NORTH;
         case SOUTH -> SHAPE_SOUTH;
         case EAST -> SHAPE_EAST;
         case WEST -> SHAPE_WEST;
         default -> Shapes.block();
      };
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      super.onPlace(state, level, pos, oldState, isMoving);
      if (!level.isClientSide && (Boolean)state.getValue(OPEN) && level.getBlockEntity(pos) instanceof FreezerBlockEntity be) {
         be.setRedstonePowered(level.hasNeighborSignal(pos));
      }
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if (!level.isClientSide) {
         if (level.getBlockEntity(pos) instanceof FreezerBlockEntity be) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != be.isRedstonePowered()) {
               be.setRedstonePowered(powered);
               be.setChanged();
               boolean isOpen = (Boolean)state.getValue(OPEN);
               if (powered != isOpen) {
                  if (powered) {
                     if ((Boolean)state.getValue(WORKING)) {
                        return;
                     }

                     BlockPos abovePos = pos.above();
                     if (!level.getBlockState(abovePos).isRedstoneConductor(level, abovePos)) {
                        level.setBlock(pos, (BlockState)state.setValue(OPEN, true), 3);
                        level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                     }
                  } else {
                     if (be.tryStartCrafting()) {
                        level.setBlock(pos, (BlockState)((BlockState)state.setValue(OPEN, false)).setValue(WORKING, true), 3);
                     } else {
                        level.setBlock(pos, (BlockState)state.setValue(OPEN, false), 3);
                     }

                     level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
                  }
               }
            }
         }
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof FreezerBlockEntity be ? be.getComparatorSignal() : 0;
   }

   public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (level.isClientSide) {
         return ItemInteractionResult.SUCCESS;
      } else if (level.getBlockEntity(pos) instanceof FreezerBlockEntity be) {
         ItemStack var12 = player.getItemInHand(hand);
         if ((Boolean)state.getValue(WORKING)) {
            int remaining = Math.max(0, (be.getMaxProgress() - be.getProgress()) / 20);
            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.remaining_time", new Object[]{remaining}), true);
            return ItemInteractionResult.CONSUME;
         } else if (player.isShiftKeyDown()) {
            boolean isOpen = (Boolean)state.getValue(OPEN);
            if (isOpen) {
               boolean success = be.tryStartCrafting();
               if (success) {
                  level.setBlock(pos, (BlockState)((BlockState)state.setValue(OPEN, false)).setValue(WORKING, true), 3);
                  player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.start_crafting"), true);
               } else {
                  level.setBlock(pos, (BlockState)state.setValue(OPEN, false), 3);
               }

               level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
               BlockPos abovePos = pos.above();
               if (!level.getBlockState(abovePos).isRedstoneConductor(level, abovePos)) {
                  level.setBlock(pos, (BlockState)state.setValue(OPEN, true), 3);
                  level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
               } else {
                  player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.blocked"), true);
               }
            }

            return ItemInteractionResult.CONSUME;
         } else {
            if ((Boolean)state.getValue(OPEN)) {
               if (be.hasOutput()) {
                  be.extractOutput(player, hand);
                  level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                  return ItemInteractionResult.CONSUME;
               }

               if (!var12.isEmpty()) {
                  if (var12.is(Items.MILK_BUCKET) && be.tank.getFluidAmount() + 1000 <= be.tank.getCapacity()) {
                     be.tank.fill(new FluidStack(ModFluids.MILK_STILL, 1000), false);
                     if (!player.isCreative()) {
                        var12.shrink(1);
                        if (var12.isEmpty()) {
                           player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        } else {
                           player.addItem(new ItemStack(Items.BUCKET));
                        }
                     }

                     level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                     return ItemInteractionResult.CONSUME;
                  }

                  // 通用流体桶交互（替代 Forge FluidUtil.interactWithFluidHandler）：
                  // 任意流体桶（水/岩浆/牛奶/酒馆果汁桶等）倒入 tank；空桶从 tank 吸出对应流体桶
                  if (var12.getItem() instanceof BucketItem) {
                     Fluid bucketFluid = FluidUtils.findFluidForBucket(var12.getItem());
                     if (bucketFluid != null && bucketFluid != Fluids.EMPTY) {
                        if (be.tank.getFluidAmount() + 1000 <= be.tank.getCapacity()
                           && (be.tank.isEmpty() || be.tank.getFluid().getFluid() == bucketFluid)) {
                           be.tank.fill(new FluidStack(bucketFluid, 1000), false);
                           if (!player.isCreative()) {
                              var12.shrink(1);
                              if (var12.isEmpty()) {
                                 player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                              } else {
                                 player.addItem(new ItemStack(Items.BUCKET));
                              }
                           }

                           level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                           return ItemInteractionResult.CONSUME;
                        }
                     } else if (be.tank.getFluidAmount() >= 1000) {
                        // 吸出：空桶 + tank 有流体 → 对应桶物品
                        Fluid tankFluid = be.tank.getFluid().getFluid();
                        Item bucketForFluid = tankFluid.getBucket();
                        if (bucketForFluid != null && bucketForFluid != Items.AIR) {
                           be.tank.drain(1000, false);
                           if (!player.isCreative()) {
                              var12.shrink(1);
                              if (var12.isEmpty()) {
                                 player.setItemInHand(hand, new ItemStack(bucketForFluid));
                              } else {
                                 player.addItem(new ItemStack(bucketForFluid));
                              }
                           }

                           level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                           return ItemInteractionResult.CONSUME;
                        }
                     }
                  }

                  if (be.insertItem(var12, player, hand)) {
                     level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.1F);
                     return ItemInteractionResult.CONSUME;
                  }
               }
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
         }
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else if (level.getBlockEntity(pos) instanceof FreezerBlockEntity be) {
         if ((Boolean)state.getValue(WORKING)) {
            int remaining = Math.max(0, (be.getMaxProgress() - be.getProgress()) / 20);
            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.remaining_time", new Object[]{remaining}), true);
            return InteractionResult.CONSUME;
         } else if (player.isShiftKeyDown()) {
            boolean isOpen = (Boolean)state.getValue(OPEN);
            if (isOpen) {
               boolean success = be.tryStartCrafting();
               if (success) {
                  level.setBlock(pos, (BlockState)((BlockState)state.setValue(OPEN, false)).setValue(WORKING, true), 3);
                  player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.start_crafting"), true);
               } else {
                  level.setBlock(pos, (BlockState)state.setValue(OPEN, false), 3);
               }

               level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
               BlockPos abovePos = pos.above();
               if (!level.getBlockState(abovePos).isRedstoneConductor(level, abovePos)) {
                  level.setBlock(pos, (BlockState)state.setValue(OPEN, true), 3);
                  level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
               } else {
                  player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.blocked"), true);
               }
            }

            return InteractionResult.CONSUME;
         } else if ((Boolean)state.getValue(OPEN)) {
            if (be.hasOutput()) {
               be.extractOutput(player, InteractionHand.MAIN_HAND);
               level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
               return InteractionResult.CONSUME;
            } else {
               be.extractItem(player);
               level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.1F);
               return InteractionResult.CONSUME;
            }
         } else {
            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, OPEN, WORKING});
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof FreezerBlockEntity freezer) {
         for (int i = 0; i < 4; i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), freezer.inputInventory.getStackInSlot(i));
         }
      }

      super.onRemove(state, level, pos, newState, moved);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new FreezerBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return createTickerHelper(type, ModBlockEntities.FREEZER_BE, FreezerBlockEntity::tick);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @NotNull
   public BlockState rotate(@NotNull BlockState state, @NotNull Rotation rot) {
      return (BlockState)state.setValue(FACING, rot.rotate((Direction)state.getValue(FACING)));
   }

   @NotNull
   public BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }
}
