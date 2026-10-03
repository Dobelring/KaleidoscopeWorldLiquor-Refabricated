package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.compat.create.network.ContraptionInteractionUtil;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class ContraptionInteractionSupport {
    private static final double EPSILON = 1.0E-4;
    // TODO(fabric): 原为 player.getBlockReach()（Forge IForgePlayer 扩展，基于 reach 属性，基础值 5.0）。
    //  Fabric 1.20.1 无对应 API（未接 reach-entity-attributes），取原版生存基础触及距离 5.0。
    private static final double BLOCK_REACH = 5.0;

    private ContraptionInteractionSupport() {
    }

    static Optional<ContraptionInteractionSupport.Hit> findHit(Player player, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 endPosition = eyePosition.add(player.getViewVector(1.0F).scale(BLOCK_REACH));
        Vec3 localEyePosition = contraptionEntity.toLocalVector(eyePosition, 1.0F);
        Vec3 localEndPosition = contraptionEntity.toLocalVector(endPosition, 1.0F);
        Optional<Vec3> intersection = new AABB(localPos).clip(localEyePosition, localEndPosition);
        return intersection.map(point -> new ContraptionInteractionSupport.Hit(point, getHitFace(point, localPos)));
    }

    static int getCellarCabinetSlot(BlockState state, BlockPos localPos, ContraptionInteractionSupport.Hit hit) {
        if (!(state.getBlock() instanceof BarCellarCabinetBlock)) {
            return -1;
        } else {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (hit.face() != facing) {
                return -1;
            } else {
                double localX = getLocalX(facing, localPos, hit.point());
                double relativeY = hit.point().y - localPos.getY();
                int column = Math.min(2, Math.max(0, (int)(localX * 3.0)));
                int rowFromTop = Math.min(2, Math.max(0, (int)(relativeY * 3.0)));
                return column + (2 - rowFromTop) * 3;
            }
        }
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
        ContraptionInteractionUtil.playSound(contraptionEntity, localPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
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
        if (relativeX <= 1.0E-4) {
            return Direction.WEST;
        } else if (relativeX >= 0.9999) {
            return Direction.EAST;
        } else if (relativeY <= 1.0E-4) {
            return Direction.DOWN;
        } else if (relativeY >= 0.9999) {
            return Direction.UP;
        } else if (relativeZ <= 1.0E-4) {
            return Direction.NORTH;
        } else if (relativeZ >= 0.9999) {
            return Direction.SOUTH;
        } else {
            double xDistance = Math.min(relativeX, 1.0 - relativeX);
            double yDistance = Math.min(relativeY, 1.0 - relativeY);
            double zDistance = Math.min(relativeZ, 1.0 - relativeZ);
            if (xDistance <= yDistance && xDistance <= zDistance) {
                return relativeX < 0.5 ? Direction.WEST : Direction.EAST;
            } else if (yDistance <= zDistance) {
                return relativeY < 0.5 ? Direction.DOWN : Direction.UP;
            } else {
                return relativeZ < 0.5 ? Direction.NORTH : Direction.SOUTH;
            }
        }
    }

    static final class Hit {
        private final Vec3 point;
        private final Direction face;

        private Hit(Vec3 point, Direction face) {
            this.point = point;
            this.face = face;
        }

        Vec3 point() {
            return this.point;
        }

        Direction face() {
            return this.face;
        }
    }
}
