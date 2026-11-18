package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.client.sound.FriendsDrinkSoundInstance;
import com.bmt.kaleidoscope_world_liquor.client.FriendsDrinkClientState;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * 官方 1.2.0：干杯之歌客户端音效管理——受影响实体在听距内时播放随体唱片音，
 * 200t 重播窗口防抖；换维度全停。
 */
@Environment(EnvType.CLIENT)
public final class FriendsDrinkSoundHandler {
   private static final double HEAR_RANGE_SQR = 4096.0;
   private static final long REPLAY_DELAY_TICKS = 200L;
   private static final Map<Integer, Playing> PLAYING = new HashMap<>();
   private static long clientTicks;
   private static ClientLevel lastLevel;

   private FriendsDrinkSoundHandler() {
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register(FriendsDrinkSoundHandler::onClientTick);
   }

   private static void onClientTick(Minecraft mc) {
      clientTicks++;
      MobEffect effect = ModEffects.OHH_YE_YE;
      if (mc.level != null && mc.player != null && effect != null) {
         if (mc.level != lastLevel) {
            stopAll(mc);
            FriendsDrinkClientState.clear();
            lastLevel = mc.level;
         }

         FriendsDrinkClientState.tick(mc.level, mc.player);
         Set<Integer> seen = new HashSet<>();

         for (Integer id : FriendsDrinkClientState.getBuffedIds()) {
            if (mc.level.getEntity(id) instanceof LivingEntity living && !(living.distanceToSqr(mc.player) > HEAR_RANGE_SQR)) {
               seen.add(id);
               update(mc, living, FriendsDrinkClientState.getDuration(id));
            }
         }

         MobEffectInstance self = mc.player.getEffect(effect);
         if (self != null) {
            seen.add(mc.player.getId());
            update(mc, mc.player, self.getDuration());
         }

         Iterator<Map.Entry<Integer, Playing>> iterator = PLAYING.entrySet().iterator();

         while (iterator.hasNext()) {
            Map.Entry<Integer, Playing> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
               entry.getValue().sound.stop();
               mc.getSoundManager().stop(entry.getValue().sound);
               iterator.remove();
            }
         }
      } else {
         stopAll(mc);
         lastLevel = null;
      }
   }

   private static void update(Minecraft mc, LivingEntity entity, int duration) {
      int id = entity.getId();
      Playing playing = PLAYING.get(id);
      if (playing != null) {
         boolean restart = playing.entity != entity || playing.sound.isStopped() || duration > playing.lastDuration + 1;
         boolean finished = clientTicks - playing.startTick >= REPLAY_DELAY_TICKS && !mc.getSoundManager().isActive(playing.sound);
         if (!restart && !finished) {
            playing.lastDuration = duration;
            return;
         }

         playing.sound.stop();
         mc.getSoundManager().stop(playing.sound);
         PLAYING.remove(id);
      }

      FriendsDrinkSoundInstance sound = new FriendsDrinkSoundInstance(entity);
      mc.getSoundManager().play(sound);
      PLAYING.put(id, new Playing(entity, sound, duration, clientTicks));
   }

   private static void stopAll(Minecraft mc) {
      for (Playing playing : PLAYING.values()) {
         playing.sound.stop();
         mc.getSoundManager().stop(playing.sound);
      }

      PLAYING.clear();
   }

   private static final class Playing {
      private final LivingEntity entity;
      private final FriendsDrinkSoundInstance sound;
      private final long startTick;
      private int lastDuration;

      private Playing(LivingEntity entity, FriendsDrinkSoundInstance sound, int lastDuration, long startTick) {
         this.entity = entity;
         this.sound = sound;
         this.lastDuration = lastDuration;
         this.startTick = startTick;
      }
   }
}
