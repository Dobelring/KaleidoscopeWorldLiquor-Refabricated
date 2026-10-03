package com.bmt.kaleidoscope_world_liquor.compat.create.network;

import com.bmt.kaleidoscope_world_liquor.compat.create.BlockRemovalAwareMovementBehaviour;
import com.bmt.kaleidoscope_world_liquor.mixins.create.accessor.ContraptionAccessor;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

public final class ContraptionInteractionUtil {
    private ContraptionInteractionUtil() {
    }

    public static void updateContraptionData(AbstractContraptionEntity contraptionEntity, BlockPos localPos, StructureBlockInfo newInfo) {
        updateContraptionDataLocally(contraptionEntity, localPos, newInfo);
        if (!contraptionEntity.level().isClientSide) {
            PacketHandler.sendToTracking(
                new ContraptionChangedPacket(contraptionEntity.getId(), localPos, newInfo.state(), newInfo.nbt()), contraptionEntity
            );
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
        Map<BlockPos, CompoundTag> updateTags = ((ContraptionAccessor)contraption).getUpdateTags();
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
        ((ContraptionAccessor)contraption).getUpdateTags().remove(localPos);
        contraption.getIsLegacy().removeBoolean(localPos);
    }

    private static MutablePair<StructureBlockInfo, MovementContext> findActor(Contraption contraption, BlockPos localPos) {
        for (MutablePair<StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
            if (actor.getLeft().pos().equals(localPos)) {
                return actor;
            }
        }

        return null;
    }

    public static void syncBlockRemoval(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        if (!contraptionEntity.level().isClientSide) {
            BlockState airState = Blocks.AIR.defaultBlockState();
            PacketHandler.sendToTracking(new ContraptionChangedPacket(contraptionEntity.getId(), localPos, airState, null), contraptionEntity);
        }
    }

    public static void playSound(
        AbstractContraptionEntity contraptionEntity, BlockPos localPos, SoundEvent soundEvent, SoundSource source, float volume, float pitch
    ) {
        Vec3 globalPos = contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0F);
        BlockPos soundPos = new BlockPos((int)globalPos.x, (int)globalPos.y, (int)globalPos.z);
        contraptionEntity.level().playSound(null, soundPos, soundEvent, source, volume, pitch);
    }
}
