package com.bmt.kaleidoscope_world_liquor.effect;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 宿命之海（respawn）效果。
 * <p>
 * 官方 1.1.12-fix 重构：弃用手写安全点搜索（isSafePosition/findSafePositionAround/searchSameYLevel
 * 与 no_dimension 提示），改用原版 {@link Player#findRespawnPositionAndUseSpawnBlock}
 * （含床/重生锚朝向、keepInventory 规则）；无有效重生点时经
 * {@link PlayerRespawnLogic#getSpawnPosInChunk} 回退主世界共享出生点。
 */
public class RespawnEffect extends MobEffect {
    public RespawnEffect() {
        super(MobEffectCategory.NEUTRAL, 8900331);
    }

    public boolean isInstantenous() {
        return true;
    }

    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health) {
        this.performEffect(livingEntity, amplifier);
    }

    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        this.performEffect(livingEntity, amplifier);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration == 1;
    }

    private void performEffect(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (!level.isClientSide()) {
            if (entity instanceof ServerPlayer serverPlayer) {
                level.playSound(
                    null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F
                );
                MinecraftServer server = level.getServer();
                boolean keepInventory = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                float respawnAngle = serverPlayer.getRespawnAngle();
                boolean respawnForced = serverPlayer.isRespawnForced();
                ServerLevel respawnLevel = server.getLevel(serverPlayer.getRespawnDimension());
                Optional<Vec3> respawnPosition = Optional.empty();
                if (respawnLevel != null && respawnPos != null) {
                    respawnPosition = Player.findRespawnPositionAndUseSpawnBlock(respawnLevel, respawnPos, respawnAngle, respawnForced, keepInventory);
                }

                float xRot = 0.0F;
                ServerLevel targetLevel;
                Vec3 targetPos;
                float yRot;
                if (respawnPosition.isPresent()) {
                    targetLevel = respawnLevel;
                    targetPos = respawnPosition.get();
                    BlockState respawnState = respawnLevel.getBlockState(respawnPos);
                    if (!respawnState.is(BlockTags.BEDS) && !respawnState.is(Blocks.RESPAWN_ANCHOR)) {
                        yRot = respawnAngle;
                    } else {
                        Vec3 direction = Vec3.atBottomCenterOf(respawnPos).subtract(targetPos).normalize();
                        yRot = (float) Mth.wrapDegrees(Mth.atan2(direction.z, direction.x) * (180.0 / Math.PI) - 90.0);
                    }
                } else {
                    targetLevel = server.overworld();
                    BlockPos sharedSpawnPos = targetLevel.getSharedSpawnPos();
                    BlockPos safeSpawn = PlayerRespawnLogic.getSpawnPosInChunk(targetLevel, new ChunkPos(sharedSpawnPos));
                    targetPos = Vec3.atBottomCenterOf(safeSpawn != null ? safeSpawn : sharedSpawnPos);
                    yRot = targetLevel.getSharedSpawnAngle();
                }

                ResourceKey<Level> targetDimension = targetLevel.dimension();
                if (level.dimension() == targetDimension) {
                    serverPlayer.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                    serverPlayer.setYRot(yRot);
                    serverPlayer.setXRot(xRot);
                } else {
                    serverPlayer.teleportTo(targetLevel, targetPos.x, targetPos.y, targetPos.z, yRot, xRot);
                }

                serverPlayer.fallDistance = 0.0F;
                serverPlayer.level()
                    .playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
            }
        }
    }
}
