package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.PositionType;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.block.mixology.CocktailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 酒柜：左右双槽展示 + 异形酒瓶占双槽（bar_cabinet_irregular tag）。
 * 邻接同款酒柜时自动算 POSITION（复用 tavern 的 PositionType 属性与更新逻辑）。
 */
public class BarCabinetBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<PositionType> POSITION =
            EnumProperty.create("position", PositionType.class);

    // 1.20.1 同款：0.001 内缩，防止被引擎判为完整立方体（光照/面剔除按不透明白块处理会让联排内侧发黑）
    public static final VoxelShape FULL_SAFE_SHAPE = Block.box(0.001, 0.001, 0.001, 15.999, 15.999, 15.999);

    public BarCabinetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POSITION, PositionType.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POSITION);
    }

    @Override
    protected @NotNull com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BarCabinetBlock::new);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return FULL_SAFE_SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return FULL_SAFE_SHAPE;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        InteractionResult result = use(state, level, pos, player, InteractionHand.MAIN_HAND, hitResult);
        return result == InteractionResult.PASS ? super.useWithoutItem(state, level, pos, player, hitResult) : result;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        InteractionResult result = use(state, level, pos, player, hand, hitResult);
        return result == InteractionResult.PASS ? super.useItemOn(stack, state, level, pos, player, hand, hitResult) : result;
    }

    private InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Direction direction = state.getValue(FACING);
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            // 仅拦本模组自定义喝声音效的饮品，防止其 use 流程启动；
            // 其余物品（含放置酒柜方块）照常 PASS 走原版预测与放置音效
            return com.bmt.kaleidoscope_world_liquor.event.DrinkingSounds.hasCustomDrinkSound(stack)
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        boolean isLeftSide = switch (direction) {
            case NORTH -> hitResult.getLocation().x - pos.getX() > 0.5;
            case SOUTH -> hitResult.getLocation().x - pos.getX() < 0.5;
            case EAST -> hitResult.getLocation().z - pos.getZ() < 0.5;
            case WEST -> hitResult.getLocation().z - pos.getZ() > 0.5;
            default -> false;
        };
        if (level.getBlockEntity(pos) instanceof BarCabinetBlockEntity barCabinet
                && this.onClick(barCabinet, player, stack, isLeftSide)) {
            float pitch = stack.isEmpty() ? 0.8F + level.random.nextFloat() * 0.2F : 0.2F + level.random.nextFloat() * 0.2F;
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GLASS_PLACE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8F + level.random.nextFloat() * 0.2F, pitch);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean onClick(BarCabinetBlockEntity barCabinet, Player player, ItemStack stack, boolean isLeftSide) {
        boolean irregular;
        boolean single = barCabinet.isSingle();
        ItemStack leftItem = barCabinet.getLeftItem();
        ItemStack rightItem = barCabinet.getRightItem();
        boolean isNativeBottle = stack.getItem() instanceof BottleBlockItem;
        boolean isNativeCocktail = stack.getItem() instanceof CocktailBlockItem;
        boolean isNativeDrink = isNativeBottle || isNativeCocktail;
        boolean isTagNormal = stack.is(ModTags.BAR_CABINET_PLACEABLE) && stack.getItem() instanceof BlockItem;
        boolean isTagIrregular = stack.is(ModTags.BAR_CABINET_IRREGULAR) && stack.getItem() instanceof BlockItem;
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
            } else {
                irregular = false;
            }
        } else {
            irregular = false;
        }

        if (stack.isEmpty()) {
            if (single) {
                isLeftSide = true;
            } else if (leftItem.isEmpty() && !rightItem.isEmpty() && isLeftSide) {
                isLeftSide = false;
            } else if (!leftItem.isEmpty() && rightItem.isEmpty() && !isLeftSide) {
                isLeftSide = true;
            }
        } else if (isAnyPlaceable && !isTagIrregular) {
            if (!leftItem.isEmpty() && rightItem.isEmpty() && isLeftSide) {
                isLeftSide = false;
            } else if (leftItem.isEmpty() && !rightItem.isEmpty() && !isLeftSide) {
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
            } else if (isTagIrregular && leftItem.isEmpty() && rightItem.isEmpty()) {
                // 1.21.1 原版：异形酒只占左槽数据位，BER 由 single 状态渲染居中；right 必须保持空
                barCabinet.setLeftItem(stack.copyWithCount(1));
                barCabinet.setSingle(true);
                barCabinet.refresh();
                stack.shrink(1);
                return true;
            } else if (isAnyPlaceable && leftItem.isEmpty()) {
                barCabinet.setLeftItem(stack.copyWithCount(1));
                barCabinet.setSingle(irregular);
                barCabinet.refresh();
                stack.shrink(1);
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
            } else if (isAnyPlaceable && rightItem.isEmpty()) {
                barCabinet.setRightItem(stack.copyWithCount(1));
                barCabinet.setSingle(irregular);
                barCabinet.refresh();
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BarCabinetBlockEntity(pos, state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    // 1.21.1 同款：挖掉酒柜时附带掉落柜内左右槽的酒
    @Override
    protected @NotNull java.util.List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>(super.getDrops(state, builder));
        net.minecraft.world.level.block.entity.BlockEntity be =
                builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (be instanceof BarCabinetBlockEntity barCabinet) {
            if (!barCabinet.getLeftItem().isEmpty()) {
                stacks.add(barCabinet.getLeftItem().copy());
            }
            if (!barCabinet.getRightItem().isEmpty()) {
                stacks.add(barCabinet.getRightItem().copy());
            }
        }
        return stacks;
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(@NotNull BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.LevelReader level, @NotNull net.minecraft.world.level.ScheduledTickAccess tickAccess, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull net.minecraft.util.RandomSource random) {
        Direction self = state.getValue(FACING);
        // 1.20.1 原版参考系：left = 顺时针、right = 逆时针（写反会导致联排左右模型互换）
        Direction left = self.getClockWise();
        Direction right = self.getCounterClockWise();
        if (direction == left) {
            boolean leftIsCabinet = neighborState.is(this) && neighborState.getValue(FACING) == self;
            PositionType position = state.getValue(POSITION);
            if (leftIsCabinet) {
                if (position == PositionType.SINGLE) {
                    return state.setValue(POSITION, PositionType.RIGHT);
                }
                if (position == PositionType.LEFT) {
                    return state.setValue(POSITION, PositionType.MIDDLE);
                }
            } else {
                if (position == PositionType.RIGHT) {
                    return state.setValue(POSITION, PositionType.SINGLE);
                }
                if (position == PositionType.MIDDLE) {
                    return state.setValue(POSITION, PositionType.LEFT);
                }
            }
        } else if (direction == right) {
            boolean rightIsCabinet = neighborState.is(this) && neighborState.getValue(FACING) == self;
            PositionType position = state.getValue(POSITION);
            if (rightIsCabinet) {
                if (position == PositionType.SINGLE) {
                    return state.setValue(POSITION, PositionType.LEFT);
                }
                if (position == PositionType.RIGHT) {
                    return state.setValue(POSITION, PositionType.MIDDLE);
                }
            } else {
                if (position == PositionType.LEFT) {
                    return state.setValue(POSITION, PositionType.SINGLE);
                }
                if (position == PositionType.MIDDLE) {
                    return state.setValue(POSITION, PositionType.RIGHT);
                }
            }
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }
}
