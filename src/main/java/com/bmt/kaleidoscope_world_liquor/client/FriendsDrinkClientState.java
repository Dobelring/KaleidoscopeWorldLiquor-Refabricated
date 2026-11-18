package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 官方 1.2.0：干杯之歌客户端状态——服务端下发的效果实体集合（TTL 60t），
 * 加上效果源 16 格内的相邻实体（含玩偶），供舞蹈渲染与音效查询。
 */
@Environment(EnvType.CLIENT)
public final class FriendsDrinkClientState {
   private static final int TTL_TICKS = 60;
   private static final double SPREAD_RANGE = 16.0;
   private static final ResourceLocation DOLL_ENTITY_ID = new ResourceLocation("kaleidoscope_doll", "doll");
   private static final Map<Integer, Tracked> TRACKED = new HashMap<>();
   private static final Set<Integer> AFFECTED = new HashSet<>();
   private static long ticks;

   private FriendsDrinkClientState() {
   }

   public static void accept(int[] entityIds, int[] durations) {
      Set<Integer> received = new HashSet<>(Math.max(4, entityIds.length * 2));

      for (int i = 0; i < entityIds.length; i++) {
         int id = entityIds[i];
         received.add(id);
         Tracked tracked = TRACKED.get(id);
         if (tracked == null) {
            TRACKED.put(id, new Tracked(durations[i], ticks));
         } else {
            tracked.duration = durations[i];
            tracked.refreshTick = ticks;
         }
      }

      TRACKED.keySet().removeIf(idx -> !received.contains(idx));
   }

   public static void tick(ClientLevel level, Player player) {
      ticks++;
      MobEffect effect = ModEffects.OHH_YE_YE;
      if (effect != null && level != null) {
         TRACKED.entrySet().removeIf(entry -> ticks - entry.getValue().refreshTick > TTL_TICKS);
         AFFECTED.clear();
         AFFECTED.addAll(TRACKED.keySet());

         for (Integer id : TRACKED.keySet()) {
            Entity source = level.getEntity(id);
            if (source != null) {
               addNearbyAffected(level, source);
            }
         }

         if (player != null && player.hasEffect(effect)) {
            AFFECTED.add(player.getId());
            addNearbyAffected(level, player);
         }
      } else {
         clear();
      }
   }

   private static void addNearbyAffected(ClientLevel level, Entity source) {
      for (Entity nearby : level.getEntities(
         source,
         source.getBoundingBox().inflate(SPREAD_RANGE),
         entity -> entity instanceof LivingEntity || DOLL_ENTITY_ID.equals(entity.getType().builtInRegistryHolder().key().location())
      )) {
         AFFECTED.add(nearby.getId());
      }
   }

   public static boolean isAffected(Entity entity) {
      return AFFECTED.contains(entity.getId());
   }

   public static boolean isBuffed(Entity entity) {
      return TRACKED.containsKey(entity.getId());
   }

   public static int getDuration(int entityId) {
      Tracked tracked = TRACKED.get(entityId);
      return tracked == null ? 0 : tracked.duration;
   }

   public static Set<Integer> getBuffedIds() {
      return TRACKED.keySet();
   }

   public static void clear() {
      TRACKED.clear();
      AFFECTED.clear();
   }

   private static final class Tracked {
      private int duration;
      private long refreshTick;

      private Tracked(int duration, long refreshTick) {
         this.duration = duration;
         this.refreshTick = refreshTick;
      }
   }
}
