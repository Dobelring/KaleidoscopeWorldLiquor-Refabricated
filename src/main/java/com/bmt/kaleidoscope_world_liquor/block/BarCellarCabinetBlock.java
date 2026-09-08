package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.block.AbstractStorageBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.properties.PositionType;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 酒窖柜：3x3 展示（复用 tavern AbstractStorageBlock 的点击取放/红石弹射框架，
 * BE 是 tavern StorageBlockEntity 的 9 槽子类）。放置过滤走 bar_cellar_cabinet_placeable
 * tag，原生酒走原生黑名单（1.20.1 中为空 tag）。POSITION 为三格联排位次（左/中/右/单）。
 */
public class BarCellarCabinetBlock extends AbstractStorageBlock {
    public BarCellarCabinetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState left = context.getLevel().getBlockState(context.getClickedPos().relative(facing.getClockWise()));
        BlockState right = context.getLevel().getBlockState(context.getClickedPos().relative(facing.getCounterClockWise()));
        boolean leftIsCabinet = left.is(this) && left.getValue(FACING) == facing;
        boolean rightIsCabinet = right.is(this) && right.getValue(FACING) == facing;
        PositionType position = PositionType.SINGLE;
        if (leftIsCabinet && rightIsCabinet) {
            position = PositionType.MIDDLE;
        } else if (leftIsCabinet) {
            position = PositionType.RIGHT;
        } else if (rightIsCabinet) {
            position = PositionType.LEFT;
        }
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, position);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.LevelReader level, @NotNull net.minecraft.world.level.ScheduledTickAccess tickAccess, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull net.minecraft.util.RandomSource random) {
        Direction self = state.getValue(FACING);
        // 1.20.1 原版参考系：left = 顺时针、right = 逆时针（写反会导致联排左右模型互换）
        Direction left = self.getClockWise();
        Direction right = self.getCounterClockWise();
        if (direction == left) {
            boolean leftIsCabinet = neighborState.is(this) && neighborState.getValue(FACING) == self;
            PositionType position = state.getValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION);
            if (leftIsCabinet) {
                if (position == PositionType.SINGLE) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.RIGHT);
                }
                if (position == PositionType.LEFT) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.MIDDLE);
                }
            } else {
                if (position == PositionType.RIGHT) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.SINGLE);
                }
                if (position == PositionType.MIDDLE) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.LEFT);
                }
            }
        } else if (direction == right) {
            boolean rightIsCabinet = neighborState.is(this) && neighborState.getValue(FACING) == self;
            PositionType position = state.getValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION);
            if (rightIsCabinet) {
                if (position == PositionType.SINGLE) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.LEFT);
                }
                if (position == PositionType.RIGHT) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.MIDDLE);
                }
            } else {
                if (position == PositionType.LEFT) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.SINGLE);
                }
                if (position == PositionType.MIDDLE) {
                    return state.setValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock.POSITION, PositionType.RIGHT);
                }
            }
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(@NotNull net.minecraft.core.BlockPos pos, @NotNull BlockState state) {
        return new com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity(pos, state);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.HorizontalDirectionalBlock> codec() {
        return simpleCodec(BarCellarCabinetBlock::new);
    }

    // 1.20.1 同款：0.001 内缩防被判为完整立方体（整块判定会让联排内侧取到 0 光照发黑）
    public static final net.minecraft.world.phys.shapes.VoxelShape FULL_SAFE_SHAPE =
            net.minecraft.world.level.block.Block.box(0.001, 0.001, 0.001, 15.999, 15.999, 15.999);

    @Override
    protected @NotNull net.minecraft.world.phys.shapes.VoxelShape getShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.BlockGetter level, @NotNull BlockPos pos, @NotNull net.minecraft.world.phys.shapes.CollisionContext context) {
        return FULL_SAFE_SHAPE;
    }

    @Override
    protected int getClickedSlot(Direction direction, BlockPos pos, BlockHitResult hitResult) {
        if (hitResult.getDirection() != direction) {
            return -1;
        }
        double localX = this.getLocalX(direction, pos, hitResult);
        double relativeY = hitResult.getLocation().y - pos.getY();
        int column = (int) (localX * 3.0) % 3;
        int row = 2 - (int) (relativeY * 3.0) % 3;
        return column + row * 3;
    }

    @Override
    protected Vec3 getShootPos(Direction direction, BlockPos pos, int slot) {
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 scale = Vec3.atLowerCornerOf(direction.getUnitVec3i()).scale(0.5);
        return center.add(scale);
    }

    @Override
    protected Vec3 getMovement(Direction direction, BlockPos pos, int slot) {
        double factor = Math.random() * 2.0 + 0.5;
        Vec3 normal = new Vec3(direction.getStepX(), 0.1, direction.getStepZ()).normalize();
        return normal.scale(factor);
    }

    @Override
    protected boolean blockListCheck(ItemStack stack) {
        boolean isNativeBottle = stack.getItem() instanceof BottleBlockItem;
        return isNativeBottle ? stack.is(ModTags.BAR_CELLAR_CABINET_NATIVE_BLACKLIST)
                : !stack.is(ModTags.BAR_CELLAR_CABINET_PLACEABLE);
    }

    @Override
    protected InteractionResult putOn(Level level, BlockPos pos, Player player, StorageBlockEntity storage, int clickedSlot) {
        ItemStack handItem = player.getMainHandItem();
        var items = storage.getItems();
        if (this.blockListCheck(handItem)) {
            this.sendMessage(player, Component.translatable("message.kaleidoscope_tavern.rack.irregular"));
            return InteractionResult.FAIL;
        }
        if (items.getStackInSlot(clickedSlot).isEmpty()) {
            items.setStackInSlot(clickedSlot, handItem.split(1));
            storage.refresh();
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.STONE_PLACE, net.minecraft.sounds.SoundSource.BLOCKS);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            // 客户端消耗动作，防止手持饮品右键时物品进入喝的状态（基类 handleUse 只在服务端干活）
            return InteractionResult.SUCCESS;
        }
        return this.handleUse(state, level, pos, player, InteractionHand.MAIN_HAND, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            // 仅拦本模组自定义音效饮品；酒柜方块等照常 PASS 保放置预测与音效
            return com.bmt.kaleidoscope_world_liquor.event.DrinkingSounds.hasCustomDrinkSound(stack)
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return this.handleUse(state, level, pos, player, hand, hitResult);
    }
}
