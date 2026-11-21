package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

/**
 * 官方 1.2.0：装置方块数据读写工具（ContraptionBlocks 移植）。
 * 官方版走 kaleidoscope_contraption 模组的 ContraptionInteractionUtil；
 * 26.x 无该模组，改用本模组自建的 {@link ContraptionDataSync}（updateContraptionData /
 * updateContraptionDataLocally / playSound，1.2.0 同步轮已落地）。
 */
public final class ContraptionBlocks {
    private ContraptionBlocks() {
    }

    @Nullable
    public static StructureBlockInfo getInfo(AbstractContraptionEntity entity, BlockPos localPos) {
        return entity.getContraption().getBlocks().get(localPos);
    }

    public static CompoundTag save(BlockEntity be, Level level) {
        CompoundTag tag = be.saveWithFullMetadata(level.registryAccess());
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        return tag;
    }

    public static void update(AbstractContraptionEntity entity, BlockPos localPos, BlockState state, @Nullable CompoundTag nbt) {
        ContraptionDataSync.updateContraptionData(entity, localPos, new StructureBlockInfo(localPos, state, nbt));
    }

    public static void updateNbt(AbstractContraptionEntity entity, BlockPos localPos, CompoundTag nbt) {
        StructureBlockInfo info = getInfo(entity, localPos);
        if (info != null) {
            ContraptionDataSync.updateContraptionDataLocally(entity, localPos, new StructureBlockInfo(localPos, info.state(), nbt));
        }
    }

    public static void playSound(AbstractContraptionEntity entity, BlockPos localPos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        ContraptionDataSync.playSound(entity, localPos, sound, source, volume, pitch);
    }
}
