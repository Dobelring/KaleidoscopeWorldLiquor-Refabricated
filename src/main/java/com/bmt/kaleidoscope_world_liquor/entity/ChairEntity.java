package com.bmt.kaleidoscope_world_liquor.entity;

import com.bmt.kaleidoscope_world_liquor.block.ChairBlock;
import com.bmt.kaleidoscope_world_liquor.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ChairEntity extends Entity {
    // 座面顶=15/16（bar/stool/base.json 顶面 y=15）。1.20.1 原值 0.65 会让腿部插进凳体，
    // 调到座面顶脚底贴面（1.21.11 同步修改）。
    private static final double SEAT_HEIGHT = 0.9375D;

    public ChairEntity(EntityType<? extends ChairEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static ChairEntity create(Level level, BlockPos pos) {
        ChairEntity entity = new ChairEntity(ModEntities.CHAIR, level);
        entity.setPos(pos.getX() + 0.5, pos.getY() + SEAT_HEIGHT, pos.getZ() + 0.5);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            boolean shouldDiscard = this.getPassengers().isEmpty();
            if (!shouldDiscard) {
                BlockPos pos = this.blockPosition();
                shouldDiscard = !(this.level().getBlockState(pos).getBlock() instanceof ChairBlock);
            }
            if (shouldDiscard) {
                this.discard();
            }
        }
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity passenger) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Vec3 candidate = new Vec3(
                    this.getX() + direction.getStepX() * 1.0,
                    this.getY(),
                    this.getZ() + direction.getStepZ() * 1.0);
            if (this.isPositionSafe(passenger, candidate)) {
                this.discard();
                return candidate.add(0.0, 0.1, 0.0);
            }
        }
        this.discard();
        return super.getDismountLocationForPassenger(passenger);
    }

    private boolean isPositionSafe(LivingEntity passenger, Vec3 pos) {
        AABB passengerBox = passenger.getBoundingBox().move(pos.subtract(passenger.position()));
        return this.level().noCollision(passenger, passengerBox);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput valueInput) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput valueOutput) {
    }

    @Override
    public @NotNull ClientboundAddEntityPacket getAddEntityPacket(@NotNull net.minecraft.server.level.ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }
}
