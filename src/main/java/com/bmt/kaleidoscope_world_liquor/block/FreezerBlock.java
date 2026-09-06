package com.bmt.kaleidoscope_world_liquor.block;

import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.fluids.MilkFluid;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 冰柜：开盖/关盖（潜行）、投料/取料、桶装流体注入/抽取（牛奶桶特判 +
 * 通用 BucketItem.getContent 反查满桶）、红石自动开合（关盖瞬间匹配配方即开工）、
 * 比较器信号。漏斗自动化见 BE 的 WorldlyContainer 实现（下方抽输出，
 * 需碗的配方除外）；流体槽经 Fabric Transfer API 暴露（见 BE）。
 */
public class FreezerBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 1.0, 16.0, 12.0, 14.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 2.0, 16.0, 12.0, 15.0);
    private static final VoxelShape SHAPE_EAST = Block.box(2.0, 0.0, 0.0, 15.0, 12.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(1.0, 0.0, 0.0, 14.0, 12.0, 16.0);

    public FreezerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(WORKING, false));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean open = level.hasNeighborSignal(pos) && !level.getBlockState(pos.above()).isRedstoneConductor(level, pos.above());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(OPEN, open);
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && state.getValue(OPEN) && level.getBlockEntity(pos) instanceof FreezerBlockEntity be) {
            be.setRedstonePowered(level.hasNeighborSignal(pos));
        }
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof FreezerBlockEntity be)) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == be.isRedstonePowered()) {
            return;
        }
        be.setRedstonePowered(powered);
        be.setChanged();
        boolean isOpen = state.getValue(OPEN);
        if (powered == isOpen) {
            return;
        }
        if (powered) {
            if (state.getValue(WORKING)) {
                return;
            }
            BlockPos abovePos = pos.above();
            if (!level.getBlockState(abovePos).isRedstoneConductor(level, abovePos)) {
                level.setBlock(pos, state.setValue(OPEN, true), 3);
                level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            if (be.tryStartCrafting()) {
                level.setBlock(pos, state.setValue(OPEN, false).setValue(WORKING, true), 3);
            } else {
                level.setBlock(pos, state.setValue(OPEN, false), 3);
            }
            level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, WORKING);
    }

    @Override
    protected @NotNull com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(FreezerBlock::new);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        return use(state, level, pos, player, InteractionHand.MAIN_HAND);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        return use(state, level, pos, player, hand);
    }

    private InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof FreezerBlockEntity be)) {
            return InteractionResult.PASS;
        }
        if (state.getValue(WORKING)) {
            int remaining = Math.max(0, (be.getMaxProgress() - be.getProgress()) / 20);
            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.remaining_time", remaining), true);
            return InteractionResult.CONSUME;
        }
        if (player.isCrouching()) {
            boolean isOpen = state.getValue(OPEN);
            if (isOpen) {
                boolean success = be.tryStartCrafting();
                if (success) {
                    level.setBlock(pos, state.setValue(OPEN, false).setValue(WORKING, true), 3);
                    player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.start_crafting"), true);
                } else {
                    level.setBlock(pos, state.setValue(OPEN, false), 3);
                }
                level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                BlockPos abovePos = pos.above();
                if (!level.getBlockState(abovePos).isRedstoneConductor(level, abovePos)) {
                    level.setBlock(pos, state.setValue(OPEN, true), 3);
                    level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.freezer.blocked"), true);
                }
            }
            return InteractionResult.CONSUME;
        }
        if (state.getValue(OPEN)) {
            ItemStack held = player.getItemInHand(hand);
            if (be.hasOutput()) {
                boolean extracted = be.extractOutput(player, hand);
                if (extracted) {
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.CONSUME;
            }
            if (!held.isEmpty()) {
                boolean tankEmpty = be.tank.isResourceBlank() || be.tank.getFluidAmountMb() == 0;
                // 牛奶桶特判（1.20.1 同款：牛奶在 1.21.11 不是注册流体，必须先于通用分支）
                if (held.is(Items.MILK_BUCKET) && tankEmpty) {
                    be.tank.fill(FluidVariant.of(MilkFluid.STILL), FluidConstants.BUCKET,
                            com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank.FluidAction.EXECUTE);
                    giveBucketBack(level, player, hand, held);
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.CONSUME;
                }
                // 通用流体桶（水/岩浆/酒馆果汁桶等）：BucketItem.getContent 反查
                if (held.getItem() instanceof BucketItem bucket) {
                    Fluid content = bucket.getContent();
                    if (content != Fluids.EMPTY && tankEmpty) {
                        be.tank.fill(FluidVariant.of(content), FluidConstants.BUCKET,
                                com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank.FluidAction.EXECUTE);
                        giveBucketBack(level, player, hand, held);
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return InteractionResult.CONSUME;
                    }
                    if (content == Fluids.EMPTY && !be.tank.isResourceBlank() && be.tank.getFluidAmountMb() >= 1000) {
                        // 空桶抽取：抽走 1000mB，得到对应满桶
                        ItemStack fullBucket = findBucketFor(be.tank.getFluidVariant());
                        if (!fullBucket.isEmpty()) {
                            be.tank.drain(FluidConstants.BUCKET,
                                    com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank.FluidAction.EXECUTE);
                            if (!player.isCreative()) {
                                held.shrink(1);
                                if (!player.getInventory().add(fullBucket)) {
                                    player.drop(fullBucket, false);
                                }
                            }
                            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                            return InteractionResult.CONSUME;
                        }
                    }
                    // tank 非空且不是空桶动作 → 桶作为物品进输入槽（不混合）
                }
                be.insertItem(held, player);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.1F);
                return InteractionResult.CONSUME;
            }
            be.extractItem(player);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.1F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** 倒空桶后回桶：手里空了直接回手上，否则进背包（背包满掉地上）；创造不消耗桶 */
    private void giveBucketBack(Level level, Player player, InteractionHand hand, ItemStack held) {
        if (player.isCreative()) {
            return;
        }
        held.shrink(1);
        ItemStack bucket = new ItemStack(Items.BUCKET);
        if (held.isEmpty()) {
            player.setItemInHand(hand, bucket);
        } else if (!player.getInventory().add(bucket)) {
            ItemEntity drop = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), bucket);
            level.addFreshEntity(drop);
        }
    }

    /** 按流体反查满桶物品：牛奶/水/岩浆特判，其余全注册表扫 BucketItem.getContent
     *  并排除生物桶（MobBucketItem extends BucketItem，getContent 返回的是水） */
    private ItemStack findBucketFor(FluidVariant variant) {
        Fluid fluid = variant.getFluid();
        if (fluid == MilkFluid.STILL) {
            return new ItemStack(Items.MILK_BUCKET);
        }
        if (fluid == Fluids.WATER) {
            return new ItemStack(Items.WATER_BUCKET);
        }
        if (fluid == Fluids.LAVA) {
            return new ItemStack(Items.LAVA_BUCKET);
        }
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            if (entry.getValue() instanceof BucketItem bucket
                    && !(entry.getValue() instanceof net.minecraft.world.item.MobBucketItem)
                    && bucket.getContent() == fluid) {
                return new ItemStack(entry.getValue());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void spawnAfterBreak(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull ItemStack tool, boolean dropExperience) {
        if (level.getBlockEntity(pos) instanceof FreezerBlockEntity be) {
            for (int i = 0; i < 4; i++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.inputInventory.getStackInSlot(i));
            }
        }
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return level.getBlockEntity(pos) instanceof FreezerBlockEntity be ? be.getComparatorSignal() : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FreezerBlockEntity(pos, state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.FREEZER_BE,
                (lvl, pos, st, be) -> FreezerBlockEntity.tick(lvl, pos, st, be));
    }
}
