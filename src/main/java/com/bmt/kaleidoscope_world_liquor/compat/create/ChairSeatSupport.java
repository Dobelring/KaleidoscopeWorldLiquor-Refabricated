package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.ChairBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.SitUtil;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 装置上吧台凳的座位支撑（官方同名类的移植，只改 Create 包名）。
 * <p>
 * 与官方的唯一差别：官方在几个地方会顺手清掉 NeoForge 版 Create 写在实体上的
 * {@code ContraptionDismountLocation} 持久数据；Fabric 侧既没有 {@code getPersistentData()}，
 * create-fly 也完全不使用这个键，故这些清理调用直接省掉。
 */
public final class ChairSeatSupport {
    private static final double CHAIR_SEAT_HEIGHT = 0.65;
    private static final double CHAIR_RIDING_OFFSET = -0.3;

    private ChairSeatSupport() {
    }

    public static boolean isChairSeat(BlockState state) {
        return state != null && state.getBlock() instanceof ChairBlock;
    }

    public static double getSeatHeight(BlockState state) {
        if (isChairSeat(state)) {
            return CHAIR_SEAT_HEIGHT;
        }
        throw new IllegalArgumentException("Not a bar stool: " + state);
    }

    public static Vec3 getSeatEntityPosition(AbstractContraptionEntity contraptionEntity, BlockPos localPos, float partialTicks) {
        if (contraptionEntity.getContraption() == null) {
            return null;
        }
        StructureBlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
        if (info == null || !isChairSeat(info.state())) {
            return null;
        }
        Vec3 localCenter = Vec3.atCenterOf(localPos);
        return contraptionEntity.toGlobalVector(localCenter.add(0.0, getSeatHeight(info.state()) - 0.5, 0.0), partialTicks);
    }

    public static Vec3 getPassengerPosition(AbstractContraptionEntity contraptionEntity, Entity passenger, float partialTicks) {
        if (contraptionEntity.getContraption() == null) {
            return null;
        }
        BlockPos localPos = contraptionEntity.getContraption().getSeatOf(passenger.getUUID());
        if (localPos == null) {
            return null;
        }
        Vec3 seatEntityPosition = getSeatEntityPosition(contraptionEntity, localPos, partialTicks);
        return seatEntityPosition == null ? null : seatEntityPosition.add(0.0, CHAIR_RIDING_OFFSET, 0.0);
    }

    public static void ejectPassengers(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        int seatIndex = contraption.getSeats().indexOf(localPos);
        if (seatIndex < 0) {
            return;
        }
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
            }
        }
    }

    public static void removeSeat(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        Contraption contraption = contraptionEntity.getContraption();
        int removedIndex = contraption.getSeats().indexOf(localPos);
        if (removedIndex < 0) {
            return;
        }
        contraption.getSeats().remove(removedIndex);
        Iterator<Map.Entry<UUID, Integer>> mappingIterator = contraption.getSeatMapping().entrySet().iterator();
        while (mappingIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = mappingIterator.next();
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
                int seatIndex = context.data.getInt("SeatIndex").orElse(0);
                if (seatIndex == removedIndex) {
                    context.data.putInt("SeatIndex", -1);
                } else if (seatIndex > removedIndex) {
                    context.data.putInt("SeatIndex", seatIndex - 1);
                }
            }
        }
    }

    public static boolean restorePassenger(Level level, BlockPos worldPos, BlockState state, Entity passenger) {
        if (level.isClientSide() || !isChairSeat(state)) {
            return false;
        }
        BlockState placedState = level.getBlockState(worldPos);
        if (!isChairSeat(placedState)) {
            return false;
        }
        // 落座写法照本端口 ChairBlock（tavern 1.2.0.5 的 SitEntity 只有 (Level) 构造器，
        // 1.2.0.9 才有 (Level, BlockPos, double)；用 absSnapTo + SitUtil 两端都能编译）
        SitEntity seat = new SitEntity(level);
        seat.absSnapTo(worldPos.getX() + 0.5, worldPos.getY() + CHAIR_SEAT_HEIGHT, worldPos.getZ() + 0.5);
        seat.setYRot(placedState.getValue(ChairBlock.FACING).toYRot());
        SitUtil.addSitEntity(level, worldPos, seat, seat.position());
        if (!level.addFreshEntity(seat)) {
            return false;
        }
        passenger.stopRiding();
        if (!passenger.startRiding(seat, true, true)) {
            seat.discard();
            return false;
        }
        return true;
    }
}
