package com.bmt.kaleidoscope_world_liquor.block;

import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.SitUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * 吧台凳：水平朝向 + 水淹，坐姿交给 tavern 的 SitEntity（1.1.9 起官方同款，不再自建实体）。
 *
 * <p>落座由本类的 {@code useWithoutItem} 一手包办（官方 1.1.9 的 ChairBlock 也不实现
 * tavern 的 {@code ISittable}）：实现它会让 tavern 的全局 PlayerSitEvent 抢走"空手点顶面"这条路径，
 * 而那条路径登记的"下凳落点"是玩家入座时的站位 → 下凳会被送回原处，而不是凳子旁边。
 */
public class ChairBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;    // 座面顶=15/16（bar/stool/base.json 顶面 y=15）。1.20.1 原值 0.65 会让腿部插进凳体，
    // 调到座面顶脚底贴面（1.21.11/26.x 同步修改；官方 1.1.9 的 0.9 不适用本工程）。
    private static final float SIT_HEIGHT = 0.9375F;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0),
            Block.box(7.0, 1.0, 7.0, 9.0, 12.0, 9.0),
            Block.box(3.0, 12.0, 3.0, 13.0, 15.0, 13.0)
    );

    /**
     * 必须接收注册时注入的 Properties：26.x 的方块 id 随 Properties 传入
     * （{@code ModBlocks.register} 里 {@code properties.setId(key)}），
     * 自己另造 {@code Properties.of()} 会丢 id → 构造 BlockBehaviour 时 "Block id not set" NPE。
     * 具体属性（黑 + 金属音效 + 1.0F 硬度 + noOcclusion）由 {@code ModBlocks.stoolReg} 提供。
     */
    public ChairBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(ChairBlock::new);
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
        return SHAPE;
    }

    @Override
    public @NotNull FluidState getFluidState(@NotNull BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level,
                                              @NotNull ScheduledTickAccess tickAccess, @NotNull BlockPos pos,
                                              @NotNull Direction direction, @NotNull BlockPos neighborPos,
                                              @NotNull BlockState neighborState, @NotNull RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!level.getEntitiesOfClass(SitEntity.class, new AABB(pos)).isEmpty()) {
            return InteractionResult.PASS;
        }

        SitEntity seat = new SitEntity(level);
        seat.absSnapTo(pos.getX() + 0.5, pos.getY() + SIT_HEIGHT, pos.getZ() + 0.5);
        seat.setYRot(state.getValue(FACING).toYRot());

        // 登记"下凳落点"：tavern 的 SitEntity 下凳时会优先返回这个位置，
        // 传玩家入座时的站位会把人送回原处（旧 ChairEntity 是放到凳子旁一格的安全点）。
        // 四侧都被堵住时退回座位中心，仍然登记——tavern 靠这张表在方块被破坏时清理座位。
        Vec3 dismountPos = findDismountPos(level, pos, player);
        SitUtil.addSitEntity(level, pos, seat, dismountPos != null ? dismountPos : seat.position());

        level.addFreshEntity(seat);
        player.startRiding(seat, false, false);
        return InteractionResult.CONSUME;
    }

    /** 凳子四侧第一个无碰撞的落点（沿用旧 ChairEntity 的取法）；全被堵住时返回 null，交给 vanilla 兜底。 */
    private static Vec3 findDismountPos(Level level, BlockPos pos, Player player) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Vec3 candidate = new Vec3(
                    pos.getX() + 0.5 + direction.getStepX(),
                    pos.getY() + SIT_HEIGHT,
                    pos.getZ() + 0.5 + direction.getStepZ());
            AABB passengerBox = player.getBoundingBox().move(candidate.subtract(player.position()));
            if (level.noCollision(player, passengerBox)) {
                return candidate.add(0.0, 0.1, 0.0);
            }
        }
        return null;
    }
}
