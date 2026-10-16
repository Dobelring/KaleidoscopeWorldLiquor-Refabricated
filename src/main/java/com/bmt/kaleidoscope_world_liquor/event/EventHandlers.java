package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.api.event.PlayerTickEvents;
import com.bmt.kaleidoscope_world_liquor.effect.DoubleDamageEffect;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.BrushableBlockEntityAccessor;
import com.bmt.kaleidoscope_world_liquor.network.NetworkHandler;
import com.bmt.kaleidoscope_world_liquor.network.TreasureSensePayload;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class EventHandlers {
   private static final Random RANDOM = new Random();
   private static final double HOSTILE_DETECTION_RANGE = 32.0;
   private static final boolean SHOW_INVISIBLE_MOBS = true;
   private static final boolean SHOW_NEUTRAL_MOBS = false;
   private static final TagKey<EntityType<?>> BOSSES_TAG = TagKey.create(
      BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "bosses")
   );
   private static final TagKey<Block> CROPS_TAG = BlockTags.CROPS;private static final int TREASURE_SENSE_RADIUS = 24;
   private static final TagKey<Block> ORES_TAG = TagKey.create(
      BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath("c", "ores")
   );
   private static final Set<UUID> BEHEADED_ENTITIES = new HashSet<>();
   private static final Set<UUID> BEHEADING_PROCESSING = new HashSet<>();
   private static final Set<UUID> CRIT_PROCESSING = new HashSet<>();
   private static final Set<UUID> DOUBLE_DAMAGE_PROCESSING = new HashSet<>();

   /**
    * 嗨棒飞行的跨模组共存状态（与黎明之星等其他飞行模组同装时互不打断）。
    * own=本模组把 mayfly 从关翻到开过（所有权唯一：另一模组只会在 mayfly 为关时翻转）；
    * pendingSince=两段式回收窗口起点——第一段只撤 mayfly 保留 flying（其他模组的授予分支
    * 会在 ≤1 tick 内重新打开 mayfly），下一 tick 确认无人接手才完成回收；
    * lastFlyingTick=近期飞行观察，重授予时同步还原 flying，封死“回收完成与对方重授予同 tick”的竞态。
    * FLIGHT_TICK 用 JVM 内静态计数而非 server tick：跨世界重载单调，配合登录重建状态避免挂死。
    */
   private static final Map<UUID, FlightClaim> FLIGHT_CLAIMS = new HashMap<>();
   private static int FLIGHT_TICK;

   private static final class FlightClaim {
      boolean own;
      int pendingSince = -1;
      int lastFlyingTick = -1000;
   }

   public static void register() {
      BrewAcceleratorEventHandler.register();
      DamageEvents.register();
      MusicDiscEvents.register();
      DollInteractionEvents.register();
      ServerLivingEntityEvents.ALLOW_DAMAGE.register(EventHandlers::onLivingIncomingDamage);
      ServerLivingEntityEvents.AFTER_DAMAGE.register(EventHandlers::onLivingDamageAfter);
      ServerLivingEntityEvents.AFTER_DEATH.register(EventHandlers::onLivingDeath);
      PlayerBlockBreakEvents.AFTER.register(EventHandlers::onBlockBreak);
      PlayerTickEvents.END.register(EventHandlers::onPlayerTick);
      // 飞行认领：登录按存档 mayfly/flying 对账（孤儿飞行由两段式回收兜底），断开清理
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
         (handler, sender, server) -> reconcileFlightClaim(handler.player));
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
         (handler, server) -> FLIGHT_CLAIMS.remove(handler.player.getUUID()));
      // 飞行状态机的窗口单位：每服务器 tick 一次（本分支玩家 tick 走自定义 PlayerTickEvents，
      // 不能在玩家事件里自增，否则多在线玩家会把 +1/+2 评估窗口跳过导致两段式退化成一步清除）
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> FLIGHT_TICK++);
   }

   public static void registerClient() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> EventHandlers.onClientTick());
   }

   private static boolean onLivingIncomingDamage(LivingEntity target, DamageSource source, float amount) {
      if (target.level().isClientSide || BEHEADING_PROCESSING.contains(target.getUUID())) {
         return true;
      }

      // 重斩（double_damage）：攻击者带效果时按 20%+20%/级 概率双倍伤害——
      // Fabric ALLOW_DAMAGE 无改额语义，走 cancel+按新额二次结算（龙舌兰同范式）；
      // applyDoubleDamage 内含暴击音/粒子，返回翻倍后的伤害额。
      if (source.getEntity() instanceof LivingEntity attacker
         && attacker.hasEffect(ModEffects.DOUBLE_DAMAGE_EFFECT)
         && !DOUBLE_DAMAGE_PROCESSING.contains(target.getUUID())) {
         DOUBLE_DAMAGE_PROCESSING.add(target.getUUID());
         try {
            float doubled = DoubleDamageEffect.applyDoubleDamage(attacker, source, amount);
            if (doubled > amount) {
               target.hurt(source, doubled);
               return false;
            }
         } finally {
            DOUBLE_DAMAGE_PROCESSING.remove(target.getUUID());
         }
      }

      if (source.getDirectEntity() instanceof LivingEntity attacker) {
         if (attacker.hasEffect(ModEffects.BEHEADING_EFFECT) && !target.getType().is(BOSSES_TAG) && target.isAlive()) {
            int amplifier = attacker.getEffect(ModEffects.BEHEADING_EFFECT).getAmplifier();
            double baseChance = 0.04;
            double extraChance = amplifier * 0.03;
            double totalChance = baseChance + extraChance;
            if (RANDOM.nextDouble() < totalChance) {
               BEHEADED_ENTITIES.add(target.getUUID());
               BEHEADING_PROCESSING.add(target.getUUID());
               float healthBefore = target.getHealth();
               DamageSource killSource = attacker instanceof Player player
                  ? target.damageSources().playerAttack(player)
                  : target.damageSources().mobAttack(attacker);
               target.hurt(killSource, 10000.0F);
               float healthAfter = target.getHealth();
               if (Float.isNaN(healthAfter) || healthAfter >= healthBefore) {
                  target.setHealth(1.0F);
                  target.hurt(killSource, 2.0F);
                  if (target.isAlive()) {
                     target.setHealth(0.0F);
                     target.die(killSource);
                  }
               }

               BEHEADING_PROCESSING.remove(target.getUUID());
               return false;
            }
         }
      }
      return true;
   }

   private static void onLivingDamageAfter(LivingEntity target, DamageSource source, float dealt, float originalAmount, boolean blockedByShield) {
      if (target.level().isClientSide || CRIT_PROCESSING.contains(target.getUUID())) {
         return;
      }

      if (source.getDirectEntity() instanceof LivingEntity attacker
         && attacker instanceof Player player
         && player.hasEffect(ModEffects.GROUND_CRIT_EFFECT)
         && isMeleeAttack(source)) {
         int critAmplifier = player.getEffect(ModEffects.GROUND_CRIT_EFFECT).getAmplifier();
         double baseCritChance = 0.2;
         double extraCritChance = critAmplifier * 0.1;
         double totalCritChance = baseCritChance + extraCritChance;
         if (!isVanillaCrit(player) && RANDOM.nextDouble() < totalCritChance) {
            CRIT_PROCESSING.add(target.getUUID());
            try {
               player.crit(target);
               if (target.isAlive()) {
                  target.hurt(target.damageSources().playerAttack(player), originalAmount * 0.5F);
               }
            } finally {
               CRIT_PROCESSING.remove(target.getUUID());
            }
         }
      }
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

   private static void onLivingDeath(LivingEntity entity, DamageSource source) {
      if (entity.level().isClientSide) {
         return;
      }

      // 宝藏指引：复制掉落物
      if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.TREASURE_GUIDE_EFFECT)) {
         int amplifier = attacker.getEffect(ModEffects.TREASURE_GUIDE_EFFECT).getAmplifier();
         double totalChance = Math.min(0.15 + amplifier * 0.05, 1.0);
         if (RANDOM.nextDouble() < totalChance) {
            List<ItemEntity> drops = entity.level().getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(2.0), itemEntity -> itemEntity.isAlive());
            for (ItemEntity itemEntity : new ArrayList<>(drops)) {
               ItemStack extraStack = itemEntity.getItem().copy();
               ItemEntity extraEntity = new ItemEntity(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), extraStack);
               extraEntity.setDefaultPickUpDelay();
               entity.level().addFreshEntity(extraEntity);
            }
         }
      }

      // 斩首头颅掉落
      if (BEHEADED_ENTITIES.remove(entity.getUUID())) {
         List<ItemEntity> drops = entity.level().getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(2.0), itemEntity -> itemEntity.isAlive());
         addBeheadingHeadIfMissing(entity, drops);
      }
   }

   private static void addBeheadingHeadIfMissing(LivingEntity entity, Collection<ItemEntity> drops) {
      ItemStack beheadingHead = getEntityHead(entity);
      if (!beheadingHead.isEmpty()) {
         boolean alreadyHasHead = drops.stream().anyMatch(itemEntity -> ItemStack.isSameItemSameComponents(itemEntity.getItem(), beheadingHead));
         if (!alreadyHasHead) {
            ItemEntity headEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), beheadingHead);
            headEntity.setDefaultPickUpDelay();
            entity.level().addFreshEntity(headEntity);
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
         playerHead.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
         return playerHead;
      } else {
         // 官方 1.1.11：通用命名匹配（<ns>:<path>_head/_skull、skull_/head_ 前缀、dead_、_item）
         ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
         if (entityId != null) {
            String ns = entityId.getNamespace();
            String path = entityId.getPath();
            String[][] patterns = new String[][]{{path + "_head", path + "_skull"}, {"skull_" + path, "head_" + path}, {"dead_" + path}, {path + "_item"}};

            for (String[] group : patterns) {
               for (String suffix : group) {
                  ResourceLocation headId = ResourceLocation.fromNamespaceAndPath(ns, suffix);
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

   private static void onBlockBreak(net.minecraft.world.level.Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
      if (world.isClientSide || player == null) {
         return;
      }

      if (player.hasEffect(ModEffects.TREASURE_GUIDE_EFFECT)) {
         int amplifier = player.getEffect(ModEffects.TREASURE_GUIDE_EFFECT).getAmplifier();
         boolean isCrop = state.is(CROPS_TAG) || state.getBlock() instanceof CropBlock;
         boolean isOre = state.is(ORES_TAG);
         if (!isCrop && !isOre) {
            return;
         }

         double totalChance = Math.min(0.15 + amplifier * 0.05, 1.0);
         if (RANDOM.nextDouble() < totalChance) {
            ServerLevel level = (ServerLevel)world;

            for (ItemStack drop : Block.getDrops(state, level, pos, blockEntity, player, player.getMainHandItem())) {
               if (!drop.isEmpty()) {
                  ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop.copy());
                  itemEntity.setDefaultPickUpDelay();
                  level.addFreshEntity(itemEntity);
               }
            }
         }
      }
   }

   private static void onPlayerTick(Player player) {
      if (player.level().isClientSide) {
         return;
      }

      if (player.hasEffect(ModEffects.FROST_WALKER_EFFECT)) {
         int amplifier = player.getEffect(ModEffects.FROST_WALKER_EFFECT).getAmplifier();
         freezeWater(player, (ServerLevel)player.level(), player.blockPosition(), amplifier + 1);
      }

      // 官方 1.1.11：春野之息移除，改为宝藏感知每 20tick 服务端扫描下发
      if (player.hasEffect(ModEffects.TREASURE_SENSE_EFFECT) && player.tickCount % 20 == 0) {
         syncTreasureSenseTargets(player);
      }

      updateCreativeFlight(player);
   }

   /** 官方 1.1.11：服务端扫描 5×5 区块 24 格内有战利品表的容器/饰纹陶罐/陶罐 + 近身容器矿车，打包下发。 */
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
                           if (be instanceof RandomizableContainerBlockEntity container && container.getLootTable() != null) {
                              targets.add(pos.immutable());
                           } else if (be instanceof BrushableBlockEntity && ((BrushableBlockEntityAccessor)be).kwl$getLootTable() != null) {
                              targets.add(pos.immutable());
                           } else if (be instanceof DecoratedPotBlockEntity pot && pot.getLootTable() != null) {
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

         NetworkHandler.send(serverPlayer, new TreasureSensePayload(targets, lootMinecartIds));
      }
   }

   /**
    * 嗨棒（creative_flight）：授予只补 mayfly 并登记翻转所有权；回收走两段式（见 {@link FlightClaim}）。
    * 官方 1.1.9 回收侧是无条件清 mayfly+flying，会把黎明之星等其他模组授予的飞行一并打断
    * （跨模组冲突根因）；两段式回收保证只回收“本模组翻转打开、且无人接手”的飞行。
    * 已验证原版在 mayfly=false 且 flying=true 时不做强制停飞（1.21.1 反编译核对），此逻辑勿改回一步清除。
    */
   private static void updateCreativeFlight(Player player) {
      Abilities abilities = player.getAbilities();
      FlightClaim claim = FLIGHT_CLAIMS.computeIfAbsent(player.getUUID(), k -> new FlightClaim());
      if (player.isCreative() || player.isSpectator()) {
         claim.own = false;
         claim.pendingSince = -1;
         return;
      }
      int tick = FLIGHT_TICK;
      boolean source = player.hasEffect(ModEffects.CREATIVE_FLIGHT);

      if (abilities.flying) {
         claim.lastFlyingTick = tick;
      }
      // 来源回归（效果重新挂上）→ 取消进行中的回收
      if (claim.pendingSince >= 0 && source) {
         claim.pendingSince = -1;
      }
      // 两段式第一段：只撤 mayfly，flying 原样保留（其他模组的授予分支此刻仍可重新打开 mayfly）
      if (claim.own && !source && claim.pendingSince < 0) {
         if (abilities.mayfly) {
            abilities.mayfly = false;
            player.onUpdateAbilities();
         }
         claim.pendingSince = tick;
      }
      // 回收评估：启动当 tick 不评估（给其他模组一次完整的重授予机会）
      if (claim.pendingSince >= 0 && tick >= claim.pendingSince + 1) {
         if (abilities.mayfly) {
            // 有人在窗口内重新打开 → 所有权移交，放弃回收（flying 全程未动，无缝）
            claim.pendingSince = -1;
            claim.own = false;
         } else if (tick >= claim.pendingSince + 2) {
            // 连续两窗无人接手 → 完成回收
            abilities.flying = false;
            abilities.mayfly = false;
            player.onUpdateAbilities();
            player.fallDistance = 0.0F;
            claim.pendingSince = -1;
            claim.own = false;
         }
      }
      // 授予：效果在场但 mayfly 为关 → 翻转并认领；近期（≤2 tick）观察到在飞则同步还原 flying，
      // 封死“他方刚完成回收、本方同 tick 重授予”导致的 1 tick 掉飞竞态
      if (source && !abilities.mayfly) {
         abilities.mayfly = true;
         claim.own = true;
         if (!abilities.flying && tick - claim.lastFlyingTick <= 2) {
            abilities.flying = true;
         }
         player.onUpdateAbilities();
      }
   }

   /** 登录对账：存档里 mayfly/flying 已为开、或嗨棒在身时认领为本模组所有权（可能是也可能不是真所有者，
    *  误认领由两段式回收的窗口协商自行化解）；两段式回收同时兜底跨重启遗留的孤儿飞行。 */
   private static void reconcileFlightClaim(ServerPlayer player) {
      Abilities abilities = player.getAbilities();
      FlightClaim claim = new FlightClaim();
      if (!player.isCreative() && !player.isSpectator()
         && (abilities.mayfly || abilities.flying || player.hasEffect(ModEffects.CREATIVE_FLIGHT))) {
         claim.own = true;
      }
      FLIGHT_CLAIMS.put(player.getUUID(), claim);
   }

   private static void freezeWater(LivingEntity entity, ServerLevel world, BlockPos centerPos, int amplifier) {
      if (entity.onGround()) {
         int radius = 2 + amplifier;
         BlockState frostedIceState = net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState();

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + z * z <= radius * radius) {
                  BlockPos pos = centerPos.offset(x, -1, z);
                  BlockState state = world.getBlockState(pos);
                  if (state.is(net.minecraft.world.level.block.Blocks.WATER)
                     && state.getFluidState().is(Fluids.WATER)
                     && state.getFluidState().isSource()
                     && world.getBlockState(pos.above()).isAir()) {
                     world.setBlockAndUpdate(pos, frostedIceState);
                     world.scheduleTick(pos, frostedIceState.getBlock(), 60);
                  }
               }
            }
         }
      }
   }

   private static void onClientTick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && !mc.isPaused()) {
         boolean hasEffect = mc.player.hasEffect(ModEffects.HOSTILE_DETECTION_EFFECT);
         double rangeSq = 1024.0;
         List<Mob> mobs = mc.level.getEntitiesOfClass(Mob.class, mc.player.getBoundingBox().inflate(37.0), mobx -> mobx.isAlive());

         for (Mob mob : mobs) {
            IGlowingEntity glowingMob = (IGlowingEntity)mob;
            boolean isHostile = mob instanceof Enemy;
            if (isHostile) {
               double distanceSq = mc.player.distanceToSqr(mob);
               boolean shouldGlow = hasEffect && distanceSq <= rangeSq;
               boolean isCurrentlyModGlowing = glowingMob.isModGlowing();
               if (isCurrentlyModGlowing != shouldGlow) {
                  glowingMob.setGlowing(shouldGlow);
               }
            }
         }

         if (!hasEffect) {
            for (Mob mobx : mobs) {
               IGlowingEntity glowingMob = (IGlowingEntity)mobx;
               if (glowingMob.isModGlowing()) {
                  glowingMob.setGlowing(false);
               }
            }
         }
      }
   }
}
