package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.bmt.kaleidoscope_world_liquor.mixin.accessor.BrushableBlockEntityAccessor;
import com.bmt.kaleidoscope_world_liquor.network.NetworkHandler;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePayload;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;

/**
 * 淘金热（treasure_guide）/ 斩首（beheading）/ 宝藏感知（treasure_sense，服务端扫描下发）/
 * 冰霜行者（frost_walker）/ 多段跳摔落豁免（multi_jump，mixin 部分）的服务端逻辑。
 * <p>官方 1.1.11：春野之息（bonemeal_spreader）整体移除。
 */
public final class EventHandlers {
    private static final RandomSource RANDOM = RandomSource.create();
    /** 官方 1.1.11：宝藏感知服务端扫描半径（矿车 inflate 用） */
    private static final int TREASURE_SENSE_RADIUS = 24;

    private EventHandlers() {
    }

    public static void register() {
        // ===== 淘金热：击败生物双倍掉落 =====
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(EventHandlers::tequilaCapDamage);
        // 破势并入 ALLOW_DAMAGE（tequilaCapDamage 内处理，避免无敌帧吞伤）
        ServerLivingEntityEvents.AFTER_DEATH.register(EventHandlers::onLivingDeath);
        // ===== 淘金热：挖方块双倍掉落 =====
        PlayerBlockBreakEvents.AFTER.register(EventHandlers::onBlockBreak);
        // ===== 冰霜行者与宝藏感知下发：玩家 tick =====
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 每个玩家在 PlayerTick 的等效：在服务端 tick 末尾遍历在线玩家
            for (Player player : server.getPlayerList().getPlayers()) {
                if (player.isSpectator()) {
                    continue;
                }
                var frost = player.getEffect(ModEffects.FROST_WALKER);
                if (frost != null) {
                    applyFrostWalker(player, frost.getAmplifier());
                }
                updateCreativeFlight(player);
                // 官方 1.1.11：春野之息移除，改为宝藏感知每 20 tick 服务端扫描并下发
                if (player.hasEffect(ModEffects.TREASURE_SENSE) && player.tickCount % 20 == 0) {
                    syncTreasureSenseTargets(player);
                }
            }
        });
    }

    /**
     * 官方 1.1.11：扫描玩家周围 5×5 区块、24 格内「有战利品表」的容器方块
     * （随机战利品容器 / 沙砾类 Brushable / 饰纹陶罐）+ 近身 24 格的容器矿车，
     * 打包成 TreasureSensePayload 下发给该玩家。
     */
    private static void syncTreasureSenseTargets(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ArrayList<BlockPos> targets = new ArrayList<>();
        BlockPos playerPos = player.blockPosition();
        ChunkPos playerChunkPos = ChunkPos.containing(playerPos);
        int chunkRadius = 2;
        double radiusSq = 576.0;

        for (int cx = playerChunkPos.x() - chunkRadius; cx <= playerChunkPos.x() + chunkRadius; cx++) {
            for (int cz = playerChunkPos.z() - chunkRadius; cz <= playerChunkPos.z() + chunkRadius; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                if (chunk == null || chunk.isEmpty()) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.isRemoved()) {
                        continue;
                    }
                    BlockPos pos = be.getBlockPos();
                    if (pos.distSqr(playerPos) > radiusSq) {
                        continue;
                    }
                    if (be instanceof RandomizableContainerBlockEntity container && container.getLootTable() != null) {
                        targets.add(pos.immutable());
                    } else if (be instanceof BrushableBlockEntity
                            && ((BrushableBlockEntityAccessor) be).kwl$getLootTable() != null) {
                        targets.add(pos.immutable());
                    } else if (be instanceof DecoratedPotBlockEntity pot && pot.getLootTable() != null) {
                        targets.add(pos.immutable());
                    }
                }
            }
        }

        ArrayList<Integer> lootMinecartIds = new ArrayList<>();
        for (AbstractMinecartContainer minecart : level.getEntitiesOfClass(AbstractMinecartContainer.class,
                player.getBoundingBox().inflate(TREASURE_SENSE_RADIUS),
                cart -> !cart.isRemoved() && cart.getContainerLootTable() != null)) {
            lootMinecartIds.add(minecart.getId());
        }

        NetworkHandler.send(serverPlayer, new TreasureSensePayload(targets, lootMinecartIds));
    }

    /** 龙舌兰（tequila）：单次伤害不超过最大生命的百分比（40%-5%/级，下限 5%） */
    private static boolean tequilaCapDamage(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        var effect = entity.getEffect(ModEffects.TEQUILA);
        if (effect != null) {
            // 1.20.1 用 LivingHurt 事件在护甲前削减——此处无法修改已结算伤害，
            // 用"超上限部分退回"近似：直接把血量抬高到受伤前+超出量（等效削减）
            float maxDamagePercent = Math.max(0.05F, 0.4F - effect.getAmplifier() * 0.05F);
        float maxAllowedDamage = entity.getMaxHealth() * maxDamagePercent;
        if (amount > maxAllowedDamage) {
            // 1.20.1 用 LivingHurt 护甲前削减；Fabric 无伤害修改事件，
            // ALLOW_DAMAGE 的取消+重打语义等价：取消本次并以削减后伤害重打
            entity.hurt(source, maxAllowedDamage);
            return false;
        }
        }
        // 破势（ground_crit）：地面近战 20%+10%/级 → 暴击 1.5 倍（非原版跳劈时）
        // Fabric 无伤害修改事件，与龙舌兰同款 cancel+重打一次结算（AFTER_DAMAGE 补刀会被无敌帧吞）
        if (source.getDirectEntity() instanceof LivingEntity critAttacker
                && critAttacker instanceof Player critPlayer
                && critPlayer.hasEffect(ModEffects.GROUND_CRIT)
                && isMeleeAttack(source)
                && !CRIT_PROCESSING.contains(entity.getUUID())) {
            int critAmplifier = critPlayer.getEffect(ModEffects.GROUND_CRIT).getAmplifier();
            double totalCritChance = 0.2 + critAmplifier * 0.1;
            if (!isVanillaCrit(critPlayer) && RANDOM.nextDouble() < totalCritChance) {
                CRIT_PROCESSING.add(entity.getUUID());
                try {
                    critPlayer.crit(entity);
                    DamageSource critSource = entity.damageSources().playerAttack(critPlayer);
                    if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                            && entity.hurtServer(serverLevel, critSource, amount * 1.5F)) {
                        return false;
                    }
                    // 结算失败则放行原伤害
                    return true;
                } finally {
                    CRIT_PROCESSING.remove(entity.getUUID());
                }
            }
        }
        // 手肘击（elbow_strike）攻击音效
        if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.ELBOW_STRIKE)) {
            attacker.playSound(com.bmt.kaleidoscope_world_liquor.init.ModSounds.ICE_TEA_EAT, 0.6F, 1.0F);
        }
        // 斩首（beheading）：概率秒杀非 boss 生物并保证掉头
        if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.BEHEADING)) {
            LivingEntity target = entity;
            if (!target.is(ModTags.BOSSES) && target.isAlive()) {
                int amplifier = attacker.getEffect(ModEffects.BEHEADING).getAmplifier();
                double totalChance = 0.04 + amplifier * 0.03;
                if (RANDOM.nextDouble() < totalChance) {
                    float safeDamage = 10000.0F;
                    float healthBefore = target.getHealth();
                    DamageSource killSource = attacker instanceof Player player
                            ? entity.damageSources().playerAttack(player)
                            : entity.damageSources().mobAttack(attacker);
                    target.hurt(killSource, safeDamage);
                    float healthAfter = target.getHealth();
                    if (Float.isNaN(healthAfter) || healthAfter > healthBefore) {
                        target.setHealth(1.0F);
                        target.hurt(killSource, 2.0F);
                        if (target.isAlive()) {
                            target.setHealth(0.0F);
                            target.die(killSource);
                        }
                    }
                }
            }
        }
        return true;
    }

    /** 斩首掉头 + 淘金热双倍掉落（AFTER_DEATH）：目标被标记秒杀或攻击者有斩首效果时补头；
     *  攻击者有淘金热时按概率复制死亡位置 2 格内的掉落物（1.21.1 同款） */
    private static void onLivingDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        // 淘金热：复制掉落物（官方 1.1.11：概率封顶 100%）
        if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.TREASURE_GUIDE)) {
            int amplifier = attacker.getEffect(ModEffects.TREASURE_GUIDE).getAmplifier();
            double totalChance = Math.min(0.15 + amplifier * 0.05, 1.0);
            if (RANDOM.nextDouble() < totalChance) {
                java.util.List<ItemEntity> drops = entity.level().getEntitiesOfClass(
                        ItemEntity.class, entity.getBoundingBox().inflate(2.0), itemEntity -> itemEntity.isAlive());
                for (ItemEntity itemEntity : new ArrayList<>(drops)) {
                    ItemStack extraStack = itemEntity.getItem().copy();
                    ItemEntity extraEntity = new ItemEntity(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), extraStack);
                    extraEntity.setDefaultPickUpDelay();
                    entity.level().addFreshEntity(extraEntity);
                }
            }
        }
        // 斩首掉头
        var attackerOpt = source.getEntity() instanceof LivingEntity l && l.hasEffect(ModEffects.BEHEADING) ? java.util.Optional.of(l) : java.util.Optional.<LivingEntity>empty();
        boolean beheadingKill = attackerOpt.isPresent() && !entity.is(ModTags.BOSSES);
        if (!beheadingKill) {
            return;
        }
        if (entity.level() instanceof ServerLevel serverLevel) {
            ItemStack head = getEntityHead(entity);
            if (!head.isEmpty()) {
                serverLevel.addFreshEntity(new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), head));
            }
        }
    }

    /** 破势（ground_crit）：地面近战 20%+10%/级 触发暴击 ×1.5（非原版跳劈时）；
     *  Fabric 无伤害修改事件，用 AFTER_DAMAGE 补 0.5 倍额外伤害 + 暴击粒子 */
    private static final java.util.Set<java.util.UUID> CRIT_PROCESSING = new java.util.HashSet<>();

    private static boolean isMeleeAttack(DamageSource source) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) || source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return source.getEntity() != null && source.getDirectEntity() != null
                ? source.getDirectEntity() == source.getEntity() : false;
    }

    private static boolean isVanillaCrit(Player player) {
        return !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                && !player.isPassenger()
                && player.getDeltaMovement().y < 0.0;
    }

    private static ItemStack getEntityHead(LivingEntity entity) {
        if (entity.getClass() == Zombie.class) {
            return new ItemStack(Items.ZOMBIE_HEAD);
        } else if (entity.getClass() == Skeleton.class) {
            return new ItemStack(Items.SKELETON_SKULL);
        } else if (entity.getClass() == Creeper.class) {
            return new ItemStack(Items.CREEPER_HEAD);
        } else if (entity.getClass() == WitherSkeleton.class) {
            return new ItemStack(Items.WITHER_SKELETON_SKULL);
        } else if (entity.getClass() == Piglin.class) {
            return new ItemStack(Items.PIGLIN_HEAD);
        } else if (entity instanceof Player player) {
            ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
            // 1.21.11 玩家头用 profile 组件
            playerHead.set(DataComponents.PROFILE, net.minecraft.world.item.component.ResolvableProfile.createResolved(player.getGameProfile()));
            return playerHead;
        } else {
            // 官方 1.1.11：通用命名匹配（<ns>:<path>_head/_skull、skull_<path>/head_<path>、
            // dead_<path>、<path>_item），让带头颅物品的模组生物也能掉头
            net.minecraft.resources.Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (entityId != null) {
                String ns = entityId.getNamespace();
                String path = entityId.getPath();
                String[][] patterns = new String[][]{
                        {path + "_head", path + "_skull"},
                        {"skull_" + path, "head_" + path},
                        {"dead_" + path},
                        {path + "_item"}
                };
                for (String[] group : patterns) {
                    for (String suffix : group) {
                        net.minecraft.resources.Identifier headId =
                                net.minecraft.resources.Identifier.fromNamespaceAndPath(ns, suffix);
                        Item item = BuiltInRegistries.ITEM.getValue(headId);
                        if (item != null && item != Items.AIR) {
                            return new ItemStack(item);
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }
    }

    /** 淘金热：挖掘作物/矿物（官方 1.1.11 起统一 15%+5%/级，封顶 100%）补一份掉落 */
    private static void onBlockBreak(net.minecraft.world.level.Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world.isClientSide() || player == null) {
            return;
        }
        var effect = player.getEffect(ModEffects.TREASURE_GUIDE);
        if (effect == null) {
            return;
        }
        int amplifier = effect.getAmplifier();
        boolean isCrop = state.is(BlockTags.CROPS) || state.getBlock() instanceof CropBlock;
        boolean isOre = state.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ores")));
        if (!isCrop && !isOre) {
            return;
        }
        double totalChance = Math.min(0.15 + amplifier * 0.05, 1.0);
        if (RANDOM.nextDouble() < totalChance && world instanceof ServerLevel serverLevel) {
            LootParams.Builder params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
            for (ItemStack drop : state.getDrops(params)) {
                if (!drop.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(serverLevel,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop.copy());
                    itemEntity.setDefaultPickUpDelay();
                    serverLevel.addFreshEntity(itemEntity);
                }
            }
        }
    }

    /** 冰霜行者：1.21.11 的 frost_walker 是数据驱动 location_changed 效果（slots: []），手动放置 frosted ice 圆盘（原版 ReplaceDisk 语义） */
    /**
     * 嗨棒（creative_flight）：加 buff 只补 mayfly、不清已有飞行状态；掉 buff 只在非创造/旁观下清除。
     * 官方 1.1.9 原样（「修复飞行buff会覆盖其他模组飞行能力的bug」）——不要自行加"记录来源"之类的改动。
     */
    private static void updateCreativeFlight(Player player) {
        Abilities abilities = player.getAbilities();
        if (player.hasEffect(ModEffects.CREATIVE_FLIGHT)) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (!player.isCreative() && !player.isSpectator() && (abilities.mayfly || abilities.flying)) {
            abilities.flying = false;
            abilities.mayfly = false;
            player.onUpdateAbilities();
            player.fallDistance = 0.0F;
        }
    }

    private static void applyFrostWalker(Player player, int amplifier) {
        if (!(player.level() instanceof ServerLevel serverLevel) || !player.onGround()) {
            return;
        }
        BlockPos center = player.blockPosition();
        int radius = 2 + Math.min(amplifier, 4);
        for (BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius), center.offset(radius, -1, radius))) {
            if (serverLevel.getFluidState(pos).is(net.minecraft.world.level.material.Fluids.WATER) && serverLevel.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.LiquidBlock
                    && serverLevel.getBlockState(pos.above()).isAir()) {
                serverLevel.setBlockAndUpdate(pos.immutable(), net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState());
            }
        }
    }

    /** 春野之息（bonemeal_spreader）随官方 1.1.11 整体移除：地面/植物骨粉扩散逻辑删除。 */
}
