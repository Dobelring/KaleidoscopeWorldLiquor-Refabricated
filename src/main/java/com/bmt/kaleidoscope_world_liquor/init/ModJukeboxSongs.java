package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

/**
 * 数据驱动 JukeboxSong（data/&lt;ns&gt;/jukebox_song/*.json）。
 * random_disc_two/music_disc.random_disc 的声音层 50/50 随机会导致 length_in_seconds 与实际播放曲长不匹配
 * （短曲播完后唱片机仍按长曲时长发音符粒子），因此拆成两个 song，放入唱片机时随机选一首。
 */
public class ModJukeboxSongs {
   /** 长曲 custom_music_2（146.7s），默认组件。 */
   public static final ResourceKey<JukeboxSong> RANDOM_DISC = create("random_disc");
   /** 短曲 custom_music_1（104.7s），放唱片机时随机写入组件。 */
   public static final ResourceKey<JukeboxSong> RANDOM_DISC_SHORT = create("random_disc_short");

   private static ResourceKey<JukeboxSong> create(String name) {
      return ResourceKey.create(Registries.JUKEBOX_SONG, KaleidoscopeWorldLiquor.id(name));
   }
}
