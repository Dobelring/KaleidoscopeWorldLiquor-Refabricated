package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.blockentity.BarCellarCabinetBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;

/**
 * 淘金热（treasure_guide）/ 斩首（beheading）/ 春野之息（bonemeal_spreader）/
 * 冰霜行者（frost_walker）/ 多段跳摔落豁免（multi_jump，mixin 部分）的服务端逻辑。
 */
public final class EventHandlers {
    private static final RandomSource RANDOM = RandomSource.create();

    private EventHandlers() {
    }

    public static void register() {
        // ===== 淘金热：击败生物双倍掉落 =====
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(EventHandlers::tequilaCapDamage);
        // 破势并入 ALLOW_DAMAGE（tequilaCapDamage 内处理，避免无敌帧吞伤）
        ServerLivingEntityEvents.AFTER_DEATH.register(EventHandlers::onLivingDeath);
        // ===== 淘金热：挖方块双倍掉落 =====
        PlayerBlockBreakEvents.AFTER.register(EventHandlers::onBlockBreak);
        // ===== 骨粉扩散（春野之息）与冰霜行者：玩家 tick =====
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
                var bonemeal = player.getEffect(ModEffects.BONEMEAL_SPREADER);
                if (bonemeal != null && bonemeal.getDuration() % 20 == 0) {
                    int amplifier = bonemeal.getAmplifier();
                    if (bonemeal.getDuration() % 20 == 0) {
                        spreadBonemealOnGround(player, amplifier);
                    }
                    if (bonemeal.getDuration() % 40 == 0) {
                        spreadBonemealOnPlants(player, amplifier);
                    }
                }
            }
        });
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
        // 淘金热：复制掉落物
        if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.TREASURE_GUIDE)) {
            int amplifier = attacker.getEffect(ModEffects.TREASURE_GUIDE).getAmplifier();
            double totalChance = 0.15 + amplifier * 0.05;
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
        } else if (entity instanceof Creeper) {
            return new ItemStack(Items.CREEPER_HEAD);
        } else if (entity instanceof WitherSkeleton) {
            return new ItemStack(Items.WITHER_SKELETON_SKULL);
        } else if (entity instanceof Player player) {
            ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
            // 1.21.11 玩家头用 profile 组件
            playerHead.set(DataComponents.PROFILE, net.minecraft.world.item.component.ResolvableProfile.createResolved(player.getGameProfile()));
            return playerHead;
        }
        return ItemStack.EMPTY;
    }

    /** 淘金热：挖掘作物 15%/矿石 20% 概率补一份掉落 */
    private static void onBlockBreak(net.minecraft.world.level.Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world.isClientSide() || player == null) {
            return;
        }
        var effect = player.getEffect(ModEffects.TREASURE_GUIDE);
        if (effect == null) {
            return;
        }
        int amplifier = effect.getAmplifier();
        double baseChance = 0.0;
        if (state.is(BlockTags.CROPS) || state.getBlock() instanceof CropBlock) {
            baseChance = 0.15;
        } else if (state.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ores")))) {
            baseChance = 0.2;
        }
        if (baseChance <= 0.0) {
            return;
        }
        double totalChance = baseChance + amplifier * 0.05;
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

    /** 春野之息：地面骨粉 */
    private static void spreadBonemealOnGround(Player player, int amplifier) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos playerPos = player.blockPosition();
        int range = 1 + amplifier;
        BlockPos targetPos = playerPos.offset(RANDOM.nextInt(range * 2 + 1) - range, 0, RANDOM.nextInt(range * 2 + 1) - range);
        applyBonemealIfGround(level, targetPos, player);
        applyBonemealIfGround(level, targetPos.below(), player);
    }

    /** 春野之息：植物骨粉 */
    private static void spreadBonemealOnPlants(Player player, int amplifier) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos playerPos = player.blockPosition();
        int range = 1 + amplifier;
        BlockPos targetPos = playerPos.offset(RANDOM.nextInt(range * 2 + 1) - range, 0, RANDOM.nextInt(range * 2 + 1) - range);
        applyBonemealIfPlant(level, targetPos, player);
        applyBonemealIfPlant(level, targetPos.below(), player);
    }

    private static void applyBonemealIfGround(ServerLevel level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bonemealableBlock && !isPlantBlock(state)) {
            bonemealableBlock.performBonemeal(level, level.getRandom(), pos, state);
        }
    }

    private static void applyBonemealIfPlant(ServerLevel level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bonemealableBlock && isPlantBlock(state)) {
            bonemealableBlock.performBonemeal(level, level.getRandom(), pos, state);
        }
    }

    private static boolean isPlantBlock(BlockState state) {
        if (!state.is(BlockTags.CROPS) && !state.is(BlockTags.SAPLINGS) && !state.is(BlockTags.FLOWERS)) {
            String className = state.getBlock().getClass().getName();
            return className.endsWith("VineBlock") || className.endsWith("CaveVinesBlock")
                    || className.endsWith("BambooBlock") || className.endsWith("SugarCaneBlock")
                    || className.endsWith("CactusBlock") || className.endsWith("NetherWartBlock")
                    || className.endsWith("CocoaBlock") || className.endsWith("SweetBerryBushBlock");
        }
        return true;
    }
}
