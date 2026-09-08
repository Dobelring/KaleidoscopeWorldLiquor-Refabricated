package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.blockentity.WallRecordBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 墙上唱片：22 个随机模型变体（model_index 由第 9 步 MusicDiscEvents 放置时写入）。
 * 空手右键取回唱片；掉落表由 LootParams 里的 BE 附加（第 9 步 mixin 掉落表或直接
 * 由 here 的 getDrops 附加——1.20.1 原版即覆写 getDrops）。
 */
public class WallRecordBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty MODEL_INDEX = IntegerProperty.create("model_index", 0, 21);

    private static final VoxelShape NORTH_WALL_SHAPE = Block.box(1.0, 1.0, 0.0, 15.0, 15.0, 1.0);
    private static final VoxelShape SOUTH_WALL_SHAPE = Block.box(1.0, 1.0, 15.0, 15.0, 15.0, 16.0);
    private static final VoxelShape WEST_WALL_SHAPE = Block.box(0.0, 1.0, 1.0, 1.0, 15.0, 15.0);
    private static final VoxelShape EAST_WALL_SHAPE = Block.box(15.0, 1.0, 1.0, 16.0, 15.0, 15.0);

    public WallRecordBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(MODEL_INDEX, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODEL_INDEX);
    }

    @Override
    protected @NotNull com.mojang.serialization.MapCodec<? extends Block> codec() {
        return simpleCodec(WallRecordBlock::new);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (!clickedFace.getAxis().isHorizontal()) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing.getOpposite());
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.LevelReader level, @NotNull net.minecraft.world.level.ScheduledTickAccess tickAccess, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull net.minecraft.util.RandomSource random) {
        Direction facing = state.getValue(FACING);
        return direction == facing && !state.canSurvive(level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_WALL_SHAPE;
            case SOUTH -> SOUTH_WALL_SHAPE;
            case WEST -> WEST_WALL_SHAPE;
            case EAST -> EAST_WALL_SHAPE;
            default -> super.getShape(state, level, pos, context);
        };
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY) instanceof WallRecordBlockEntity be
                && !be.getRecord().isEmpty()) {
            drops.add(be.getRecord().split(1));
        }
        return drops;
    }

    @Override
    protected @NotNull net.minecraft.world.InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (level.getBlockEntity(pos) instanceof WallRecordBlockEntity be && !be.getRecord().isEmpty()) {
                net.minecraft.world.item.ItemStack record = be.getRecord().split(1);
                if (!player.getInventory().add(record)) {
                    player.drop(record, false);
                }
            }
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    player.getSoundSource(), 1.0F, 1.0F);
            return net.minecraft.world.InteractionResult.SUCCESS_SERVER;
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WallRecordBlockEntity(pos, state);
    }
}
