package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.network.ContraptionChangedPacket;
import com.bmt.kaleidoscope_world_liquor.compat.create.network.ContraptionNetwork;
import com.bmt.kaleidoscope_world_liquor.mixin.create.accessor.ContraptionAccessor;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Map;

/**
 * 装置上方块数据的读写与同步（官方同名类的移植）。
 * <p>
 * 与官方的两处差别：
 * <ol>
 *   <li>26.x 的方块实体序列化改走 {@code ValueInput}/{@code ValueOutput}，
 *       所以读用 {@code TagValueInput.create(...) + loadWithComponents}、
 *       写用 {@code saveWithFullMetadata(registries)}（官方 1.21.1 是
 *       {@code loadWithComponents(CompoundTag, ...)} / 直接拿 CompoundTag）。</li>
 *   <li>官方用 {@code PacketHandler.sendToTracking}（NeoForge 的 PacketDistributor），
 *       这里换成自建的 Fabric 网络层 {@link ContraptionNetwork}。</li>
 * </ol>
 */
public final class ContraptionDataSync {
    private ContraptionDataSync() {
    }

    static <T extends BlockEntity> T loadBlockEntity(T blockEntity, CompoundTag tag, AbstractContraptionEntity contraptionEntity) {
        blockEntity.setLevel(contraptionEntity.level());
        CompoundTag data = tag == null ? new CompoundTag() : tag.copy();
        blockEntity.loadWithComponents(TagValueInput.create(
                ProblemReporter.DISCARDING, contraptionEntity.level().registryAccess(), data));
        return blockEntity;
    }

    static void saveBlockEntity(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureBlockInfo info, BlockEntity blockEntity) {
        HolderLookup.Provider registries = contraptionEntity.level().registryAccess();
        CompoundTag tag = blockEntity.saveWithFullMetadata(registries);
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        updateContraptionData(contraptionEntity, localPos, new StructureBlockInfo(info.pos(), info.state(), tag));
    }

    static void playSound(AbstractContraptionEntity contraptionEntity, BlockPos localPos, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        Vec3 globalPos = contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0F);
        BlockPos soundPos = BlockPos.containing(globalPos);
        contraptionEntity.level().playSound(null, soundPos, soundEvent, source, volume, pitch);
    }

    static void updateContraptionData(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureBlockInfo newInfo) {
        updateContraptionDataLocally(contraptionEntity, localPos, newInfo);
        if (!contraptionEntity.level().isClientSide()) {
            ContraptionNetwork.sendToTracking(
                    new ContraptionChangedPacket(contraptionEntity.getId(), localPos, newInfo.state(), newInfo.nbt()), contraptionEntity);
        }
    }

    public static void updateContraptionDataLocally(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureBlockInfo newInfo) {
        Contraption contraption = contraptionEntity.getContraption();
        MutablePair<StructureBlockInfo, MovementContext> existingActor = findActor(contraption, localPos);
        MovementBehaviour previousMovement = existingActor == null
                ? null
                : MovementBehaviour.REGISTRY.get(existingActor.getLeft().state());
        MovementContext previousContext = existingActor == null ? null : existingActor.getRight();
        MovementBehaviour movement = MovementBehaviour.REGISTRY.get(newInfo.state());

        if (previousMovement != movement && previousMovement != null && previousContext != null) {
            previousMovement.stopMoving(previousContext);
        }
        if (previousMovement != movement && previousMovement instanceof BlockRemovalAwareMovementBehaviour removalAware) {
            removalAware.onBlockRemoved(contraptionEntity, localPos);
        }

        contraption.getBlocks().put(localPos, newInfo);
        contraption.getIsLegacy().removeBoolean(localPos);

        Map<BlockPos, CompoundTag> updateTags = ((ContraptionAccessor) contraption).kaleidoscope_world_liquor$getUpdateTags();
        if (newInfo.nbt() == null) {
            updateTags.remove(localPos);
        } else {
            updateTags.put(localPos, newInfo.nbt());
        }

        MovingInteractionBehaviour interaction = MovingInteractionBehaviour.REGISTRY.get(newInfo.state());
        if (interaction == null) {
            contraption.getInteractors().remove(localPos);
        } else {
            contraption.getInteractors().put(localPos, interaction);
        }

        if (movement == null) {
            if (existingActor != null) {
                contraption.getActors().remove(existingActor);
            }
        } else if (existingActor != null && previousMovement == movement && previousContext != null) {
            existingActor.setLeft(newInfo);
            previousContext.state = newInfo.state();
            previousContext.blockEntityData = newInfo.nbt();
        } else {
            MovementContext context = new MovementContext(contraptionEntity.level(), newInfo, contraption);
            if (existingActor == null) {
                contraption.getActors().add(MutablePair.of(newInfo, context));
            } else {
                existingActor.setLeft(newInfo);
                existingActor.setRight(context);
            }
            movement.startMoving(context);
        }
    }

    private static MutablePair<StructureBlockInfo, MovementContext> findActor(Contraption contraption, BlockPos localPos) {
        for (MutablePair<StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
            if (actor.getLeft().pos().equals(localPos)) {
                return actor;
            }
        }
        return null;
    }

    public static void removeBlockFromContraption(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        MutablePair<StructureBlockInfo, MovementContext> actor = findActor(contraption, localPos);
        if (actor != null) {
            MovementBehaviour movement = MovementBehaviour.REGISTRY.get(actor.getLeft().state());
            if (movement instanceof BlockRemovalAwareMovementBehaviour removalAware) {
                removalAware.onBlockRemoved(contraptionEntity, localPos);
            }
            if (movement != null && actor.getRight() != null) {
                movement.stopMoving(actor.getRight());
            }
        }
        contraption.getBlocks().remove(localPos);
        contraption.getInteractors().remove(localPos);
        contraption.getActors().removeIf(entry -> entry.getLeft().pos().equals(localPos));
        ((ContraptionAccessor) contraption).kaleidoscope_world_liquor$getUpdateTags().remove(localPos);
        contraption.getIsLegacy().removeBoolean(localPos);
    }
}
