package com.bmt.kaleidoscope_world_liquor.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 玩偶方块（1.21.11 无 kaleidoscope_doll 模组，liquor 自实现所需类）：
 * 水平朝向 + 水淹；右键播放随机音符音效与音符粒子（doll 模组同款交互）。
 */
public class DollBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    // 原版 kaleidoscope_doll 同款：像素单位 2~14、高 12（此前误写为米制 0.25~0.75，碰撞箱近乎为零）
    private static final VoxelShape DOLL_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    private static final double PARTICLE_OFFSET_RANGE = 0.2;
    private static final double PARTICLE_HEIGHT_OFFSET = 0.5;
    private static final double PARTICLE_HEIGHT_VARIANCE = 0.25;
    private static final float NOTE_COLOR_DIVISOR = 24.0F;
    private static final int MAX_NOTE_COLORS = 24;
    private static final float BASE_VOLUME = 0.75F;
    private static final float PITCH_VARIANCE = 1.2F;

    public DollBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                // 默认朝向必须 SOUTH（doll mod 原版同款）：酒柜 BER 用 defaultBlockState
                // 渲染，single 路径旋转角=-facing*90，默认 SOUTH 时玩偶面朝柜外；
                // 默认 NORTH 会让酒柜内的玩偶背对玩家。地面放置由 getStateForPlacement
                // 覆写，不受影响。
                .setValue(FACING, Direction.SOUTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(DollBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluidState.is(Fluids.WATER));
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return DOLL_SHAPE;
    }

    @Override
    public @NotNull FluidState getFluidState(@NotNull BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.LevelReader level,
                                              @NotNull net.minecraft.world.level.ScheduledTickAccess tickAccess, @NotNull BlockPos pos,
                                              @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState,
                                              @NotNull RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            this.spawnNoteParticles(serverLevel, pos);
            this.playDollSound(serverLevel, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private void spawnNoteParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * PARTICLE_OFFSET_RANGE;
        double y = pos.getY() + PARTICLE_HEIGHT_OFFSET + level.random.nextDouble() * PARTICLE_HEIGHT_VARIANCE;
        double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * PARTICLE_OFFSET_RANGE;
        int color = Mth.nextInt(level.random, 0, MAX_NOTE_COLORS - 1);
        level.sendParticles(ParticleTypes.NOTE, x, y, z, 1, 0.0, 0.0, 0.0, color / NOTE_COLOR_DIVISOR);
    }

    private void playDollSound(ServerLevel level, BlockPos pos) {
        // 1.20.1 玩偶模组原版公式：volume 1.0，pitch 0.75 + rand*0.5，音效 duck_toy
        float pitch = BASE_VOLUME + level.random.nextFloat() * 0.5F;
        level.playSound(null, pos, com.bmt.kaleidoscope_world_liquor.init.ModSounds.DUCK_TOY, SoundSource.BLOCKS, 1.0F, pitch);
    }
}
