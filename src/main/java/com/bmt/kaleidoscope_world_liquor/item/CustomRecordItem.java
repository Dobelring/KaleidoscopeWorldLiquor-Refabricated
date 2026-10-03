package com.bmt.kaleidoscope_world_liquor.item;

import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import java.util.Random;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Item.Properties;

public class CustomRecordItem extends RecordItem {
    private static final Random RANDOM = new Random();

    public CustomRecordItem() {
        super(15, ModSounds.CUSTOM_MUSIC_1, new Properties().stacksTo(1).rarity(Rarity.RARE), 2920);
    }

    public SoundEvent getSound() {
        return RANDOM.nextBoolean() ? ModSounds.CUSTOM_MUSIC_1 : ModSounds.CUSTOM_MUSIC_2;
    }
}
