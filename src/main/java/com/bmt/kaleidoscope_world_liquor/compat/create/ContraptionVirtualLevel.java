package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.ContraptionWorld;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

/**
 * 官方 1.2.0：装置上的虚拟 Level（ContraptionVirtualLevel 移植）——
 * 装置内方块实体的 setBlock/邻块更新/声音全部落到装置数据（经 ContraptionBlocks 同步），
 * 邻块传播按官方置空（装置方块不驱动现实世界邻块）。
 */
public class ContraptionVirtualLevel extends ContraptionWorld {
    private final AbstractContraptionEntity entity;
    private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

    public ContraptionVirtualLevel(AbstractContraptionEntity entity) {
        super(entity.level(), entity.getContraption());
        this.entity = entity;
    }

    public AbstractContraptionEntity getEntity() {
        return this.entity;
    }

    public void putBlockEntity(BlockPos pos, BlockEntity be) {
        this.blockEntities.put(pos, be);
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        this.blockEntities.remove(pos);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.blockEntities.get(pos);
    }

    @Override
    public RecipeAccess recipeAccess() {
        return this.entity.level().recipeAccess();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags) {
        return this.setBlock(pos, state, flags, 512);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        StructureBlockInfo info = ContraptionBlocks.getInfo(this.entity, pos);
        CompoundTag nbt = info == null ? null : info.nbt();
        BlockEntity be = this.blockEntities.get(pos);
        if (be != null) {
            nbt = ContraptionBlocks.save(be, this);
            be.setBlockState(state);
        }

        ContraptionBlocks.update(this.entity, pos, state, nbt);
        return true;
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean isMoving, @Nullable Entity source, int recursionLeft) {
        ContraptionBlocks.update(this.entity, pos, Blocks.AIR.defaultBlockState(), null);
        this.blockEntities.remove(pos);
        return true;
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        StructureBlockInfo info = ContraptionBlocks.getInfo(this.entity, pos);
        BlockEntity be = this.blockEntities.get(pos);
        if (info != null && be != null) {
            ContraptionBlocks.update(this.entity, pos, info.state(), ContraptionBlocks.save(be, this));
        }
    }

    @Override
    public void updateNeighborsAt(BlockPos pos, Block block) {
    }

    @Override
    public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {
    }

    @Override
    public void playSound(@Nullable Entity player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        ContraptionBlocks.playSound(this.entity, pos, sound, source, volume, pitch);
    }
}
