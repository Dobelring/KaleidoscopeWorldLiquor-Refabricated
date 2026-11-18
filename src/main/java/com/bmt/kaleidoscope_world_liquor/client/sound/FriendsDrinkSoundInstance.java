package com.bmt.kaleidoscope_world_liquor.client.sound;

import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

/**
 * 官方 1.2.0：干杯之歌随实体移动的循环唱片音（RECORDS 源、音量 4）。
 */
@Environment(EnvType.CLIENT)
public class FriendsDrinkSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
   private final Entity entity;
   private boolean stopped;

   public FriendsDrinkSoundInstance(Entity entity) {
      super(ModSounds.MUSIC_DISC_FRIENDS_DRINK, SoundSource.RECORDS, RandomSource.create());
      this.entity = entity;
      this.volume = 4.0F;
      this.pitch = 1.0F;
      this.attenuation = Attenuation.LINEAR;
      this.relative = false;
      this.updatePosition();
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void stop() {
      this.stopped = true;
   }

   public boolean isStopped() {
      return this.stopped;
   }

   private void updatePosition() {
      this.x = this.entity.getX();
      this.y = this.entity.getY();
      this.z = this.entity.getZ();
   }

   @Override
   public void tick() {
      if (this.entity.isRemoved() || !this.entity.isAlive() || this.entity.level() == null) {
         this.stopped = true;
         return;
      }

      this.updatePosition();
   }
}
