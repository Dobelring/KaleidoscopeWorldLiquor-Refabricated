package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** 装置上的命中判定与酒窖柜槽位换算（官方同名类的移植，只改 Create 包名）。 */
final class ContraptionInteractionSupport {
    private static final double EPSILON = 1.0E-4;

    private ContraptionInteractionSupport() {
    }

    static Optional<Hit> findHit(Player player, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 endPosition = eyePosition.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        Vec3 localEyePosition = contraptionEntity.toLocalVector(eyePosition, 1.0F);
        Vec3 localEndPosition = contraptionEntity.toLocalVector(endPosition, 1.0F);
        Optional<Vec3> intersection = new AABB(localPos).clip(localEyePosition, localEndPosition);
        return intersection.map(point -> new Hit(point, getHitFace(point, localPos)));
    }

    static int getCellarCabinetSlot(BlockState state, BlockPos localPos, Hit hit) {
        if (!(state.getBlock() instanceof BarCellarCabinetBlock)) {
            return -1;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (hit.face() != facing) {
            return -1;
        }
        double localX = getLocalX(facing, localPos, hit.point());
        double relativeY = hit.point().y - localPos.getY();
        int column = Math.min(2, Math.max(0, (int) (localX * 3.0)));
        int rowFromTop = Math.min(2, Math.max(0, (int) (relativeY * 3.0)));
        return column + (2 - rowFromTop) * 3;
    }

    static boolean isLeftSide(Direction facing, BlockPos localPos, Vec3 hitPoint) {
        double relativeX = hitPoint.x - localPos.getX();
        double relativeZ = hitPoint.z - localPos.getZ();
        return switch (facing) {
            case NORTH -> relativeX > 0.5;
            case SOUTH -> relativeX < 0.5;
            case EAST -> relativeZ < 0.5;
            case WEST -> relativeZ > 0.5;
            default -> false;
        };
    }

    static void playSound(AbstractContraptionEntity contraptionEntity, BlockPos localPos, SoundEvent sound) {
        ContraptionDataSync.playSound(contraptionEntity, localPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static double getLocalX(Direction direction, BlockPos pos, Vec3 hitPoint) {
        double relativeX = hitPoint.x - pos.getX();
        double relativeZ = hitPoint.z - pos.getZ();
        return switch (direction) {
            case NORTH -> 1.0 - relativeX;
            case SOUTH -> relativeX;
            case EAST -> 1.0 - relativeZ;
            case WEST -> relativeZ;
            default -> 0.5;
        };
    }

    private static Direction getHitFace(Vec3 point, BlockPos pos) {
        double relativeX = point.x - pos.getX();
        double relativeY = point.y - pos.getY();
        double relativeZ = point.z - pos.getZ();
        if (relativeX <= EPSILON) {
            return Direction.WEST;
        } else if (relativeX >= 1.0 - EPSILON) {
            return Direction.EAST;
        } else if (relativeY <= EPSILON) {
            return Direction.DOWN;
        } else if (relativeY >= 1.0 - EPSILON) {
            return Direction.UP;
        } else if (relativeZ <= EPSILON) {
            return Direction.NORTH;
        } else if (relativeZ >= 1.0 - EPSILON) {
            return Direction.SOUTH;
        }
        double xDistance = Math.min(relativeX, 1.0 - relativeX);
        double yDistance = Math.min(relativeY, 1.0 - relativeY);
        double zDistance = Math.min(relativeZ, 1.0 - relativeZ);
        if (xDistance <= yDistance && xDistance <= zDistance) {
            return relativeX < 0.5 ? Direction.WEST : Direction.EAST;
        } else if (yDistance <= zDistance) {
            return relativeY < 0.5 ? Direction.DOWN : Direction.UP;
        }
        return relativeZ < 0.5 ? Direction.NORTH : Direction.SOUTH;
    }

    record Hit(Vec3 point, Direction face) {
    }
}
