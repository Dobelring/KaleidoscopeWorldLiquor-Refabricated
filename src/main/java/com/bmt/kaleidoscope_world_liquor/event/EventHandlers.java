package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.api.event.PlayerTickEvents;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class EventHandlers {
   private static final Random RANDOM = new Random();
   private static final double GRAVITY = 0.08;
   private static final double JUMP_POWER = -0.32;
   private static final double HOSTILE_DETECTION_RANGE = 32.0;
   private static final boolean SHOW_INVISIBLE_MOBS = true;
   private static final boolean SHOW_NEUTRAL_MOBS = false;
   private static final TagKey<EntityType<?>> BOSSES_TAG = TagKey.create(
      BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "bosses")
   );
   private static final TagKey<Block> CROPS_TAG = BlockTags.CROPS;
   private static final TagKey<Block> SAPLINGS_TAG = BlockTags.SAPLINGS;
   private static final TagKey<Block> FLOWERS_TAG = BlockTags.FLOWERS;
   private static final TagKey<Block> ORES_TAG = TagKey.create(
      BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath("c", "ores")
   );
   private static final Set<UUID> BEHEADED_ENTITIES = new HashSet<>();
   private static final Set<UUID> BEHEADING_PROCESSING = new HashSet<>();
   private static final Set<UUID> CRIT_PROCESSING = new HashSet<>();

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
   }

   public static void registerClient() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> EventHandlers.onClientTick());
   }

   private static boolean onLivingIncomingDamage(LivingEntity target, DamageSource source, float amount) {
      if (target.level().isClientSide || BEHEADING_PROCESSING.contains(target.getUUID())) {
         return true;
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
         double baseChance = 0.15;
         double extraChance = amplifier * 0.05;
         double totalChance = baseChance + extraChance;
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
      } else if (entity instanceof Creeper) {
         return new ItemStack(Items.CREEPER_HEAD);
      } else if (entity instanceof WitherSkeleton) {
         return new ItemStack(Items.WITHER_SKELETON_SKULL);
      } else if (entity instanceof Piglin) {
         return new ItemStack(Items.PIGLIN_HEAD);
      } else if (entity instanceof Player player) {
         ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
         CompoundTag tag = new CompoundTag();
         tag.putString("SkullOwner", player.getGameProfile().getName());
         playerHead.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
         return playerHead;
      } else {
         return ItemStack.EMPTY;
      }
   }

   private static void onBlockBreak(net.minecraft.world.level.Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
      if (world.isClientSide || player == null) {
         return;
      }

      if (player.hasEffect(ModEffects.TREASURE_GUIDE_EFFECT)) {
         int amplifier = player.getEffect(ModEffects.TREASURE_GUIDE_EFFECT).getAmplifier();
         double baseChance = 0.0;
         if (state.is(CROPS_TAG) || state.getBlock() instanceof CropBlock) {
            baseChance = 0.15;
         } else if (state.is(ORES_TAG)) {
            baseChance = 0.2;
         }

         if (baseChance <= 0.0) {
            return;
         }

         double extraChance = amplifier * 0.05;
         double totalChance = baseChance + extraChance;
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

      if (player.hasEffect(ModEffects.BONEMEAL_SPREADER_EFFECT)) {
         int amplifier = player.getEffect(ModEffects.BONEMEAL_SPREADER_EFFECT).getAmplifier();
         int duration = player.getEffect(ModEffects.BONEMEAL_SPREADER_EFFECT).getDuration();
         if (duration % 20 == 0) {
            spreadBonemealOnGround(player, amplifier);
         }

         if (duration % 40 == 0) {
            spreadBonemealOnPlants(player, amplifier);
         }
      }
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

   private static void spreadBonemealOnGround(Player player, int amplifier) {
      ServerLevel level = (ServerLevel)player.level();
      BlockPos playerPos = player.blockPosition();
      int range = 1 + amplifier;
      int offsetX = RANDOM.nextInt(range * 2 + 1) - range;
      int offsetZ = RANDOM.nextInt(range * 2 + 1) - range;
      BlockPos targetPos = playerPos.offset(offsetX, 0, offsetZ);
      applyBonemealIfGround(level, targetPos, player);
      applyBonemealIfGround(level, targetPos.below(), player);
   }

   private static void spreadBonemealOnPlants(Player player, int amplifier) {
      ServerLevel level = (ServerLevel)player.level();
      BlockPos playerPos = player.blockPosition();
      int range = 1 + amplifier;
      int offsetX = RANDOM.nextInt(range * 2 + 1) - range;
      int offsetZ = RANDOM.nextInt(range * 2 + 1) - range;
      BlockPos targetPos = playerPos.offset(offsetX, 0, offsetZ);
      applyBonemealIfPlant(level, targetPos, player);
      applyBonemealIfPlant(level, targetPos.below(), player);
   }

   private static void applyBonemealIfGround(ServerLevel level, BlockPos pos, Player player) {
      BlockState state = level.getBlockState(pos);
      if (state.getBlock() instanceof BonemealableBlock bonemealableBlock && !isPlantBlock(state)) {
         bonemealableBlock.performBonemeal(level, level.random, pos, state);
      }
   }

   private static void applyBonemealIfPlant(ServerLevel level, BlockPos pos, Player player) {
      BlockState state = level.getBlockState(pos);
      if (state.getBlock() instanceof BonemealableBlock bonemealableBlock && isPlantBlock(state)) {
         bonemealableBlock.performBonemeal(level, level.random, pos, state);
      }
   }

   private static boolean isPlantBlock(BlockState state) {
      if (!state.is(CROPS_TAG) && !state.is(SAPLINGS_TAG) && !state.is(FLOWERS_TAG)) {
         Block block = state.getBlock();
         return block instanceof CropBlock
            || block.getClass().getName().equals("net.minecraft.world.level.block.VineBlock")
            || block.getClass().getName().equals("net.minecraft.world.level.block.CaveVinesBlock")
            || block.getClass().getName().equals("net.minecraft.world.level.block.BambooBlock")
            || block.getClass().getName().equals("net.minecraft.world.level.block.SugarCaneBlock")
            || block.getClass().getName().equals("net.minecraft.world.level.block.CactusBlock")
            || block.getClass().getName().equals("net.minecraft.world.level.block.NetherWartBlock")
            || block.getClass().getName().equals("net.minecraft.world.level.block.CocoaBlock")
            || block.getClass().getName().equals("net.minecraft.world.level.block.SweetBerryBushBlock");
      } else {
         return true;
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
