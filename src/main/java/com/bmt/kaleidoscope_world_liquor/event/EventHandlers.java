package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.BrushableBlockEntityAccessor;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.RandomizableContainerBlockEntityAccessor;
import com.bmt.kaleidoscope_world_liquor.network.NetworkHandler;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePacket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.util.Mth;

/**
 * 原 Forge 版 @EventBusSubscriber(Bus.FORGE) 的核心事件集合（413 行）。
 * <p>
 * 事件换型对照（详见批次报告）：
 * <ul>
 *   <li>LivingAttackEvent（肘击音效 + 斩首）→ ServerLivingEntityEvents.ALLOW_DAMAGE</li>
 *   <li>LivingHurtEvent（破势 ground_crit 改伤害）→ mixin/LivingEntityDamageMixin
 *       （actuallyHurt 内 getDamageAfterMagicAbsorb 之后，等价 Forge 触发点）</li>
 *   <li>LivingDropsEvent（淘金热复制掉落 + 斩首补头）→ mixin/LivingEntityDropsMixin +
 *       mixin/EntitySpawnAtLocationMixin 捕获 dropAllDeathLoot 生成的 ItemEntity</li>
 *   <li>BreakEvent（淘金热挖矿复制掉落）→ PlayerBlockBreakEvents.BEFORE</li>
 *   <li>PlayerTickEvent（Phase.END、仅服务端）→ ServerTickEvents.END_WORLD_TICK 遍历玩家</li>
 *   <li>RenderTickEvent（客户端每帧 END）→ ClientTickEvents.END_CLIENT_TICK
 *       （本方法只刷新发光标记，每 tick 一次、渲染前执行，语义等价）</li>
 * </ul>
 */
public class EventHandlers {
    private static final Random RANDOM = new Random();
    private static final double HOSTILE_DETECTION_RANGE = 32.0;
    private static final boolean SHOW_INVISIBLE_MOBS = true;
    private static final boolean SHOW_NEUTRAL_MOBS = false;
    private static final TagKey<EntityType<?>> BOSSES_TAG = TagKey.create(
        Registries.ENTITY_TYPE, new ResourceLocation("kaleidoscope_world_liquor", "bosses")
    );
    // 原 getPersistentData() 用的两个标记键：保留官方常量名作对照；
    // Fabric 侧改用下方 UUID 集合承载（无 Entity#getPersistentData），故本常量仅存档参照。
    private static final String BEHEADED_MARKER = "kaleidoscope_world_liquor_beheaded";
    private static final String CREATIVE_FLIGHT_MARKER = "kaleidoscope_world_liquor_creative_flight";
    private static final TagKey<Block> CROPS_TAG = BlockTags.CROPS;
    private static final TagKey<Block> ORES_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores"));private static final int TREASURE_SENSE_RADIUS = 24;

    /**
     * 斩首标记。原 Forge 用 {@code Entity#getPersistentData()}（随实体 NBT 落盘），
     * Fabric 无该 API，改为进程内 UUID 集合：标记只需存活到该实体下一次死亡掉落，
     * 期间实体不会跨存档重启，可接受的等价降级（见批次报告）。
     */
    private static final Set<UUID> BEHEADED_ENTITIES = new HashSet<>();

    /**
     * 创造模式飞行授予标记（原 CREATIVE_FLIGHT_MARKER 写进 Player#getPersistentData 的布尔值）。
     * Fabric 无 getPersistentData → 进程内 UUID 集合。重启后集合清空而 abilities.mayfly
     * 是随玩家 NBT 落盘的，故在“效果仍在 + 已有 mayfly”分支补写标记（见 updateCreativeFlight），
     * 避免重启后标记丢失导致飞行权限永不回收。
     */
    private static final Set<UUID> CREATIVE_FLIGHT_ENTITIES = new HashSet<>();

    /**
     * 掉落捕获栈（线程本地）。原 Forge 在 dropAllDeathLoot 内开 captureDrops，
     * spawnAtLocation 生成的 ItemEntity 进列表、随后统一过 LivingDropsEvent；
     * Fabric 无该机制，由 mixin 在 dropAllDeathLoot 前后开关本栈，Entity#spawnAtLocation
     * 的 mixin 在窗口内把 ItemEntity 交给 {@link #captureDrop}。
     */
    private static final ThreadLocal<Deque<List<ItemEntity>>> DROP_CAPTURE = new ThreadLocal<>();

    public EventHandlers() {
    }

    /**
     * 原 Forge 总线监听器的显式注册入口（由主类 KaleidoscopeWorldLiquor#onInitialize 调用）。
     */
    public static void register() {
        // LivingAttackEvent → ALLOW_DAMAGE（服务端、伤害生效前；返回 false 等价 setCanceled(true)）
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(EventHandlers::onLivingAttack);
        // BreakEvent → PlayerBlockBreakEvents.BEFORE（服务端、方块破坏前）
        PlayerBlockBreakEvents.BEFORE.register(EventHandlers::onBlockBreak);
        // PlayerTickEvent(Phase.END) → END_WORLD_TICK（逐世界遍历玩家；官方方法体整体在 !isClientSide 内）
        ServerTickEvents.END_WORLD_TICK.register(EventHandlers::onWorldTick);
    }

    // ===== 掉落捕获（由 mixin/LivingEntityDropsMixin、mixin/EntitySpawnAtLocationMixin 回调） =====

    /** mixins/LivingEntityDropsMixin 在 LivingEntity#dropAllDeathLoot HEAD 调用：压入本层捕获列表。 */
    public static void beginDropCapture() {
        Deque<List<ItemEntity>> stack = DROP_CAPTURE.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            DROP_CAPTURE.set(stack);
        }
        stack.push(new ArrayList<>());
    }

    /** mixins/LivingEntityDropsMixin 在 LivingEntity#dropAllDeathLoot RETURN 调用：弹出本层捕获列表（关闭窗口）。 */
    public static List<ItemEntity> endDropCapture() {
        Deque<List<ItemEntity>> stack = DROP_CAPTURE.get();
        if (stack == null || stack.isEmpty()) {
            return new ArrayList<>();
        }
        return stack.pop();
    }

    /** mixins/EntitySpawnAtLocationMixin 在 Entity#spawnAtLocation RETURN 调用，仅捕获窗口内生效。 */
    public static void captureDrop(ItemEntity itemEntity) {
        Deque<List<ItemEntity>> stack = DROP_CAPTURE.get();
        if (stack != null && !stack.isEmpty() && itemEntity != null) {
            stack.peek().add(itemEntity);
        }
    }

    /**
     * 原 onLivingDrops(LivingDropsEvent)（淘金热：击杀者持藏宝指南时按概率复制全部掉落）。
     * <p>
     * Forge 侧是把复制体加进 event.getDrops() 由 Forge 统一 spawn；本侧捕获到的
     * 原始掉落已经进世界了，所以这里直接把复制体 spawn 出来（净效果一致：原物 + 同坐标复制体）。
     */
    public static void onLivingDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops) {
        if (!entity.level().isClientSide()) {
            if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.TREASURE_GUIDE_EFFECT)) {
                int amplifier = Objects.requireNonNull(attacker.getEffect(ModEffects.TREASURE_GUIDE_EFFECT)).getAmplifier();
                double totalChance = Math.min(0.15 + amplifier * 0.05, 1.0);
                if (RANDOM.nextDouble() < totalChance) {
                    for (ItemEntity itemEntity : new ArrayList<>(drops)) {
                        ItemStack extraStack = itemEntity.getItem().copy();
                        ItemEntity extraEntity = new ItemEntity(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), extraStack);
                        extraEntity.setDefaultPickUpDelay();
                        itemEntity.level().addFreshEntity(extraEntity);
                    }
                }
            }
        }
    }

    /**
     * 原 onBlockBreak(BreakEvent)（Forge BlockEvent.BreakEvent）→ PlayerBlockBreakEvents.BEFORE。
     * Fabric 该回调返回 false 会取消破坏，官方监听器从不取消 → 恒返回 true。
     * 原 event.isCanceled() 守卫在 BEFORE 回调里没有对应语义（回调按注册序执行、被前序取消则不再回调）。
     */
    public static boolean onBlockBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (player != null && !player.level().isClientSide()) {
            if (player.hasEffect(ModEffects.TREASURE_GUIDE_EFFECT)) {
                int amplifier = Objects.requireNonNull(player.getEffect(ModEffects.TREASURE_GUIDE_EFFECT)).getAmplifier();
                // 官方 1.1.12：三档概率统一为 0.15+amp×0.05 封顶 1.0（矿物基础 0.20→0.15）
                boolean isCrop = state.is(CROPS_TAG) || state.getBlock() instanceof CropBlock;
                boolean isOre = state.is(ORES_TAG);
                if (!isCrop && !isOre) {
                    return true;
                }

                double totalChance = Math.min(0.15 + amplifier * 0.05, 1.0);
                if (RANDOM.nextDouble() < totalChance) {
                    ServerLevel serverLevel = (ServerLevel) level;

                    for (ItemStack drop : Block.getDrops(state, serverLevel, pos, blockEntity, player, player.getMainHandItem())) {
                        if (!drop.isEmpty()) {
                            ItemEntity itemEntity = new ItemEntity(
                                serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop.copy()
                            );
                            itemEntity.setDefaultPickUpDelay();
                            serverLevel.addFreshEntity(itemEntity);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 原 onLivingAttack(LivingAttackEvent) → ServerLivingEntityEvents.ALLOW_DAMAGE。
     * <p>
     * ALLOW_DAMAGE 注入在 LivingEntity#hurt 的 isSleeping 判定前（isInvulnerableTo/
     * 客户端侧/已死亡判定之后），返回 false 即取消本次伤害——与 Forge setCanceled 等价。
     * 官方 handler 里的“取消 + 直接结算 10000 伤害”重入结构原样保留：
     * 内层 target.hurt(...) 会再次触发 ALLOW_DAMAGE 并重新掷斩首概率（与官方
     * LivingAttackEvent 重入行为一致，逐层 4% 概率自然收敛）。
     *
     * @return true 允许伤害；false 取消本次伤害（斩首触发时）
     */
    public static boolean onLivingAttack(LivingEntity target, DamageSource source, float amount) {
        if (ModEffects.ELBOW_STRIKE != null
            && source.getDirectEntity() instanceof LivingEntity attacker
            && attacker.hasEffect(ModEffects.ELBOW_STRIKE)) {
            // 原实现无 isClientSide 守卫（Forge 事件双侧触发）；ALLOW_DAMAGE 仅服务端，
            // playSound 由服务端广播，攻击者与附近玩家仍会听到，音效结果等价。
            attacker.playSound(ModSounds.ICE_TEA_EAT, 0.6F, 1.0F);
            // 官方（Forge 1.20.1）对 Player.attack 打了补丁：击退计数 i 初始化为
            // ATTACK_KNOCKBACK 属性值（Forge 注释原话 "Initialize this value to the attack
            // knockback attribute of the player"），强度 = 0.5 × i（原版公式）。原版 Fabric
            // 无此补丁、玩家攻击不读该属性，故这里按 Forge 同款公式直接结算：
            // SMCEffect 给玩家 +9 属性 → 强度 0.5 × 9 = 4.5，与官方一致。
            if (!target.level().isClientSide() && target != attacker) {
                double knockbackAttr = attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
                if (knockbackAttr > 0.0D) {
                    float yawRad = attacker.getYRot() * ((float) Math.PI / 180.0F);
                    target.knockback(0.5D * knockbackAttr, (double) Mth.sin(yawRad), (double) (-Mth.cos(yawRad)));
                }
            }
        }

        if (!target.level().isClientSide()) {
            if (source.getDirectEntity() instanceof LivingEntity attacker
                && attacker.hasEffect(ModEffects.BEHEADING_EFFECT)
                && !target.getType().is(BOSSES_TAG)
                && target.isAlive()) {
                int amplifier = Objects.requireNonNull(attacker.getEffect(ModEffects.BEHEADING_EFFECT)).getAmplifier();
                double baseChance = 0.04;
                double extraChance = amplifier * 0.03;
                double totalChance = baseChance + extraChance;
                if (RANDOM.nextDouble() < totalChance) {
                    // 原 target.getPersistentData().putBoolean(BEHEADING_MARKER, true)
                    BEHEADED_ENTITIES.add(target.getUUID());
                    float safeDamage = 10000.0F;
                    float healthBefore = target.getHealth();
                    if (attacker instanceof Player player) {
                        target.hurt(attacker.damageSources().playerAttack(player), safeDamage);
                    } else {
                        target.hurt(attacker.damageSources().mobAttack(attacker), safeDamage);
                    }

                    float healthAfter = target.getHealth();
                    if (Float.isNaN(healthAfter) || healthAfter > healthBefore) {
                        target.setHealth(1.0F);
                        if (attacker instanceof Player player) {
                            target.hurt(attacker.damageSources().playerAttack(player), 2.0F);
                        } else {
                            target.hurt(attacker.damageSources().mobAttack(attacker), 2.0F);
                        }

                        if (target.isAlive()) {
                            target.setHealth(0.0F);
                            target.die(source);
                        }
                    }
                    return false; // 原 event.setCanceled(true)：原始攻击伤害不生效
                }
            }
        }
        return true;
    }

    /**
     * 原 onLivingHurt(LivingHurtEvent)（破势 ground_crit：未触发原版暴击时按概率 ×1.5 并播暴击表现）。
     * 改伤害数值在 Fabric 无事件可用，由 mixin/LivingEntityDamageMixin 在
     * LivingEntity#actuallyHurt 的 getDamageAfterMagicAbsorb 之后回调本方法。
     */
    public static float onLivingHurt(LivingEntity target, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof LivingEntity attacker
            && attacker instanceof Player player
            && player.hasEffect(ModEffects.GROUND_CRIT_EFFECT)
            && isMeleeAttack(source)) {
            int critAmplifier = Objects.requireNonNull(player.getEffect(ModEffects.GROUND_CRIT_EFFECT)).getAmplifier();
            double baseCritChance = 0.2;
            double extraCritChance = critAmplifier * 0.1;
            double totalCritChance = baseCritChance + extraCritChance;
            if (!isVanillaCrit(player) && RANDOM.nextDouble() < totalCritChance) {
                float critDamage = amount * 1.5F;
                player.crit(target);
                return critDamage;
            }
        }
        return amount;
    }

    private static boolean isMeleeAttack(DamageSource source) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        } else {
            return source.getEntity() != null && source.getDirectEntity() != null ? source.getDirectEntity() == source.getEntity() : false;
        }
    }

    private static boolean isVanillaCrit(Player player) {
        return !player.onGround()
            && !player.onClimbable()
            && !player.isInWater()
            && !player.hasEffect(MobEffects.BLINDNESS)
            && !player.isPassenger()
            && player.getDeltaMovement().y < 0.0;
    }

    /**
     * 原 onLivingDropsBeheading(LivingDropsEvent)（斩首：死亡掉落里补生物头颅）。
     * Forge 侧把头颅加进 event.getDrops() 由 Forge 统一 spawn；
     * 本侧捕获到的掉落已在世界里，直接 spawn 头颅 ItemEntity（净效果一致）。
     */
    public static void onLivingDropsBeheading(LivingEntity entity, Collection<ItemEntity> drops) {
        if (!entity.isAlive()
            && !entity.level().isClientSide()
            && BEHEADED_ENTITIES.remove(entity.getUUID())) {
            addBeheadingHeadIfMissing(entity, drops);
        }
    }

    private static void addBeheadingHeadIfMissing(LivingEntity entity, Collection<ItemEntity> drops) {
        if (!entity.level().isClientSide()) {
            ItemStack beheadingHead = getEntityHead(entity);
            if (!beheadingHead.isEmpty()) {
                boolean alreadyHasHead = drops.stream().anyMatch(itemEntity -> ItemStack.isSameItemSameTags(itemEntity.getItem(), beheadingHead));
                if (!alreadyHasHead) {
                    ItemEntity headEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), beheadingHead);
                    headEntity.setDefaultPickUpDelay();
                    entity.level().addFreshEntity(headEntity);
                }
            }
        }
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
            CompoundTag tag = new CompoundTag();
            tag.putString("SkullOwner", player.getGameProfile().getName());
            playerHead.setTag(tag);
            return playerHead;
        } else {
            // 官方 1.1.12：通用命名匹配（<ns>:<path>_head/_skull、skull_/head_ 前缀、dead_、_item），
            // 支持模组生物的头颅物品；对齐官方“若该生物存在头颅物品则必定掉落”的描述。
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (entityId != null) {
                String ns = entityId.getNamespace();
                String path = entityId.getPath();
                String[][] patterns = new String[][]{{path + "_head", path + "_skull"}, {"skull_" + path, "head_" + path}, {"dead_" + path}, {path + "_item"}};

                for (String[] group : patterns) {
                    for (String suffix : group) {
                        ResourceLocation headId = new ResourceLocation(ns, suffix);
                        Item item = BuiltInRegistries.ITEM.get(headId);
                        if (item != null && item != Items.AIR) {
                            return new ItemStack(item);
                        }
                    }
                }
            }

            return ItemStack.EMPTY;
        }
    }

    /**
     * 原 onPlayerTick(PlayerTickEvent) 的世界侧入口。
     * Fabric 无 PlayerTickEvent：END_WORLD_TICK 每世界回调一次，这里遍历该世界玩家。
     * 官方 handler 只在 Phase.END 且 !isClientSide 下执行（方法体内无任何客户端分支），
     * END_WORLD_TICK 天然满足两端（仅服务端世界、每 tick 一次）。
     */
    public static void onWorldTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            onPlayerTick(player);
        }
    }

    private static void onPlayerTick(Player player) {
        if (!player.level().isClientSide()) {
            updateCreativeFlight(player);
            if (player.hasEffect(ModEffects.FROST_WALKER_EFFECT)) {
                int amplifier = Objects.requireNonNull(player.getEffect(ModEffects.FROST_WALKER_EFFECT)).getAmplifier();
                FrostWalkerEnchantment.onEntityMoved(player, player.level(), player.blockPosition(), amplifier + 1);
            }

            // 官方 1.1.12：春野之息（骨粉扩散）移除，改为宝藏感知每 20tick 服务端扫描并下发目标
            if (player.hasEffect(ModEffects.TREASURE_SENSE_EFFECT) && player.tickCount % 20 == 0) {
                syncTreasureSenseTargets(player);
            }
        }
    }

    /** 官方 1.1.12：服务端扫描 5×5 区块内 24 格有战利品表的容器/饰纹陶罐（1.20.1 官方无陶罐项）+ 近身容器矿车，打包下发。 */
    private static void syncTreasureSenseTargets(Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.level() instanceof ServerLevel level) {
            ArrayList<BlockPos> targets = new ArrayList<>();
            BlockPos playerPos = player.blockPosition();
            ChunkPos playerChunkPos = new ChunkPos(playerPos);
            byte chunkRadius = 2;
            double radiusSq = 576.0;

            for (int cx = playerChunkPos.x - chunkRadius; cx <= playerChunkPos.x + chunkRadius; cx++) {
                for (int cz = playerChunkPos.z - chunkRadius; cz <= playerChunkPos.z + chunkRadius; cz++) {
                    LevelChunk chunk = level.getChunk(cx, cz);
                    if (chunk != null && !chunk.isEmpty()) {
                        for (BlockEntity be : chunk.getBlockEntities().values()) {
                            if (!be.isRemoved()) {
                                BlockPos pos = be.getBlockPos();
                                if (!(pos.distSqr(playerPos) > radiusSq)) {
                                    if (be instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntityAccessor)be).kwl$getLootTable() != null) {
                                        targets.add(pos.immutable());
                                    } else if (be instanceof BrushableBlockEntity && ((BrushableBlockEntityAccessor)be).kwl$getLootTable() != null) {
                                        targets.add(pos.immutable());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            List<Integer> lootMinecartIds = new ArrayList<>();

            for (AbstractMinecartContainer minecart : level.getEntitiesOfClass(
                AbstractMinecartContainer.class, player.getBoundingBox().inflate(TREASURE_SENSE_RADIUS), minecartx -> !minecartx.isRemoved() && minecartx.getLootTable() != null
            )) {
                lootMinecartIds.add(minecart.getId());
            }

            NetworkHandler.send(serverPlayer, new TreasureSensePacket(targets, lootMinecartIds));
        }
    }

    private static void updateCreativeFlight(Player player) {
        Abilities abilities = player.getAbilities();
        boolean grantedByThisMod = CREATIVE_FLIGHT_ENTITIES.contains(player.getUUID());
        if (player.hasEffect(ModEffects.CREATIVE_FLIGHT)) {
            if (!player.isCreative() && !player.isSpectator()) {
                if (!abilities.mayfly) {
                    abilities.mayfly = true;
                    player.onUpdateAbilities();
                }

                // 原实现只在“首次授予”时写 persistentData.get/putBoolean(CREATIVE_FLIGHT_MARKER)（NBT 落盘、重启不丢）；
                // Fabric 侧集合重启清空，而 mayfly 已随玩家存档保存 → 非创造/旁观且持有效果时每 tick 补写标记，
                // 保证效果结束（哪怕跨过一次重启）后仍能识别“飞行是本模组授予的”并按官方语义回收。
                CREATIVE_FLIGHT_ENTITIES.add(player.getUUID());
            }
        } else if (grantedByThisMod) {
            boolean defaultMayfly = player.isCreative() || player.isSpectator();
            if (!defaultMayfly) {
                abilities.flying = false;
            }

            abilities.mayfly = defaultMayfly;
            player.onUpdateAbilities();
            player.fallDistance = 0.0F;
            CREATIVE_FLIGHT_ENTITIES.remove(player.getUUID());
        }
    }

    /**
     * 原 @EventBusSubscriber(value = Dist.CLIENT) 的 ClientEventHandler（RenderTickEvent.END）。
     * Fabric 无逐帧必要性：本逻辑只按效果刷新附近敌对生物的发光标记（标记是持久状态），
     * 改挂 ClientTickEvents.END_CLIENT_TICK（每 tick 一次、同帧渲染前执行）语义等价；
     * 官方 phase == Phase.END 由“END_TICK”天然满足，isPaused 守卫原样保留。
     * <p>
     * 嵌套类加 @Environment(CLIENT)：仅供客户端入口点调用，服务端不会加载本类。
     */
    @Environment(EnvType.CLIENT)
    public static class ClientEventHandler {
        public ClientEventHandler() {
        }

        public static void register() {
            ClientTickEvents.END_CLIENT_TICK.register(ClientEventHandler::onRenderTick);
        }

        public static void onRenderTick(Minecraft mc) {
            if (mc.player != null && mc.level != null && !mc.isPaused()) {
                boolean hasEffect = mc.player.hasEffect(ModEffects.HOSTILE_DETECTION_EFFECT);
                double rangeSq = 1024.0;
                List<Mob> mobs = mc.level.getEntitiesOfClass(Mob.class, mc.player.getBoundingBox().inflate(37.0), mobx -> mobx.isAlive());

                for (Mob mob : mobs) {
                    IGlowingEntity glowingMob = (IGlowingEntity) mob;
                    boolean isHostile = mob instanceof Enemy;
                    boolean shouldGlow = hasEffect && isHostile && mc.player.distanceToSqr(mob) <= rangeSq;
                    boolean isCurrentlyModGlowing = glowingMob.isModGlowing();
                    if (isCurrentlyModGlowing != shouldGlow) {
                        glowingMob.setGlowing(shouldGlow);
                    }
                }

                if (!hasEffect) {
                    for (Mob mobx : mobs) {
                        IGlowingEntity glowingMob = (IGlowingEntity) mobx;
                        if (glowingMob.isModGlowing()) {
                            glowingMob.setGlowing(false);
                        }
                    }
                }
            }
        }
    }
}
