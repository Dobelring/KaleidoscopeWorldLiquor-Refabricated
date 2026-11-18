package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.PositionType;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BarCabinetBlock extends BaseEntityBlock {
   public static final MapCodec<BarCabinetBlock> CODEC = simpleCodec(properties -> new BarCabinetBlock());
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<PositionType> POSITION = EnumProperty.create("position", PositionType.class);
   public static final TagKey<Item> BAR_CABINET_PLACEABLE = TagKey.create(
      Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "bar_cabinet_placeable")
   );
   public static final TagKey<Item> BAR_CABINET_IRREGULAR = TagKey.create(
      Registries.ITEM, ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "bar_cabinet_irregular")
   );
   private static final VoxelShape FULL_SAFE_SHAPE = Block.box(0.001, 0.001, 0.001, 15.999, 15.999, 15.999);
   private final WoodType woodType;

   // 官方 1.2.0：WoodType 决定相邻拼接——只有同木种酒柜（同朝向）才相接成排；扩展木种各自成类
   public enum WoodType {
      OAK, BIRCH, SPRUCE, DARK_OAK, CHERRY, JUNGLE, ACACIA, MANGROVE, BAMBOO, CRIMSON, WARPED
   }

   public BarCabinetBlock() {
      this(WoodType.OAK, SoundType.WOOD);
   }

   // 按木种指定音效：放置/打碎/踩踏/击打音效都取自 SoundType
   public BarCabinetBlock(SoundType sound) {
      this(WoodType.OAK, sound);
   }

   public BarCabinetBlock(WoodType woodType) {
      this(woodType, SoundType.WOOD);
   }

   public BarCabinetBlock(WoodType woodType, SoundType sound) {
      super(Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(sound).noOcclusion().ignitedByLava());
      this.woodType = woodType;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POSITION, PositionType.SINGLE)
      );
   }

   public WoodType getWoodType() {
      return this.woodType;
   }

   // 官方 1.2.0：同木种 + 同朝向才允许拼接
   protected static boolean canConnect(BlockState self, BlockState neighbor) {
      if (neighbor.getBlock() instanceof BarCabinetBlock other) {
         return neighbor.getValue(FACING) != self.getValue(FACING) ? false : ((BarCabinetBlock)self.getBlock()).getWoodType() == other.getWoodType();
      } else {
         return false;
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return FULL_SAFE_SHAPE;
   }

   public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
      return FULL_SAFE_SHAPE;
   }

   public ItemInteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
         Direction direction = (Direction)state.getValue(FACING);

         boolean isLeftSide = switch (direction) {
            case NORTH -> hitResult.getLocation().x - pos.getX() > 0.5;
            case SOUTH -> hitResult.getLocation().x - pos.getX() < 0.5;
            case EAST -> hitResult.getLocation().z - pos.getZ() < 0.5;
            case WEST -> hitResult.getLocation().z - pos.getZ() > 0.5;
            default -> false;
         };
         if (level.getBlockEntity(pos) instanceof BarCabinetBlockEntity barCabinet && this.onClick(barCabinet, player, stack, isLeftSide)) {
            float pitch = stack.isEmpty() ? 0.8F + level.random.nextFloat() * 0.2F : 0.2F + level.random.nextFloat() * 0.2F;
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.8F + level.random.nextFloat() * 0.2F, pitch);
            return ItemInteractionResult.SUCCESS;
         } else {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
         }
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   private boolean onClick(BarCabinetBlockEntity barCabinet, Player player, ItemStack stack, boolean isLeftSide) {
      boolean irregular = false;
      boolean single = barCabinet.isSingle();
      ItemStack leftItem = barCabinet.getLeftItem();
      ItemStack rightItem = barCabinet.getRightItem();
      boolean isNativeBottle = stack.getItem() instanceof BottleBlockItem;
      boolean isNativeCocktail = stack.getItem() instanceof CocktailBlockItem;
      boolean isNativeDrink = isNativeBottle || isNativeCocktail;
      boolean isTagNormal = stack.is(BAR_CABINET_PLACEABLE) && stack.getItem() instanceof BlockItem;
      boolean isTagIrregular = stack.is(BAR_CABINET_IRREGULAR) && stack.getItem() instanceof BlockItem;
      boolean isAnyPlaceable = isNativeDrink || isTagNormal || isTagIrregular;
      if (isAnyPlaceable) {
         if (single) {
            return false;
         }

         if (isTagIrregular) {
            if (!leftItem.isEmpty() || !rightItem.isEmpty()) {
               return false;
            }

            isLeftSide = true;
            irregular = true;
         } else if (!leftItem.isEmpty() && rightItem.isEmpty() && isLeftSide) {
            isLeftSide = false;
         } else if (leftItem.isEmpty() && !rightItem.isEmpty() && !isLeftSide) {
            isLeftSide = true;
         }
      }

      if (stack.isEmpty()) {
         if (single) {
            isLeftSide = true;
            irregular = true;
         } else if (leftItem.isEmpty() && !rightItem.isEmpty() && isLeftSide) {
            isLeftSide = false;
         } else if (!leftItem.isEmpty() && rightItem.isEmpty() && !isLeftSide) {
            isLeftSide = true;
         }
      }

      if (isLeftSide) {
         if (stack.isEmpty()) {
            if (!leftItem.isEmpty()) {
               player.setItemInHand(InteractionHand.MAIN_HAND, leftItem.copy());
               barCabinet.setLeftItem(ItemStack.EMPTY);
               barCabinet.setSingle(false);
               barCabinet.refresh();
               return true;
            }

            return false;
         }

         if (isAnyPlaceable && leftItem.isEmpty()) {
            barCabinet.setLeftItem(stack.split(1));
            barCabinet.setSingle(irregular);
            barCabinet.refresh();
            return true;
         }
      } else {
         if (stack.isEmpty()) {
            if (!rightItem.isEmpty()) {
               player.setItemInHand(InteractionHand.MAIN_HAND, rightItem.copy());
               barCabinet.setRightItem(ItemStack.EMPTY);
               barCabinet.setSingle(false);
               barCabinet.refresh();
               return true;
            }

            return false;
         }

         if (isAnyPlaceable && rightItem.isEmpty()) {
            barCabinet.setRightItem(stack.split(1));
            barCabinet.setSingle(irregular);
            barCabinet.refresh();
            return true;
         }
      }

      return false;
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      Direction facing = (Direction)state.getValue(FACING);
      Direction left = facing.getClockWise();
      Direction right = facing.getCounterClockWise();
      if (direction != left && direction != right) {
         return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
      } else {
         boolean leftIsCabinet = canConnect(state, level.getBlockState(pos.relative(left)));
         boolean rightIsCabinet = canConnect(state, level.getBlockState(pos.relative(right)));
         PositionType position;
         if (leftIsCabinet && rightIsCabinet) {
            position = PositionType.MIDDLE;
         } else if (leftIsCabinet) {
            position = PositionType.RIGHT;
         } else if (rightIsCabinet) {
            position = PositionType.LEFT;
         } else {
            position = PositionType.SINGLE;
         }

         return (BlockState)state.setValue(POSITION, position);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction opposite = context.getHorizontalDirection().getOpposite();
      BlockState left = level.getBlockState(pos.relative(opposite.getClockWise()));
      BlockState right = level.getBlockState(pos.relative(opposite.getCounterClockWise()));
      BlockState self = (BlockState)this.defaultBlockState().setValue(FACING, opposite);
      PositionType position = PositionType.SINGLE;
      boolean leftIsCabinet = canConnect(self, left);
      boolean rightIsCabinet = canConnect(self, right);
      if (leftIsCabinet && rightIsCabinet) {
         position = PositionType.MIDDLE;
      } else if (leftIsCabinet) {
         position = PositionType.RIGHT;
      } else if (rightIsCabinet) {
         position = PositionType.LEFT;
      }

      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, opposite)).setValue(POSITION, position);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, POSITION});
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> stacks = super.getDrops(state, builder);
      BlockEntity blockEntity = (BlockEntity)builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (blockEntity instanceof BarCabinetBlockEntity barCabinet) {
         if (!barCabinet.getLeftItem().isEmpty()) {
            stacks.add(barCabinet.getLeftItem().copy());
         }

         if (!barCabinet.getRightItem().isEmpty()) {
            stacks.add(barCabinet.getRightItem().copy());
         }
      }

      return stacks;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new BarCabinetBlockEntity(pos, state);
   }

   // 官方 1.1.11：酒柜比较器输出——单瓶态且左槽有物=3，否则=非空槽个数（0/1/2）
   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      if (level.getBlockEntity(pos) instanceof BarCabinetBlockEntity be) {
         if (be.isSingle() && !be.getLeftItem().isEmpty()) {
            return 3;
         } else {
            int count = 0;
            if (!be.getLeftItem().isEmpty()) {
               count++;
            }

            if (!be.getRightItem().isEmpty()) {
               count++;
            }

            return count;
         }
      } else {
         return 0;
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }
}
