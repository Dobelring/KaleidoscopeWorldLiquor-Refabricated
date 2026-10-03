package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.ChairBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

public final class ChairSeatSupport {
    private static final double CHAIR_SEAT_HEIGHT = 0.65;
    private static final double CHAIR_RIDING_OFFSET = 0.0;
    private static final double CHAIR_SIT_ENTITY_Y = 0.9;

    private ChairSeatSupport() {
    }

    public static boolean isChairSeat(BlockState state) {
        return state != null && state.getBlock() instanceof ChairBlock;
    }

    public static double getSeatHeight(BlockState state) {
        if (isChairSeat(state)) {
            return 0.65;
        } else {
            throw new IllegalArgumentException("Not a bar stool: " + state);
        }
    }

    public static Vec3 getSeatEntityPosition(AbstractContraptionEntity contraptionEntity, BlockPos localPos, float partialTicks) {
        if (contraptionEntity.getContraption() == null) {
            return null;
        } else {
            StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
            if (info != null && isChairSeat(info.state())) {
                Vec3 localCenter = Vec3.atCenterOf(localPos);
                return contraptionEntity.toGlobalVector(localCenter.add(0.0, getSeatHeight(info.state()) - 0.5, 0.0), partialTicks);
            } else {
                return null;
            }
        }
    }

    public static Vec3 getPassengerPosition(AbstractContraptionEntity contraptionEntity, Entity passenger, float partialTicks) {
        if (contraptionEntity.getContraption() == null) {
            return null;
        } else {
            BlockPos localPos = contraptionEntity.getContraption().getSeatOf(passenger.getUUID());
            if (localPos == null) {
                return null;
            } else {
                Vec3 seatEntityPosition = getSeatEntityPosition(contraptionEntity, localPos, partialTicks);
                return seatEntityPosition == null ? null : seatEntityPosition.add(0.0, 0.0 + passenger.getMyRidingOffset(), 0.0);
            }
        }
    }

    public static void ejectPassengers(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        int seatIndex = contraption.getSeats().indexOf(localPos);
        if (seatIndex >= 0) {
            List<Entity> passengers = new ArrayList<>(contraptionEntity.getPassengers());
            Map<UUID, Integer> seatMapping = contraption.getSeatMapping();

            for (Entity passenger : passengers) {
                if (Integer.valueOf(seatIndex).equals(seatMapping.get(passenger.getUUID()))) {
                    Vec3 position = getPassengerPosition(contraptionEntity, passenger, 1.0F);
                    passenger.stopRiding();
                    seatMapping.remove(passenger.getUUID());
                    if (position != null) {
                        passenger.teleportTo(position.x, position.y, position.z);
                    }

                    // TODO(fabric): Forge IForgeEntity#getPersistentData() 在 Fabric 1.20.1 无对应物
                    //  （未接 Porting Lib 的 getCustomData 扩展），无法清除该键；
                    //  create 的 AbstractContraptionEntity.getDismountLocationForPassenger 读取后会自行删除，影响有限
                    // passenger.getPersistentData().remove("ContraptionDismountLocation");
                }
            }
        }
    }

    public static void removeSeat(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        int removedIndex = contraption.getSeats().indexOf(localPos);
        if (removedIndex >= 0) {
            contraption.getSeats().remove(removedIndex);
            Iterator<Entry<UUID, Integer>> mappingIterator = contraption.getSeatMapping().entrySet().iterator();

            while (mappingIterator.hasNext()) {
                Entry<UUID, Integer> entry = mappingIterator.next();
                Integer seatIndex = entry.getValue();
                if (seatIndex == null || seatIndex == removedIndex) {
                    mappingIterator.remove();
                } else if (seatIndex > removedIndex) {
                    entry.setValue(seatIndex - 1);
                }
            }

            for (MutablePair<StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
                MovementContext context = actor.getRight();
                if (context != null && context.data.contains("SeatIndex")) {
                    int seatIndex = context.data.getInt("SeatIndex");
                    if (seatIndex == removedIndex) {
                        context.data.putInt("SeatIndex", -1);
                    } else if (seatIndex > removedIndex) {
                        context.data.putInt("SeatIndex", seatIndex - 1);
                    }
                }
            }
        }
    }

    public static boolean restorePassenger(Level level, BlockPos worldPos, BlockState state, Entity passenger) {
        if (!level.isClientSide && isChairSeat(state)) {
            BlockState placedState = level.getBlockState(worldPos);
            if (!isChairSeat(placedState)) {
                return false;
            } else {
                SitEntity chairEntity = new SitEntity(level, worldPos, 0.9);
                Direction facing = placedState.getValue(ChairBlock.FACING);
                chairEntity.setYRot(facing.toYRot());
                if (!level.addFreshEntity(chairEntity)) {
                    return false;
                } else {
                    passenger.stopRiding();
                    if (!passenger.startRiding(chairEntity, true)) {
                        chairEntity.discard();
                        return false;
                    } else {
                        // TODO(fabric): Forge IForgeEntity#getPersistentData() 在 Fabric 1.20.1 无对应物
                        //  （未接 Porting Lib 的 getCustomData 扩展），无法清除该键；
                        //  create 的 AbstractContraptionEntity.getDismountLocationForPassenger 读取后会自行删除，影响有限
                        // passenger.getPersistentData().remove("ContraptionDismountLocation");
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
    }
}
