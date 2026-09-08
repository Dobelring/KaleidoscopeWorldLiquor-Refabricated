package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.entity.ChairEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ChairBlock extends HorizontalDirectionalBlock {
    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0),
            Block.box(7.0, 1.0, 7.0, 9.0, 12.0, 9.0),
            Block.box(3.0, 12.0, 3.0, 13.0, 15.0, 13.0)
    );

    public ChairBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected @NotNull com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(ChairBlock::new);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isPassenger() && !player.isCrouching() && level instanceof ServerLevel serverLevel) {
            ChairEntity seat = ChairEntity.create(serverLevel, pos);
            serverLevel.addFreshEntity(seat);
            player.startRiding(seat);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
